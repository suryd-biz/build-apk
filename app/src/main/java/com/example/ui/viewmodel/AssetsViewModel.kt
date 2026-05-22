package com.example.ui.viewmodel

import android.app.Application
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.database.AppDatabase
import com.example.data.database.AssetProfileEntity
import com.example.data.database.AssetSnapshotEntity
import com.example.data.repository.AssetRepository
import com.example.utils.ScreenUtils
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.util.UUID

class AssetsViewModel(application: Application) : AndroidViewModel(application) {

    private val db = AppDatabase.getDatabase(application)
    private val repository = AssetRepository(db.assetDao())

    var assetUrl by mutableStateOf("https://script.google.com/macros/s/AKfycbwNpR6fbf9N4FynWgu9wuefPBl-7kgtpZhougkmMee5eyF_NB1sUJ30zr-QCd6eXYs-oQ/exec")

    // Core Flows
    val assetProfiles: StateFlow<List<AssetProfileEntity>> = repository.allProfiles
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    private val _selectedProfile = MutableStateFlow<AssetProfileEntity?>(null)
    val selectedProfile: StateFlow<AssetProfileEntity?> = _selectedProfile.asStateFlow()

    private val _snapshots = MutableStateFlow<List<AssetSnapshotEntity>>(emptyList())
    val snapshots: StateFlow<List<AssetSnapshotEntity>> = _snapshots.asStateFlow()

    // UI States
    var isHidden by mutableStateOf(false)
    var currentPage by mutableStateOf(1)
    val itemsPerPage = 5

    var timelineFilter by mutableStateOf("hari") // hari, minggu, bulan, tahun
    var syncStatus by mutableStateOf("IDLE") // IDLE, SYNCING, SUCCESS, ERROR, UNAUTHORIZED

    var isPinModalOpen by mutableStateOf(false)
    var pendingProfileToVerify by mutableStateOf<AssetProfileEntity?>(null)
    var pinInputText by mutableStateOf("")

    // Modals
    var addProfileModalOpen by mutableStateOf(false)
    var reorderModalOpen by mutableStateOf(false)
    var snapshotInputModalOpen by mutableStateOf(false)
    var detailModalOpen by mutableStateOf(false)
    var activeDetailSnapshot by mutableStateOf<AssetSnapshotEntity?>(null)

    // Form inputs
    var formTanggal by mutableStateOf("")
    var formKeterangan by mutableStateOf("")
    var formBni by mutableStateOf("")
    var formSeabank by mutableStateOf("")
    var formBibit by mutableStateOf("")
    var formTunai by mutableStateOf("")
    var formFlip by mutableStateOf("")
    var formOvo by mutableStateOf("")
    var formShopeepay by mutableStateOf("")
    var formDana by mutableStateOf("")
    var formJago by mutableStateOf("")
    var formLain by mutableStateOf("")
    var formAction by mutableStateOf("insert") // "insert" or "update"
    var formOriginalTanggal by mutableStateOf("")

    init {
        refreshProfiles()
        resetSnapshotForm()
    }

    fun selectProfile(profile: AssetProfileEntity, savedPin: String? = null) {
        val pin = savedPin ?: profile.pin ?: ""
        if (profile.hasPin && savedPin == null) {
            pendingProfileToVerify = profile
            pinInputText = ""
            isPinModalOpen = true
        } else {
            viewModelScope.launch {
                _selectedProfile.value = profile
                syncSnapshots(profile, pin)
                repository.getSnapshotsForAsset(profile.id).collect {
                    _snapshots.value = it
                }
            }
        }
    }

    fun verifyPinAndUnlock(pin: String) {
        val profile = pendingProfileToVerify ?: return
        viewModelScope.launch {
            syncStatus = "SYNCING"
            val res = repository.syncSnapshots(assetUrl, profile.id, pin)
            if (res.isSuccess) {
                // Pin correct -> save verified profile
                repository.createAssetProfile(assetUrl, profile.id, profile.nama, pin)
                isPinModalOpen = false
                pendingProfileToVerify = null
                _selectedProfile.value = profile.copy(pin = pin)
                syncStatus = "SUCCESS"

                repository.getSnapshotsForAsset(profile.id).collect {
                    _snapshots.value = it
                }
            } else {
                syncStatus = "UNAUTHORIZED"
            }
        }
    }

    fun refreshProfiles() {
        viewModelScope.launch {
            syncStatus = "SYNCING"
            val success = repository.syncAssetProfiles(assetUrl)
            syncStatus = if (success) "SUCCESS" else "ERROR"
            val current = _selectedProfile.value
            if (current != null) {
                syncSnapshots(current, current.pin ?: "")
            }
        }
    }

    private suspend fun syncSnapshots(profile: AssetProfileEntity, pin: String) {
        syncStatus = "SYNCING"
        val res = repository.syncSnapshots(assetUrl, profile.id, pin)
        if (res.isSuccess) {
            syncStatus = "SUCCESS"
        } else {
            val errString = res.exceptionOrNull()?.message
            syncStatus = if (errString == "UNAUTHORIZED") "UNAUTHORIZED" else "ERROR"
        }
    }

    fun createProfile(nama: String, pin: String?) {
        viewModelScope.launch {
            syncStatus = "SYNCING"
            val id = UUID.randomUUID().toString()
            val success = repository.createAssetProfile(assetUrl, id, nama, pin)
            syncStatus = if (success) "SUCCESS" else "ERROR"
            addProfileModalOpen = false
            refreshProfiles()
        }
    }

    fun updateProfile(nama: String, pin: String?) {
        val current = _selectedProfile.value ?: return
        viewModelScope.launch {
            syncStatus = "SYNCING"
            val success = repository.updateAssetProfile(assetUrl, current.id, nama, pin)
            syncStatus = if (success) "SUCCESS" else "ERROR"
            refreshProfiles()
        }
    }

    fun deleteProfile() {
        val current = _selectedProfile.value ?: return
        viewModelScope.launch {
            syncStatus = "SYNCING"
            val success = repository.deleteAssetProfile(assetUrl, current.id)
            syncStatus = if (success) "SUCCESS" else "ERROR"
            _selectedProfile.value = null
            _snapshots.value = emptyList()
            refreshProfiles()
        }
    }

    fun saveSnapshot() {
        val profile = _selectedProfile.value ?: return
        val bniVal = formBni.toDoubleOrNull() ?: 0.0
        val seabankVal = formSeabank.toDoubleOrNull() ?: 0.0
        val bibitVal = formBibit.toDoubleOrNull() ?: 0.0
        val tunaiVal = formTunai.toDoubleOrNull() ?: 0.0
        val flipVal = formFlip.toDoubleOrNull() ?: 0.0
        val ovoVal = formOvo.toDoubleOrNull() ?: 0.0
        val shopeepayVal = formShopeepay.toDoubleOrNull() ?: 0.0
        val danaVal = formDana.toDoubleOrNull() ?: 0.0
        val jagoVal = formJago.toDoubleOrNull() ?: 0.0
        val lainVal = formLain.toDoubleOrNull() ?: 0.0

        val totalVal = bniVal + seabankVal + bibitVal + tunaiVal + flipVal + ovoVal + shopeepayVal + danaVal + jagoVal + lainVal

        val snap = AssetSnapshotEntity(
            compositeId = "${profile.id}|$formTanggal",
            assetId = profile.id,
            rawTanggal = formTanggal,
            tanggal = formTanggal,
            keterangan = formKeterangan.ifBlank { "-" },
            bni = bniVal,
            seabank = seabankVal,
            bibit = bibitVal,
            tunai = tunaiVal,
            flip = flipVal,
            ovo = ovoVal,
            shopeepay = shopeepayVal,
            dana = danaVal,
            jago = jagoVal,
            lain = lainVal,
            total = totalVal
        )

        viewModelScope.launch {
            syncStatus = "SYNCING"
            val success = repository.saveSnapshot(
                scriptUrl = assetUrl,
                action = formAction,
                pin = profile.pin ?: "",
                assetId = profile.id,
                originalTanggal = formOriginalTanggal,
                snap = snap
            )
            syncStatus = if (success) "SUCCESS" else "ERROR"
            resetSnapshotForm()
            snapshotInputModalOpen = false

            repository.getSnapshotsForAsset(profile.id).first().let {
                _snapshots.value = it
            }
        }
    }

    fun deleteSnapshot(rawTanggal: String) {
        val profile = _selectedProfile.value ?: return
        viewModelScope.launch {
            syncStatus = "SYNCING"
            val success = repository.deleteSnapshot(assetUrl, rawTanggal, profile.id, profile.pin ?: "")
            syncStatus = if (success) "SUCCESS" else "ERROR"
            repository.getSnapshotsForAsset(profile.id).first().let {
                _snapshots.value = it
            }
        }
    }

    fun openEditSnapshotMode(snap: AssetSnapshotEntity) {
        formAction = "update"
        formOriginalTanggal = snap.rawTanggal
        formTanggal = snap.rawTanggal
        formKeterangan = if (snap.keterangan == "-") "" else snap.keterangan
        formBni = snap.bni.toLong().toString()
        formSeabank = snap.seabank.toLong().toString()
        formBibit = snap.bibit.toLong().toString()
        formTunai = snap.tunai.toLong().toString()
        formFlip = snap.flip.toLong().toString()
        formOvo = snap.ovo.toLong().toString()
        formShopeepay = snap.shopeepay.toLong().toString()
        formDana = snap.dana.toLong().toString()
        formJago = snap.jago.toLong().toString()
        formLain = snap.lain.toLong().toString()
        snapshotInputModalOpen = true
    }

    fun openInsertSnapshotMode() {
        formAction = "insert"
        val latest = _snapshots.value.firstOrNull()
        formTanggal = ScreenUtils.getCurrentDateString()
        formKeterangan = ""
        formBni = latest?.bni?.toLong()?.toString() ?: "0"
        formSeabank = latest?.seabank?.toLong()?.toString() ?: "0"
        formBibit = latest?.bibit?.toLong()?.toString() ?: "0"
        formTunai = latest?.tunai?.toLong()?.toString() ?: "0"
        formFlip = latest?.flip?.toLong()?.toString() ?: "0"
        formOvo = latest?.ovo?.toLong()?.toString() ?: "0"
        formShopeepay = latest?.shopeepay?.toLong()?.toString() ?: "0"
        formDana = latest?.dana?.toLong()?.toString() ?: "0"
        formJago = latest?.jago?.toLong()?.toString() ?: "0"
        formLain = latest?.lain?.toLong()?.toString() ?: "0"
        snapshotInputModalOpen = true
    }

    fun resetSnapshotForm() {
        formAction = "insert"
        formOriginalTanggal = ""
        formTanggal = ScreenUtils.getCurrentDateString()
        formKeterangan = ""
        formBni = "0"
        formSeabank = "0"
        formBibit = "0"
        formTunai = "0"
        formFlip = "0"
        formOvo = "0"
        formShopeepay = "0"
        formDana = "0"
        formJago = "0"
        formLain = "0"
    }

    fun reorderProfiles(orders: List<String>) {
        viewModelScope.launch {
            repository.updateAssetOrders(orders)
        }
    }
}

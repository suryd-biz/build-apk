package com.example.ui.viewmodel

import android.app.Application
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.database.AppDatabase
import com.example.data.database.TransactionEntity
import com.example.data.database.WalletEntity
import com.example.data.repository.WalletRepository
import com.example.utils.ScreenUtils
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.util.UUID

class WalletsViewModel(application: Application) : AndroidViewModel(application) {

    private val db = AppDatabase.getDatabase(application)
    private val repository = WalletRepository(db.walletDao())

    var walletsUrl by mutableStateOf("https://script.google.com/macros/s/AKfycbycNjBAIkYHjvU53hXnQM8WKRrAKweFBWWlwUHrIODrVkrHEONxN1nYrWm2LvSXYET5aA/exec")

    // Core Flows
    val wallets: StateFlow<List<WalletEntity>> = repository.allWallets
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    private val _selectedWallet = MutableStateFlow<WalletEntity?>(null)
    val selectedWallet: StateFlow<WalletEntity?> = _selectedWallet.asStateFlow()

    private val _transactions = MutableStateFlow<List<TransactionEntity>>(emptyList())
    val transactions: StateFlow<List<TransactionEntity>> = _transactions.asStateFlow()

    // UI States
    var isMasked by mutableStateOf(true)
    var currentPage by mutableStateOf(1)
    val itemsPerPage = 5

    var calcExpression by mutableStateOf("")
    var calcResult by mutableStateOf("-")

    var syncStatus by mutableStateOf("IDLE") // IDLE, SYNCING, SUCCESS, ERROR, UNAUTHORIZED
    var isPinModalOpen by mutableStateOf(false)
    var pendingWalletToVerify by mutableStateOf<WalletEntity?>(null)
    var pinInputText by mutableStateOf("")

    // Modals
    var helpModalOpen by mutableStateOf(false)
    var addWalletModalOpen by mutableStateOf(false)
    var reorderModalOpen by mutableStateOf(false)

    // Form Inputs
    var formTanggal by mutableStateOf("")
    var formJenis by mutableStateOf("Masuk") // "Masuk" or "Keluar"
    var formKategori by mutableStateOf("Gaji Utama")
    var formNominal by mutableStateOf("")
    var formKeterangan by mutableStateOf("")
    var formEditId by mutableStateOf<String?>(null) // null for insert, non-null for edit

    init {
        refreshWallets()
        resetForm()
    }

    fun selectWallet(wallet: WalletEntity, savedPin: String? = null) {
        val pin = savedPin ?: wallet.pin ?: ""
        if (wallet.hasPin && savedPin == null) {
            // Need PIN input
            pendingWalletToVerify = wallet
            pinInputText = ""
            isPinModalOpen = true
        } else {
            viewModelScope.launch {
                _selectedWallet.value = wallet
                // Sync transactions for this wallet
                syncTransactions(wallet, pin)
                // Observe local db
                repository.getTransactionsForWallet(wallet.id).collect {
                    _transactions.value = it
                }
            }
        }
    }

    fun verifyPinAndUnlock(pin: String) {
        val wallet = pendingWalletToVerify ?: return
        viewModelScope.launch {
            syncStatus = "SYNCING"
            val res = repository.syncTransactions(walletsUrl, wallet.id, pin)
            if (res.isSuccess) {
                // Pin correct -> save wallet with verified PIN
                repository.createWallet(walletsUrl, wallet.id, wallet.nama, pin)
                isPinModalOpen = false
                pendingWalletToVerify = null
                _selectedWallet.value = wallet.copy(pin = pin)
                syncStatus = "SUCCESS"

                repository.getTransactionsForWallet(wallet.id).collect {
                    _transactions.value = it
                }
            } else {
                syncStatus = "UNAUTHORIZED"
            }
        }
    }

    fun refreshWallets() {
        viewModelScope.launch {
            syncStatus = "SYNCING"
            val success = repository.syncWallets(walletsUrl)
            syncStatus = if (success) "SUCCESS" else "ERROR"
            // If we have select wallet, sync its transactions too
            val current = _selectedWallet.value
            if (current != null) {
                syncTransactions(current, current.pin ?: "")
            }
        }
    }

    private suspend fun syncTransactions(wallet: WalletEntity, pin: String) {
        syncStatus = "SYNCING"
        val res = repository.syncTransactions(walletsUrl, wallet.id, pin)
        if (res.isSuccess) {
            syncStatus = "SUCCESS"
        } else {
            val errString = res.exceptionOrNull()?.message
            syncStatus = if (errString == "UNAUTHORIZED") "UNAUTHORIZED" else "ERROR"
        }
    }

    fun createWallet(nama: String, pin: String?) {
        viewModelScope.launch {
            syncStatus = "SYNCING"
            val id = UUID.randomUUID().toString()
            val success = repository.createWallet(walletsUrl, id, nama, pin)
            syncStatus = if (success) "SUCCESS" else "ERROR"
            addWalletModalOpen = false
            refreshWallets()
        }
    }

    fun updateWallet(nama: String, pin: String?) {
        val current = _selectedWallet.value ?: return
        viewModelScope.launch {
            syncStatus = "SYNCING"
            val success = repository.updateWallet(walletsUrl, current.id, nama, pin)
            syncStatus = if (success) "SUCCESS" else "ERROR"
            refreshWallets()
        }
    }

    fun deleteWallet() {
        val current = _selectedWallet.value ?: return
        viewModelScope.launch {
            syncStatus = "SYNCING"
            val success = repository.deleteWallet(walletsUrl, current.id)
            syncStatus = if (success) "SUCCESS" else "ERROR"
            _selectedWallet.value = null
            _transactions.value = emptyList()
            refreshWallets()
        }
    }

    fun reorderWallets(orders: List<String>) {
        viewModelScope.launch {
            repository.updateWalletOrders(walletsUrl, orders)
        }
    }

    fun saveTransaction() {
        val wallet = _selectedWallet.value ?: return
        val nominalVal = formNominal.toDoubleOrNull() ?: 0.0
        if (nominalVal <= 0) return

        viewModelScope.launch {
            syncStatus = "SYNCING"
            val editId = formEditId
            val tx = TransactionEntity(
                id = editId ?: UUID.randomUUID().toString(),
                tanggal = formTanggal,
                jenis = formJenis,
                kategori = formKategori,
                nominal = nominalVal,
                keterangan = formKeterangan,
                walletId = wallet.id
            )

            val success = if (editId == null) {
                repository.insertTransaction(walletsUrl, tx)
            } else {
                repository.updateTransaction(walletsUrl, tx)
            }

            syncStatus = if (success) "SUCCESS" else "ERROR"
            resetForm()
            // Pull newest transactions update direct from cache stream
            repository.getTransactionsForWallet(wallet.id).first().let {
                _transactions.value = it
            }
        }
    }

    fun editTransactionMode(tx: TransactionEntity) {
        formEditId = tx.id
        formTanggal = tx.tanggal
        formJenis = tx.jenis
        formKategori = tx.kategori
        formNominal = tx.nominal.toLong().toString()
        formKeterangan = tx.keterangan
    }

    fun deleteTransaction(id: String) {
        val wallet = _selectedWallet.value ?: return
        viewModelScope.launch {
            syncStatus = "SYNCING"
            val success = repository.deleteTransaction(walletsUrl, id, wallet.id)
            syncStatus = if (success) "SUCCESS" else "ERROR"
            repository.getTransactionsForWallet(wallet.id).first().let {
                _transactions.value = it
            }
        }
    }

    fun resetForm() {
        formEditId = null
        formTanggal = ScreenUtils.getCurrentDateString()
        formJenis = "Masuk"
        formKategori = "Gaji Utama"
        formNominal = ""
        formKeterangan = ""
    }

    fun computeExpression() {
        try {
            val expr = calcExpression.replace("x", "*", ignoreCase = true)
            val result = ScreenUtils.evaluateMathExpression(expr)
            calcResult = result?.let { ScreenUtils.formatToIdr(it) } ?: "Error"
        } catch (e: Exception) {
            calcResult = "Error"
        }
    }
}

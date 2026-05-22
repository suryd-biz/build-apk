package com.example.data.repository

import com.example.data.api.AssetHistoryResponse
import com.example.data.api.AssetProfileListResponse
import com.example.data.api.NetworkClient
import com.example.data.database.AssetDao
import com.example.data.database.AssetProfileEntity
import com.example.data.database.AssetSnapshotEntity
import com.squareup.moshi.JsonAdapter
import com.squareup.moshi.Types
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.withContext

class AssetRepository(private val assetDao: AssetDao) {

    val allProfiles: Flow<List<AssetProfileEntity>> = assetDao.getAllProfilesFlow()

    fun getSnapshotsForAsset(assetId: String): Flow<List<AssetSnapshotEntity>> =
        assetDao.getSnapshotsForAssetFlow(assetId)

    suspend fun clearAllData() = withContext(Dispatchers.IO) {
        assetDao.clearProfiles()
        assetDao.clearSnapshots()
    }

    // --- CLOUD SYNC: ASSET PROFILES ---
    suspend fun syncAssetProfiles(scriptUrl: String): Boolean = withContext(Dispatchers.IO) {
        try {
            val response = NetworkClient.service.getAssetAction(
                url = scriptUrl,
                queries = mapOf("action" to "get_assets")
            )
            if (response.isSuccessful) {
                val bodyStr = response.body()?.string() ?: ""
                val adapter: JsonAdapter<AssetProfileListResponse> = NetworkClient.moshi.adapter(AssetProfileListResponse::class.java)
                val serverResult = adapter.fromJson(bodyStr)
                val serverAssets = serverResult?.assets
                if (serverAssets != null) {
                    val currentLocal = assetDao.getAllProfilesDirect()
                    val orderMap = currentLocal.associate { it.id to it.urutan }
                    val entities = serverAssets.mapIndexed { index, api ->
                        AssetProfileEntity(
                            id = api.id,
                            nama = api.nama,
                            hasPin = api.hasPin,
                            urutan = orderMap[api.id] ?: index,
                            pin = currentLocal.find { it.id == api.id }?.pin
                        )
                    }
                    assetDao.clearProfiles()
                    assetDao.insertProfiles(entities)
                    return@withContext true
                }
            }
            false
        } catch (e: Exception) {
            e.printStackTrace()
            false
        }
    }

    suspend fun createAssetProfile(scriptUrl: String, id: String, nama: String, pin: String?): Boolean = withContext(Dispatchers.IO) {
        try {
            val newProfile = AssetProfileEntity(id = id, nama = nama, hasPin = !pin.isNullOrBlank(), pin = pin)
            assetDao.insertProfile(newProfile)

            val body = mapOf(
                "action" to "create_asset",
                "id" to id,
                "nama" to nama,
                "pin" to (pin ?: "")
            )
            NetworkClient.service.postAssetAction(scriptUrl, body)
            true
        } catch (e: Exception) {
            e.printStackTrace()
            false
        }
    }

    suspend fun updateAssetProfile(scriptUrl: String, id: String, nama: String, pin: String?): Boolean = withContext(Dispatchers.IO) {
        try {
            val current = assetDao.getAllProfilesDirect().find { it.id == id }
            if (current != null) {
                val hasPin = if (pin == "HAPUS_PIN") false else (if (!pin.isNullOrBlank()) true else current.hasPin)
                val profilePin = if (pin == "HAPUS_PIN") null else (if (!pin.isNullOrBlank()) pin else current.pin)
                assetDao.insertProfile(current.copy(nama = nama, hasPin = hasPin, pin = profilePin))
            }

            val body = mapOf(
                "action" to "update_asset_profile",
                "id" to id,
                "nama" to nama,
                "pin" to (pin ?: "")
            )
            NetworkClient.service.postAssetAction(scriptUrl, body)
            true
        } catch (e: Exception) {
            e.printStackTrace()
            false
        }
    }

    suspend fun deleteAssetProfile(scriptUrl: String, id: String): Boolean = withContext(Dispatchers.IO) {
        try {
            assetDao.deleteProfileById(id)
            assetDao.deleteSnapshotsByAssetId(id)

            val body = mapOf(
                "action" to "delete_asset_profile",
                "id" to id
            )
            NetworkClient.service.postAssetAction(scriptUrl, body)
            true
        } catch (e: Exception) {
            e.printStackTrace()
            false
        }
    }

    suspend fun updateAssetOrders(orders: List<String>) = withContext(Dispatchers.IO) {
        try {
            val profiles = assetDao.getAllProfilesDirect()
            val reordered = profiles.map { profile ->
                val newIndex = orders.indexOf(profile.id)
                profile.copy(urutan = if (newIndex != -1) newIndex else profile.urutan)
            }
            assetDao.insertProfiles(reordered)
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    // --- CLOUD SYNC: SNAPSHOTS ---
    suspend fun syncSnapshots(scriptUrl: String, assetId: String, pin: String): Result<Boolean> = withContext(Dispatchers.IO) {
        try {
            val response = NetworkClient.service.getAssetAction(
                scriptUrl,
                mapOf(
                    "assetId" to assetId,
                    "assetPin" to pin
                )
            )
            if (response.isSuccessful) {
                val bodyStr = response.body()?.string() ?: ""

                val isErrorObj = bodyStr.trim().startsWith("{")
                if (isErrorObj) {
                    val errorAdapter = NetworkClient.moshi.adapter(AssetHistoryResponse::class.java)
                    val errorResult = errorAdapter.fromJson(bodyStr)
                    if (errorResult?.status == "error" && errorResult.message == "UNAUTHORIZED") {
                        return@withContext Result.failure(Exception("UNAUTHORIZED"))
                    }
                }

                val type = Types.newParameterizedType(List::class.java, com.example.data.api.AssetSnapshotApiModel::class.java)
                val listAdapter: JsonAdapter<List<com.example.data.api.AssetSnapshotApiModel>> = NetworkClient.moshi.adapter(type)
                val serverItems = listAdapter.fromJson(bodyStr)
                if (serverItems != null) {
                    val entities = serverItems.map { api ->
                        val rawTgl = api.raw_tanggal ?: api.tanggal ?: ""
                        AssetSnapshotEntity(
                            compositeId = "$assetId|$rawTgl",
                            assetId = assetId,
                            rawTanggal = rawTgl,
                            tanggal = api.tanggal ?: rawTgl,
                            keterangan = api.keterangan ?: "",
                            bni = api.getBniVal(),
                            seabank = api.getSeabankVal(),
                            bibit = api.getBibitVal(),
                            tunai = api.getTunaiVal(),
                            flip = api.getFlipVal(),
                            ovo = api.getOvoVal(),
                            shopeepay = api.getShopeepayVal(),
                            dana = api.getDanaVal(),
                            jago = api.getJagoVal(),
                            lain = api.getLainVal(),
                            total = api.total ?: 0.0
                        )
                    }
                    assetDao.deleteSnapshotsByAssetId(assetId)
                    assetDao.insertSnapshots(entities)
                    return@withContext Result.success(true)
                }
            }
            Result.success(false)
        } catch (e: Exception) {
            e.printStackTrace()
            Result.failure(e)
        }
    }

    suspend fun saveSnapshot(
        scriptUrl: String,
        action: String,
        pin: String,
        assetId: String,
        originalTanggal: String,
        snap: AssetSnapshotEntity
    ): Boolean = withContext(Dispatchers.IO) {
        try {
            assetDao.insertSnapshot(snap)
            if (action == "update" && originalTanggal != snap.rawTanggal) {
                assetDao.deleteSnapshotByCompositeId("$assetId|$originalTanggal")
            }

            val body = mapOf(
                "action" to action,
                "pin" to pin,
                "assetId" to assetId,
                "original_tanggal" to originalTanggal,
                "tanggal" to snap.rawTanggal,
                "keterangan" to snap.keterangan,
                "bni" to snap.bni,
                "seabank" to snap.seabank,
                "bibit" to snap.bibit,
                "tunai" to snap.tunai,
                "flip" to snap.flip,
                "ovo" to snap.ovo,
                "shopeepay" to snap.shopeepay,
                "dana" to snap.dana,
                "jago" to snap.jago,
                "lain" to snap.lain
            )
            NetworkClient.service.postAssetAction(scriptUrl, body)
            true
        } catch (e: Exception) {
            e.printStackTrace()
            false
        }
    }

    suspend fun deleteSnapshot(
        scriptUrl: String,
        rawTanggal: String,
        assetId: String,
        pin: String
    ): Boolean = withContext(Dispatchers.IO) {
        try {
            assetDao.deleteSnapshotByCompositeId("$assetId|$rawTanggal")

            val body = mapOf(
                "action" to "delete",
                "tanggal" to rawTanggal,
                "assetId" to assetId,
                "pin" to pin
            )
            NetworkClient.service.postAssetAction(scriptUrl, body)
            true
        } catch (e: Exception) {
            e.printStackTrace()
            false
        }
    }
}

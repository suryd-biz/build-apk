package com.example.data.repository

import com.example.data.api.NetworkClient
import com.example.data.api.TransactionListResponse
import com.example.data.api.WalletListResponse
import com.example.data.database.TransactionEntity
import com.example.data.database.WalletDao
import com.example.data.database.WalletEntity
import com.squareup.moshi.JsonAdapter
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.withContext

class WalletRepository(private val walletDao: WalletDao) {

    val allWallets: Flow<List<WalletEntity>> = walletDao.getAllWalletsFlow()

    fun getTransactionsForWallet(walletId: String): Flow<List<TransactionEntity>> =
        walletDao.getTransactionsForWalletFlow(walletId)

    suspend fun clearAllData() = withContext(Dispatchers.IO) {
        walletDao.clearWallets()
        walletDao.clearTransactions()
    }

    // --- CLOUD SYNC: WALLETS ---
    suspend fun syncWallets(scriptUrl: String): Boolean = withContext(Dispatchers.IO) {
        try {
            val response = NetworkClient.service.getWalletAction(
                url = scriptUrl,
                queries = mapOf("action" to "get_wallets")
            )
            if (response.isSuccessful) {
                val bodyStr = response.body()?.string() ?: ""
                val adapter: JsonAdapter<WalletListResponse> = NetworkClient.moshi.adapter(WalletListResponse::class.java)
                val serverResult = adapter.fromJson(bodyStr)
                val serverWallets = serverResult?.wallets
                if (serverWallets != null) {
                    val currentLocalWallets = walletDao.getAllWalletsDirect()
                    val orderMap = currentLocalWallets.associate { it.id to it.urutan }
                    val entities = serverWallets.mapIndexed { index, api ->
                        WalletEntity(
                            id = api.id,
                            nama = api.nama,
                            hasPin = api.hasPin,
                            urutan = orderMap[api.id] ?: index,
                            pin = currentLocalWallets.find { it.id == api.id }?.pin
                        )
                    }
                    walletDao.clearWallets()
                    walletDao.insertWallets(entities)
                    return@withContext true
                }
            }
            false
        } catch (e: Exception) {
            e.printStackTrace()
            false
        }
    }

    suspend fun createWallet(scriptUrl: String, id: String, nama: String, pin: String?): Boolean = withContext(Dispatchers.IO) {
        try {
            val newWallet = WalletEntity(id = id, nama = nama, hasPin = !pin.isNullOrBlank(), pin = pin)
            walletDao.insertWallet(newWallet)

            val params = mutableMapOf(
                "action" to "create_wallet",
                "id" to id,
                "nama" to nama,
                "pin" to (pin ?: "")
            )
            NetworkClient.service.postWalletAction(scriptUrl, params)
            true
        } catch (e: Exception) {
            e.printStackTrace()
            false
        }
    }

    suspend fun updateWallet(scriptUrl: String, id: String, nama: String, pin: String?): Boolean = withContext(Dispatchers.IO) {
        try {
            val current = walletDao.getAllWalletsDirect().find { it.id == id }
            if (current != null) {
                val hasPin = if (pin == "HAPUS_PIN") false else (if (!pin.isNullOrBlank()) true else current.hasPin)
                val walletPin = if (pin == "HAPUS_PIN") null else (if (!pin.isNullOrBlank()) pin else current.pin)
                walletDao.insertWallet(current.copy(nama = nama, hasPin = hasPin, pin = walletPin))
            }

            val params = mutableMapOf(
                "action" to "update_wallet",
                "id" to id,
                "nama" to nama,
                "pin" to (pin ?: "")
            )
            NetworkClient.service.postWalletAction(scriptUrl, params)
            true
        } catch (e: Exception) {
            e.printStackTrace()
            false
        }
    }

    suspend fun deleteWallet(scriptUrl: String, id: String): Boolean = withContext(Dispatchers.IO) {
        try {
            walletDao.deleteWalletById(id)
            walletDao.deleteTransactionsByWalletId(id)

            val params = mapOf(
                "action" to "delete_wallet",
                "id" to id
            )
            NetworkClient.service.postWalletAction(scriptUrl, params)
            true
        } catch (e: Exception) {
            e.printStackTrace()
            false
        }
    }

    suspend fun updateWalletOrders(scriptUrl: String, orders: List<String>) = withContext(Dispatchers.IO) {
        try {
            val wallets = walletDao.getAllWalletsDirect()
            val reordered = wallets.map { wallet ->
                val newIndex = orders.indexOf(wallet.id)
                wallet.copy(urutan = if (newIndex != -1) newIndex else wallet.urutan)
            }
            walletDao.insertWallets(reordered)

            val params = mapOf(
                "action" to "reorder_wallets",
                "order" to orders.joinToString(",")
            )
            NetworkClient.service.postWalletAction(scriptUrl, params)
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    // --- CLOUD SYNC: TRANSACTIONS ---
    suspend fun syncTransactions(scriptUrl: String, walletId: String, pin: String): Result<Boolean> = withContext(Dispatchers.IO) {
        try {
            val response = NetworkClient.service.getWalletAction(
                scriptUrl,
                mapOf(
                    "action" to "get_transactions",
                    "walletId" to walletId,
                    "pin" to pin
                )
            )
            if (response.isSuccessful) {
                val bodyStr = response.body()?.string() ?: ""
                val adapter: JsonAdapter<TransactionListResponse> = NetworkClient.moshi.adapter(TransactionListResponse::class.java)
                val serverResult = adapter.fromJson(bodyStr)
                if (serverResult?.status == "error" && serverResult.message == "UNAUTHORIZED") {
                    return@withContext Result.failure(Exception("UNAUTHORIZED"))
                }
                val serverTxs = serverResult?.data
                if (serverTxs != null) {
                    val entities = serverTxs.map { api ->
                        TransactionEntity(
                            id = api.id,
                            tanggal = api.tanggal,
                            jenis = api.jenis,
                            kategori = api.kategori,
                            nominal = api.nominal,
                            keterangan = api.keterangan ?: "",
                            walletId = api.wallet
                        )
                    }
                    walletDao.deleteTransactionsByWalletId(walletId)
                    walletDao.insertTransactions(entities)
                    return@withContext Result.success(true)
                }
            }
            Result.success(false)
        } catch (e: Exception) {
            e.printStackTrace()
            Result.failure(e)
        }
    }

    suspend fun insertTransaction(scriptUrl: String, tx: TransactionEntity): Boolean = withContext(Dispatchers.IO) {
        try {
            walletDao.insertTransaction(tx)

            val params = mapOf(
                "action" to "insert",
                "id" to tx.id,
                "tanggal" to tx.tanggal,
                "jenis" to tx.jenis,
                "kategori" to tx.kategori,
                "nominal" to tx.nominal.toLong().toString(),
                "keterangan" to tx.keterangan,
                "wallet" to tx.walletId
            )
            NetworkClient.service.postWalletAction(scriptUrl, params)
            true
        } catch (e: Exception) {
            e.printStackTrace()
            false
        }
    }

    suspend fun updateTransaction(scriptUrl: String, tx: TransactionEntity): Boolean = withContext(Dispatchers.IO) {
        try {
            walletDao.insertTransaction(tx)

            val params = mapOf(
                "action" to "update",
                "id" to tx.id,
                "tanggal" to tx.tanggal,
                "jenis" to tx.jenis,
                "kategori" to tx.kategori,
                "nominal" to tx.nominal.toLong().toString(),
                "keterangan" to tx.keterangan,
                "wallet" to tx.walletId
            )
            NetworkClient.service.postWalletAction(scriptUrl, params)
            true
        } catch (e: Exception) {
            e.printStackTrace()
            false
        }
    }

    suspend fun deleteTransaction(scriptUrl: String, id: String, walletId: String): Boolean = withContext(Dispatchers.IO) {
        try {
            walletDao.deleteTransactionById(id)

            val params = mapOf(
                "action" to "delete",
                "id" to id,
                "wallet" to walletId
            )
            NetworkClient.service.postWalletAction(scriptUrl, params)
            true
        } catch (e: Exception) {
            e.printStackTrace()
            false
        }
    }
}

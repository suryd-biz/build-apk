package com.example.data.database

import androidx.room.*
import kotlinx.coroutines.flow.Flow

@Dao
interface WalletDao {
    @Query("SELECT * FROM wallets ORDER BY urutan ASC")
    fun getAllWalletsFlow(): Flow<List<WalletEntity>>

    @Query("SELECT * FROM wallets ORDER BY urutan ASC")
    suspend fun getAllWalletsDirect(): List<WalletEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertWallet(wallet: WalletEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertWallets(wallets: List<WalletEntity>)

    @Query("DELETE FROM wallets WHERE id = :walletId")
    suspend fun deleteWalletById(walletId: String)

    @Query("DELETE FROM wallets")
    suspend fun clearWallets()

    // --- TRANSACTIONS ---
    @Query("SELECT * FROM transactions WHERE walletId = :walletId ORDER BY tanggal DESC, id DESC")
    fun getTransactionsForWalletFlow(walletId: String): Flow<List<TransactionEntity>>

    @Query("SELECT * FROM transactions WHERE walletId = :walletId ORDER BY tanggal DESC, id DESC")
    suspend fun getTransactionsForWalletDirect(walletId: String): List<TransactionEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTransaction(transaction: TransactionEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTransactions(transactions: List<TransactionEntity>)

    @Query("DELETE FROM transactions WHERE id = :txId")
    suspend fun deleteTransactionById(txId: String)

    @Query("DELETE FROM transactions WHERE walletId = :walletId")
    suspend fun deleteTransactionsByWalletId(walletId: String)

    @Query("DELETE FROM transactions")
    suspend fun clearTransactions()
}

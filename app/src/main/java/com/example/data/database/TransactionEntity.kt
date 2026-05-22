package com.example.data.database

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "transactions")
data class TransactionEntity(
    @PrimaryKey val id: String,
    val tanggal: String,
    val jenis: String, // "Masuk" or "Keluar"
    val kategori: String,
    val nominal: Double,
    val keterangan: String,
    val walletId: String
)

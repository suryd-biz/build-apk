package com.example.data.database

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "asset_snapshots")
data class AssetSnapshotEntity(
    @PrimaryKey val compositeId: String, // "$assetId|$rawTanggal"
    val assetId: String,
    val rawTanggal: String,
    val tanggal: String,
    val keterangan: String,
    val bni: Double,
    val seabank: Double,
    val bibit: Double,
    val tunai: Double,
    val flip: Double,
    val ovo: Double,
    val shopeepay: Double,
    val dana: Double,
    val jago: Double,
    val lain: Double,
    val total: Double
)

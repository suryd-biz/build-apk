package com.example.data.database

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "asset_profiles")
data class AssetProfileEntity(
    @PrimaryKey val id: String,
    val nama: String,
    val hasPin: Boolean,
    val pin: String? = null,
    val urutan: Int = 0
)

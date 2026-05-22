package com.example.data.database

import androidx.room.*
import kotlinx.coroutines.flow.Flow

@Dao
interface AssetDao {
    @Query("SELECT * FROM asset_profiles ORDER BY urutan ASC")
    fun getAllProfilesFlow(): Flow<List<AssetProfileEntity>>

    @Query("SELECT * FROM asset_profiles ORDER BY urutan ASC")
    suspend fun getAllProfilesDirect(): List<AssetProfileEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertProfile(profile: AssetProfileEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertProfiles(profiles: List<AssetProfileEntity>)

    @Query("DELETE FROM asset_profiles WHERE id = :profileId")
    suspend fun deleteProfileById(profileId: String)

    @Query("DELETE FROM asset_profiles")
    suspend fun clearProfiles()

    // --- SNAPSHOTS ---
    @Query("SELECT * FROM asset_snapshots WHERE assetId = :assetId ORDER BY rawTanggal DESC")
    fun getSnapshotsForAssetFlow(assetId: String): Flow<List<AssetSnapshotEntity>>

    @Query("SELECT * FROM asset_snapshots WHERE assetId = :assetId ORDER BY rawTanggal DESC")
    suspend fun getSnapshotsForAssetDirect(assetId: String): List<AssetSnapshotEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSnapshot(snapshot: AssetSnapshotEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSnapshots(snapshots: List<AssetSnapshotEntity>)

    @Query("DELETE FROM asset_snapshots WHERE compositeId = :compositeId")
    suspend fun deleteSnapshotByCompositeId(compositeId: String)

    @Query("DELETE FROM asset_snapshots WHERE assetId = :assetId AND rawTanggal = :rawTanggal")
    suspend fun deleteSnapshotByDate(assetId: String, rawTanggal: String)

    @Query("DELETE FROM asset_snapshots WHERE assetId = :assetId")
    suspend fun deleteSnapshotsByAssetId(assetId: String)

    @Query("DELETE FROM asset_snapshots")
    suspend fun clearSnapshots()
}

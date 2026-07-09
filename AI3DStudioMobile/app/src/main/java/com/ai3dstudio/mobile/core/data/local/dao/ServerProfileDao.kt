package com.ai3dstudio.mobile.core.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.ai3dstudio.mobile.core.data.local.entity.ServerProfileEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface ServerProfileDao {
    @Query("SELECT * FROM server_profiles WHERE isActive = 1 LIMIT 1")
    fun observeActive(): Flow<ServerProfileEntity?>

    @Query("SELECT * FROM server_profiles WHERE isActive = 1 LIMIT 1")
    suspend fun getActive(): ServerProfileEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsert(profile: ServerProfileEntity)

    @Query("UPDATE server_profiles SET isActive = 0")
    suspend fun deactivateAll()

    @Query("SELECT * FROM server_profiles ORDER BY lastDiscoveredAt DESC")
    fun observeAll(): Flow<List<ServerProfileEntity>>
}

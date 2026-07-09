package com.ai3dstudio.mobile.core.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.ai3dstudio.mobile.core.data.local.entity.AssetEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface AssetDao {
    @Query("SELECT * FROM assets ORDER BY createdAt DESC")
    fun observeAll(): Flow<List<AssetEntity>>

    @Query("SELECT * FROM assets WHERE category = :category ORDER BY createdAt DESC")
    fun observeByCategory(category: String): Flow<List<AssetEntity>>

    @Query("SELECT * FROM assets WHERE name LIKE '%' || :query || '%' ORDER BY createdAt DESC")
    fun search(query: String): Flow<List<AssetEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(asset: AssetEntity)

    @Query("DELETE FROM assets WHERE id = :id")
    suspend fun deleteById(id: String)
}

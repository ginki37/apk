package com.ai3dstudio.mobile.core.data.repository

import com.ai3dstudio.mobile.core.data.local.dao.AssetDao
import com.ai3dstudio.mobile.core.data.local.entity.AssetEntity
import com.ai3dstudio.mobile.core.domain.model.AssetCategory
import com.ai3dstudio.mobile.core.domain.model.LibraryAsset
import com.ai3dstudio.mobile.core.domain.repository.AssetRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class AssetRepositoryImpl @Inject constructor(
    private val assetDao: AssetDao
) : AssetRepository {

    override fun observeAssets(): Flow<List<LibraryAsset>> =
        assetDao.observeAll().map { list -> list.map { it.toDomain() } }

    override fun searchAssets(query: String): Flow<List<LibraryAsset>> =
        assetDao.search(query).map { list -> list.map { it.toDomain() } }

    override fun observeByCategory(category: AssetCategory): Flow<List<LibraryAsset>> =
        assetDao.observeByCategory(category.name).map { list -> list.map { it.toDomain() } }

    override suspend fun addAsset(asset: LibraryAsset) {
        assetDao.insert(
            AssetEntity(
                id = asset.id,
                projectId = asset.projectId,
                name = asset.name,
                category = asset.category.name,
                filePath = asset.filePath,
                mimeType = asset.mimeType,
                sizeBytes = asset.sizeBytes,
                createdAt = asset.createdAt,
                metadataJson = "{}"
            )
        )
    }

    override suspend fun deleteAsset(id: String) = assetDao.deleteById(id)

    private fun AssetEntity.toDomain() = LibraryAsset(
        id = id,
        projectId = projectId,
        name = name,
        category = AssetCategory.valueOf(category),
        filePath = filePath,
        mimeType = mimeType,
        sizeBytes = sizeBytes,
        createdAt = createdAt
    )
}

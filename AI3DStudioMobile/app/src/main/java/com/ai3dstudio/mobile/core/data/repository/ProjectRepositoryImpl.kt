package com.ai3dstudio.mobile.core.data.repository

import com.ai3dstudio.mobile.core.data.local.dao.ProjectDao
import com.ai3dstudio.mobile.core.data.local.entity.ProjectEntity
import com.ai3dstudio.mobile.core.domain.model.Project
import com.ai3dstudio.mobile.core.domain.model.ProjectType
import com.ai3dstudio.mobile.core.domain.repository.ProjectRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import java.util.UUID
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ProjectRepositoryImpl @Inject constructor(
    private val projectDao: ProjectDao
) : ProjectRepository {

    override fun observeProjects(): Flow<List<Project>> =
        projectDao.observeAll().map { list -> list.map { it.toDomain() } }

    override suspend fun getProject(id: String): Project? = projectDao.getById(id)?.toDomain()

    override suspend fun createProject(title: String, type: ProjectType): Project {
        val now = System.currentTimeMillis()
        val entity = ProjectEntity(
            id = UUID.randomUUID().toString(),
            title = title,
            createdAt = now,
            updatedAt = now,
            type = type.name
        )
        projectDao.upsert(entity)
        return entity.toDomain()
    }

    override suspend fun deleteProject(id: String) = projectDao.deleteById(id)

    private fun ProjectEntity.toDomain() = Project(
        id = id,
        title = title,
        createdAt = createdAt,
        updatedAt = updatedAt,
        type = ProjectType.valueOf(type)
    )
}

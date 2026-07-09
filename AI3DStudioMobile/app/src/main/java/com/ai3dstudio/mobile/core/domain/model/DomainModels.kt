package com.ai3dstudio.mobile.core.domain.model

data class Project(
    val id: String,
    val title: String,
    val createdAt: Long,
    val updatedAt: Long,
    val type: ProjectType
)

enum class ProjectType { CHAT, IMAGE, MODEL_3D, GAME }

data class ChatMessage(
    val id: String,
    val projectId: String,
    val role: ChatRole,
    val content: String,
    val attachmentPath: String? = null,
    val attachmentType: AttachmentType? = null,
    val createdAt: Long
)

enum class ChatRole { USER, ASSISTANT, SYSTEM }
enum class AttachmentType { IMAGE, MODEL_3D, GAME }

data class GeneratedImage(
    val id: String,
    val prompt: String,
    val localPath: String,
    val format: ImageFormat,
    val width: Int,
    val height: Int,
    val style: String?,
    val createdAt: Long
)

enum class ImageFormat { PNG, JPG, WEBP }

data class Generated3dModel(
    val id: String,
    val prompt: String,
    val localPath: String,
    val format: Model3dFormat,
    val detailLevel: DetailLevel,
    val polyCount: Int,
    val hasTextures: Boolean,
    val hasAnimation: Boolean,
    val createdAt: Long
)

enum class Model3dFormat { GLB, OBJ, FBX, STL, VOX, PLY, USDZ }
enum class DetailLevel { LOW, MEDIUM, HIGH, ULTRA }

data class GeneratedGame(
    val id: String,
    val prompt: String,
    val genre: GameGenre,
    val zipLocalPath: String,
    val createdAt: Long
)

enum class GameGenre { SURVIVAL, RACING, STRATEGY, PLATFORMER, OTHER }

data class LibraryAsset(
    val id: String,
    val projectId: String?,
    val name: String,
    val category: AssetCategory,
    val filePath: String,
    val mimeType: String,
    val sizeBytes: Long,
    val createdAt: Long
)

enum class AssetCategory { IMAGE, SOUND, MODEL_3D, ANIMATION, PROJECT }

data class ServerCapabilities(
    val baseUrl: String,
    val serverName: String?,
    val serverVersion: String?,
    val availableModels: List<String>,
    val chatCapable: Boolean,
    val imageCapable: Boolean,
    val model3dCapable: Boolean,
    val gameCapable: Boolean,
    val requestsPerMinute: Int?,
    val requestsPerDay: Int?
) {
    val hasAnyCapability: Boolean
        get() = chatCapable || imageCapable || model3dCapable || gameCapable
}

sealed class AiStudioResult<out T> {
    data class Success<T>(val data: T) : AiStudioResult<T>()
    data class Error(val message: String, val cause: Throwable? = null) : AiStudioResult<Nothing>()
    data object Loading : AiStudioResult<Nothing>()
}

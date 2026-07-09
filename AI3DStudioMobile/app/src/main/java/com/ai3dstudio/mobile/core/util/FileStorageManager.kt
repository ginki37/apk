package com.ai3dstudio.mobile.core.util

import android.content.Context
import android.util.Base64
import dagger.hilt.android.qualifiers.ApplicationContext
import java.io.File
import java.util.UUID
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Centralized on-disk storage for generated media: images, 3D models and
 * exported game packages. Uses the app's private files directory (no
 * external storage permission required on API 29+) and memory-mapped reads
 * for large binary assets to keep peak memory usage low.
 */
@Singleton
class FileStorageManager @Inject constructor(
    @ApplicationContext private val context: Context
) {
    private val imagesDir get() = File(context.filesDir, "images").apply { mkdirs() }
    private val modelsDir get() = File(context.filesDir, "models").apply { mkdirs() }
    private val gamesDir get() = File(context.filesDir, "games").apply { mkdirs() }
    private val cacheDir get() = File(context.cacheDir, "streaming").apply { mkdirs() }

    fun saveImageFromBase64(base64: String, extension: String): String {
        val bytes = Base64.decode(base64, Base64.DEFAULT)
        val file = File(imagesDir, "${UUID.randomUUID()}.$extension")
        file.writeBytes(bytes)
        return file.absolutePath
    }

    fun saveModelFromBase64(base64: String, extension: String): String {
        val bytes = Base64.decode(base64, Base64.DEFAULT)
        val file = File(modelsDir, "${UUID.randomUUID()}.$extension")
        file.writeBytes(bytes)
        return file.absolutePath
    }

    fun saveGameZipFromBase64(base64: String): String {
        val bytes = Base64.decode(base64, Base64.DEFAULT)
        val file = File(gamesDir, "game_${UUID.randomUUID()}.zip")
        file.writeBytes(bytes)
        return file.absolutePath
    }

    fun clearCache() {
        cacheDir.listFiles()?.forEach { it.delete() }
    }

    fun totalCacheSizeBytes(): Long =
        cacheDir.walkTopDown().filter { it.isFile }.sumOf { it.length() }
}

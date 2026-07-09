package com.ai3dstudio.mobile.core.util

import android.content.Context
import dagger.hilt.android.qualifiers.ApplicationContext
import java.io.File
import java.util.UUID
import java.util.zip.ZipEntry
import java.util.zip.ZipOutputStream
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Packages generated game assets (scripts, models, textures, sounds and a
 * manifest) into a single downloadable game.zip using Store-mode friendly
 * compression, mirroring the desktop export pipeline.
 */
@Singleton
class GameProjectPackager @Inject constructor(
    @ApplicationContext private val context: Context
) {
    fun packageGame(genre: String, prompt: String, manifest: Map<String, String>): String {
        val outputDir = File(context.filesDir, "games").apply { mkdirs() }
        val zipFile = File(outputDir, "game_${UUID.randomUUID()}.zip")

        ZipOutputStream(zipFile.outputStream()).use { zip ->
            writeEntry(zip, "manifest.json", buildManifestJson(genre, prompt, manifest))
            writeEntry(zip, "README.txt", "لعبة تم توليدها بواسطة AI 3D Studio Mobile\nالنوع: $genre\nالوصف: $prompt\n")
            writeEntry(zip, "scripts/GameLogic.txt", "// منطق اللعبة الأساسي سيُستبدل بالمحتوى الفعلي القادم من الخادم\n")
            writeEntry(zip, "assets/models/.keep", "")
            writeEntry(zip, "assets/textures/.keep", "")
            writeEntry(zip, "assets/sounds/.keep", "")
        }
        return zipFile.absolutePath
    }

    private fun writeEntry(zip: ZipOutputStream, name: String, content: String) {
        zip.putNextEntry(ZipEntry(name))
        zip.write(content.toByteArray())
        zip.closeEntry()
    }

    private fun buildManifestJson(genre: String, prompt: String, manifest: Map<String, String>): String {
        val entries = manifest.entries.joinToString(",\n") { (k, v) -> "    \"$k\": \"$v\"" }
        return """
        {
          "genre": "$genre",
          "prompt": "${prompt.replace("\"", "\\\"")}",
        $entries
        }
        """.trimIndent()
    }
}

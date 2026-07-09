package com.ai3dstudio.mobile.feature.model3d.ui

import android.view.SurfaceView
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.viewinterop.AndroidView
import com.ai3dstudio.mobile.feature.model3d.render.FilamentModelViewer
import java.io.File
import java.nio.ByteBuffer

/**
 * Embeds the native Filament renderer inside Compose to display generated
 * GLB models with PBR/IBL/real-time shadows/bloom/tone mapping, replacing
 * a web-based Three.js viewer entirely.
 */
@Composable
fun Model3dViewerScreen(modelFilePath: String?) {
    var viewer: FilamentModelViewer? = null

    AndroidView(
        modifier = Modifier.fillMaxSize(),
        factory = { context ->
            SurfaceView(context).also { surfaceView ->
                viewer = FilamentModelViewer(context, surfaceView)
            }
        },
        update = { surfaceView ->
            val currentViewer = viewer ?: FilamentModelViewer(surfaceView.context, surfaceView).also { viewer = it }
            modelFilePath?.let { path ->
                val file = File(path)
                if (file.exists()) {
                    currentViewer.loadGlb(ByteBuffer.wrap(file.readBytes()))
                }
            }
        }
    )

    DisposableEffect(Unit) {
        onDispose { viewer?.destroy() }
    }
}

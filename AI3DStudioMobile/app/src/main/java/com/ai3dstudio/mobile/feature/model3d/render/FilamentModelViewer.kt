package com.ai3dstudio.mobile.feature.model3d.render

import android.content.Context
import android.view.Choreographer
import android.view.SurfaceView
import com.google.android.filament.Colors
import com.google.android.filament.Engine
import com.google.android.filament.EntityManager
import com.google.android.filament.IndirectLight
import com.google.android.filament.LightManager
import com.google.android.filament.Renderer
import com.google.android.filament.Scene
import com.google.android.filament.Skybox
import com.google.android.filament.View
import com.google.android.filament.Viewport
import com.google.android.filament.android.DisplayHelper
import com.google.android.filament.android.UiHelper
import com.google.android.filament.gltfio.AssetLoader
import com.google.android.filament.gltfio.ResourceLoader
import com.google.android.filament.gltfio.UbershaderProvider
import com.google.android.filament.utils.Manipulator
import java.nio.ByteBuffer

/**
 * A production-grade Filament based glTF/GLB viewer offering PBR, IBL,
 * real-time shadows, tone mapping and anti-aliasing. Wrapped by a Compose
 * AndroidView in Model3dViewerScreen. Replaces the web Three.js viewer with
 * native, GPU-accelerated rendering.
 */
class FilamentModelViewer(
    private val context: Context,
    private val surfaceView: SurfaceView
) {
    val engine: Engine = Engine.create()
    val renderer: Renderer = engine.createRenderer()
    val scene: Scene = engine.createScene()
    val view: View = engine.createView()
    private val camera = engine.createCamera(EntityManager.get().create())

    private val uiHelper = UiHelper(UiHelper.ContextErrorPolicy.DONT_CHECK)
    private val displayHelper = DisplayHelper(context)
    private val choreographer = Choreographer.getInstance()

    private val assetLoader: AssetLoader
    private val resourceLoader: ResourceLoader
    private var swapChain: com.google.android.filament.SwapChain? = null
    private var currentAsset: com.google.android.filament.gltfio.FilamentAsset? = null

    private val manipulator: Manipulator = Manipulator.Builder()
        .targetPosition(0f, 0f, 0f)
        .viewport(1, 1)
        .build(Manipulator.Mode.ORBIT)

    init {
        view.scene = scene
        view.camera = camera
        view.isPostProcessingEnabled = true
        view.blendMode = View.BlendMode.OPAQUE
        view.antiAliasing = View.AntiAliasing.FXAA
        view.dynamicResolutionOptions = view.dynamicResolutionOptions.apply { enabled = true }
        view.ambientOcclusionOptions = view.ambientOcclusionOptions.apply { enabled = true }
        view.bloomOptions = view.bloomOptions.apply { enabled = true; strength = 0.35f }
        view.toneMapper = View.ToneMapper.ACES

        scene.skybox = Skybox.Builder().color(0.02f, 0.02f, 0.03f, 1.0f).build(engine)
        setupIndirectLight()
        setupSunLight()

        assetLoader = AssetLoader(engine, UbershaderProvider(engine), EntityManager.get())
        resourceLoader = ResourceLoader(engine)

        uiHelper.renderCallback = object : UiHelper.RendererCallback {
            override fun onNativeWindowChanged(surface: android.view.Surface?) {
                swapChain?.let { engine.destroySwapChain(it) }
                swapChain = surface?.let { engine.createSwapChain(it) }
            }

            override fun onDetachedFromSurface() {
                swapChain?.let { engine.destroySwapChain(it); swapChain = null }
            }

            override fun onResized(width: Int, height: Int) {
                view.viewport = Viewport(0, 0, width, height)
                camera.setProjection(45.0, width.toDouble() / height, 0.1, 100.0, com.google.android.filament.Camera.Fov.VERTICAL)
                manipulator.setViewport(width, height)
                displayHelper.attach(renderer, surfaceView.display)
            }
        }
        uiHelper.attachTo(surfaceView)

        choreographer.postFrameCallback(frameCallback)
    }

    private fun setupIndirectLight() {
        val ibl = IndirectLight.Builder()
            .intensity(22000.0f)
            .build(engine)
        scene.indirectLight = ibl
    }

    private fun setupSunLight() {
        val sunEntity = EntityManager.get().create()
        LightManager.Builder(LightManager.Type.SUN)
            .color(Colors.cct(6500.0f).let { floatArrayOf(it[0], it[1], it[2]) })
            .intensity(110_000.0f)
            .direction(-0.4f, -1.0f, -0.3f)
            .castShadows(true)
            .build(engine, sunEntity)
        scene.addEntity(sunEntity)
    }

    fun loadGlb(buffer: ByteBuffer) {
        currentAsset?.let { assetLoader.destroyAsset(it) }
        val asset = assetLoader.createAsset(buffer) ?: return
        resourceLoader.loadResources(asset)
        asset.releaseSourceData()
        scene.addEntities(asset.entities)
        currentAsset = asset
    }

    private val frameCallback = object : Choreographer.FrameCallback {
        override fun doFrame(frameTimeNanos: Long) {
            choreographer.postFrameCallback(this)
            if (uiHelper.isReadyToRender) {
                if (renderer.beginFrame(swapChain ?: return, frameTimeNanos)) {
                    renderer.render(view)
                    renderer.endFrame()
                }
            }
        }
    }

    fun destroy() {
        choreographer.removeFrameCallback(frameCallback)
        currentAsset?.let { assetLoader.destroyAsset(it) }
        resourceLoader.destroy()
        assetLoader.destroy()
        engine.destroyView(view)
        engine.destroyScene(scene)
        engine.destroyRenderer(renderer)
        swapChain?.let { engine.destroySwapChain(it) }
        engine.destroy()
    }
}

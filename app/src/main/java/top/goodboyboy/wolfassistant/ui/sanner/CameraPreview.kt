package top.goodboyboy.wolfassistant.ui.sanner

import androidx.camera.core.CameraSelector
import androidx.camera.core.ImageAnalysis
import androidx.camera.core.Preview
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.PreviewView
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.content.ContextCompat
import androidx.lifecycle.compose.LocalLifecycleOwner
import dagger.hilt.android.EntryPointAccessors
import top.goodboyboy.wolfassistant.di.LoggerEntryPoint
import java.util.concurrent.Executors

@Composable
fun CameraPreview(onResult: (String) -> Unit) {
    val context = LocalContext.current
    val logger =
        remember {
            EntryPointAccessors
                .fromApplication(
                    context.applicationContext,
                    LoggerEntryPoint::class.java,
                ).logger()
        }
    val lifecycleOwner = LocalLifecycleOwner.current
    val cameraExecutor = remember { Executors.newSingleThreadExecutor() }
    var cameraProvider by remember { mutableStateOf<ProcessCameraProvider?>(null) }

    DisposableEffect(Unit) {
        onDispose {
            cameraProvider?.unbindAll()
            cameraExecutor.shutdown()
        }
    }

    AndroidView(
        factory = { ctx ->
            val previewView =
                PreviewView(ctx).apply {
                    implementationMode = PreviewView.ImplementationMode.COMPATIBLE
                }
            val cameraProviderFuture = ProcessCameraProvider.getInstance(ctx)
            val mainExecutor = ContextCompat.getMainExecutor(ctx)
            cameraProviderFuture.addListener({
                val provider = cameraProviderFuture.get()
                cameraProvider = provider
                val preview =
                    Preview.Builder().build().also {
                        it.surfaceProvider = previewView.surfaceProvider
                    }
                val analyzer =
                    ImageAnalysis.Builder().build().also {
                        it.setAnalyzer(
                            cameraExecutor,
                            BarcodeAnalyzer(
                                onBarcodeScanned = { result ->
                                    mainExecutor.execute {
                                        onResult(result)
                                    }
                                },
                                logger = logger,
                            ),
                        )
                    }
                val cameraSelector = CameraSelector.DEFAULT_BACK_CAMERA
                try {
                    provider.unbindAll()
                    provider.bindToLifecycle(
                        lifecycleOwner,
                        cameraSelector,
                        preview,
                        analyzer,
                    )
                } catch (e: Exception) {
                    logger.tag("CameraPreview").e(e, "绑定相机生命周期失败")
                }
            }, mainExecutor)
            previewView
        },
        modifier = Modifier.fillMaxSize(),
    )
}

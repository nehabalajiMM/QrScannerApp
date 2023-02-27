package com.example.qrscannerapp.ui.components

import android.annotation.SuppressLint
import android.graphics.Color
import android.util.Size
import android.view.ViewGroup
import android.widget.LinearLayout
import androidx.camera.core.* // ktlint-disable no-wildcard-imports
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.PreviewView
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.content.ContextCompat
import androidx.lifecycle.LifecycleOwner
import com.google.common.util.concurrent.ListenableFuture
import java.util.concurrent.ExecutorService

@ExperimentalGetImage
@Composable
fun CameraPreview(
    cameraProviderFuture: ListenableFuture<ProcessCameraProvider>,
    executor: ExecutorService,
    analyzer: BarcodeScannerAnalyzer
) {
    val lifecycleOwner = LocalLifecycleOwner.current
    AndroidView(
        factory = { context ->
            PreviewView(context).apply {
                setBackgroundColor(Color.BLUE)
                layoutParams = LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT)
                scaleType = PreviewView.ScaleType.FILL_START
                implementationMode = PreviewView.ImplementationMode.COMPATIBLE
                post {
                    cameraProviderFuture.addListener({
                        val cameraProvider = cameraProviderFuture.get()
                        bindPreview(
                            cameraProvider,
                            this,
                            lifecycleOwner,
                            executor,
                            analyzer
                        )
                    }, ContextCompat.getMainExecutor(context))
                }
            }
        }
    )
}

@SuppressLint("UnsafeExperimentalUsageError")
private fun bindPreview(
    cameraProvider: ProcessCameraProvider,
    previewView: PreviewView,
    lifecycleOwner: LifecycleOwner,
    executor: ExecutorService,
    analyzer: BarcodeScannerAnalyzer
) {
    val preview: Preview = Preview.Builder()
        .build()
    val cameraSelector: CameraSelector = CameraSelector.Builder()
        .requireLensFacing(CameraSelector.LENS_FACING_BACK)
        .build()
    preview.setSurfaceProvider(previewView.surfaceProvider)

    val imageAnalysis = ImageAnalysis.Builder()
        .setTargetResolution(Size(1280, 720))
        .setBackpressureStrategy(ImageAnalysis.STRATEGY_KEEP_ONLY_LATEST)
        .build()
    imageAnalysis.setAnalyzer(executor, analyzer)

    cameraProvider.bindToLifecycle(
        lifecycleOwner,
        cameraSelector,
        imageAnalysis,
        preview
    )
}

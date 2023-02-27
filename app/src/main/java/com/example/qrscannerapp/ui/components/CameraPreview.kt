package com.example.qrscannerapp.ui.components

import android.annotation.SuppressLint
import android.graphics.Color
import android.util.Log
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
import com.google.mlkit.vision.barcode.BarcodeScanner
import com.google.mlkit.vision.common.InputImage
import java.util.concurrent.ExecutorService

@androidx.camera.core.ExperimentalGetImage
@Composable
fun CameraPreview(
    cameraProviderFuture: ListenableFuture<ProcessCameraProvider>,
    executor: ExecutorService,
    analyzer: BarcodeScannerAnalyzer
//    barcodeScanner: BarcodeScanner,
//    onQrCodeScanned: (String) -> Unit
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

// @androidx.camera.core.ExperimentalGetImage
// fun bindPreview(
//    cameraProvider: ProcessCameraProvider,
//    lifecycleOwner: LifecycleOwner,
//    previewView: PreviewView,
//    barcodeScanner: BarcodeScanner,
//    onQrCodeScanned: (String) -> Unit,
//    executor: Executor
// ) {
//    val preview: Preview = Preview.Builder()
//        .setTargetAspectRatio(AspectRatio.RATIO_16_9)
//        .build()
//
//    val cameraSelector: CameraSelector = CameraSelector.Builder()
//        .requireLensFacing(CameraSelector.LENS_FACING_BACK)
//        .build()
//
//    preview.setSurfaceProvider(previewView.surfaceProvider)
//
// //    val imageAnalyzer = ImageAnalysis.Builder()
// //        .setBackpressureStrategy(ImageAnalysis.STRATEGY_KEEP_ONLY_LATEST)
// //        .build()
// //
// //    val barcodeScannerAnalyzer = BarcodeScannerAnalyzer(barcodeScanner, onQrCodeScanned)
//
//    cameraProvider.unbindAll()
//    cameraProvider.bindToLifecycle(
//        lifecycleOwner,
//        cameraSelector,
//        preview
//    )
//    val analysisUseCase = ImageAnalysis.Builder()
//        .setTargetAspectRatio(AspectRatio.RATIO_DEFAULT)
//        .setTargetRotation(previewView.display.rotation)
//        .build()
//    analysisUseCase.setAnalyzer(
//        executor,
//        ImageAnalysis.Analyzer { imageProxy ->
//            processImageProxy(barcodeScanner, imageProxy)
//        }
//    )
// }

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

@androidx.camera.core.ExperimentalGetImage
private fun processImageProxy(
    barcodeScanner: BarcodeScanner,
    imageProxy: ImageProxy
) {
    val inputImage = InputImage.fromMediaImage(imageProxy.image!!, imageProxy.imageInfo.rotationDegrees)

    barcodeScanner.process(inputImage)
        .addOnSuccessListener { barcodes ->
            barcodes.forEach {
                Log.d("QR", it.rawValue.toString())
            }
        }
        .addOnFailureListener {
            Log.e("QR", it.message.toString())
        }.addOnCompleteListener {
            imageProxy.close()
        }
}

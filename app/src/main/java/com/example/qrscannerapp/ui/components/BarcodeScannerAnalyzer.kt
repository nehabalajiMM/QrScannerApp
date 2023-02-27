package com.example.qrscannerapp.ui.components

import androidx.camera.core.ImageAnalysis
import androidx.camera.core.ImageProxy
import com.google.mlkit.vision.barcode.BarcodeScanning
import com.google.mlkit.vision.barcode.common.Barcode
import com.google.mlkit.vision.common.InputImage

@androidx.camera.core.ExperimentalGetImage
class BarcodeScannerAnalyzer(
    private val onQrCodeScanned: (String) -> Unit
) : ImageAnalysis.Analyzer {

    override fun analyze(imageProxy: ImageProxy) {
        scanBarcode(imageProxy)
    }

    private fun scanBarcode(imageProxy: ImageProxy) {
        imageProxy.image?.let { image ->
            val inputImage = InputImage.fromMediaImage(image, imageProxy.imageInfo.rotationDegrees)
            val scanner = BarcodeScanning.getClient()
            scanner.process(inputImage)
                .addOnCompleteListener {
                    imageProxy.close()
                    if (it.isSuccessful) {
                        val barcodes = it.result as List<Barcode>
                        for (barcode in barcodes) {
                            onQrCodeScanned(barcode.displayValue.toString())
                        }
                    } else {
                        it.exception?.printStackTrace()
                    }
                }
        }
    }
}

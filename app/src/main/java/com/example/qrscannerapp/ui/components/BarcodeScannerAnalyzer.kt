package com.example.qrscannerapp.ui.components

import android.annotation.SuppressLint
import android.util.Log
import androidx.camera.core.ImageAnalysis
import androidx.camera.core.ImageProxy
import com.google.mlkit.vision.barcode.BarcodeScanning
import com.google.mlkit.vision.barcode.common.Barcode
import com.google.mlkit.vision.common.InputImage

// @androidx.camera.core.ExperimentalGetImage
// class BarcodeScannerAnalyzer(
//    private val barcodeScanner: BarcodeScanner,
//    private val onQrCodeScanned: (String) -> Unit
// ) : ImageAnalysis.Analyzer {
//    override fun analyze(image: ImageProxy) {
//        val mediaImage = image.image ?: return
//        val imageRotation = image.imageInfo.rotationDegrees
//
//        val inputImage = InputImage.fromMediaImage(mediaImage, imageRotation)
//
//        barcodeScanner.process(inputImage)
//            .addOnSuccessListener { barcodes ->
//                for (barcode in barcodes) {
//                    if (barcode.valueType == Barcode.FORMAT_QR_CODE) {
//                        onQrCodeScanned(barcode.rawValue.toString())
//                    }
//                }
//            }
//            .addOnFailureListener { e ->
//                e.printStackTrace()
//            }
//            .addOnCompleteListener {
//                image.close()
//            }
//    }
// }

@androidx.camera.core.ExperimentalGetImage
class BarcodeScannerAnalyzer : ImageAnalysis.Analyzer {

    override fun analyze(imageProxy: ImageProxy) {
        scanBarcode(imageProxy)
    }

    @SuppressLint("UnsafeExperimentalUsageError")
    private fun scanBarcode(imageProxy: ImageProxy) {
        imageProxy.image?.let { image ->
            val inputImage = InputImage.fromMediaImage(image, imageProxy.imageInfo.rotationDegrees)
            val scanner = BarcodeScanning.getClient()
            scanner.process(inputImage)
                .addOnCompleteListener {
                    imageProxy.close()
                    if (it.isSuccessful) {
                        readBarcodeData(it.result as List<Barcode>)
                    } else {
                        it.exception?.printStackTrace()
                    }
                }
        }
    }

    private fun readBarcodeData(barcodes: List<Barcode>) {
        for (barcode in barcodes) {
            Log.v("QRCODE", barcode.displayValue.toString())
        }
    }
}

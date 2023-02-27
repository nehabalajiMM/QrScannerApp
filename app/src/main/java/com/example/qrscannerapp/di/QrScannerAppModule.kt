package com.example.qrscannerapp.di

import com.google.mlkit.vision.barcode.BarcodeScanner
import com.google.mlkit.vision.barcode.BarcodeScanning
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent

@Module
@InstallIn(SingletonComponent::class)
object QrScannerAppModule {

    @Provides
    fun provideBarcodeScanner(): BarcodeScanner {
        return BarcodeScanning.getClient()
    }
}

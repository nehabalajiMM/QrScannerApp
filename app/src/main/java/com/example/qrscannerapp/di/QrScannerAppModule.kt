package com.example.qrscannerapp.di

import android.content.Context
import androidx.camera.lifecycle.ProcessCameraProvider
import com.google.common.util.concurrent.ListenableFuture
import com.google.mlkit.vision.barcode.BarcodeScanner
import com.google.mlkit.vision.barcode.BarcodeScanning
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object QrScannerAppModule {

    @Provides
    fun provideBarcodeScanner(): BarcodeScanner {
        return BarcodeScanning.getClient()
    }

    @Singleton
    @Provides
    fun provideCameraProviderFuture(
        @ApplicationContext context: Context
    ): ListenableFuture<ProcessCameraProvider> {
        return ProcessCameraProvider.getInstance(context)
    }

    @Singleton
    @Provides
    fun provideCameraExecutor(): ExecutorService {
        return Executors.newSingleThreadExecutor()
    }
}

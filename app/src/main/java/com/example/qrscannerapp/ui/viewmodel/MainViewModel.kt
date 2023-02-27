package com.example.qrscannerapp.ui.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.google.mlkit.vision.barcode.BarcodeScanner
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@HiltViewModel
class MainViewModel @Inject constructor(
    val barcodeScanner: BarcodeScanner
) : ViewModel() {
    private val _qrCode = MutableLiveData<String>()
    val qrCode: LiveData<String> = _qrCode

    fun onQrCodeScanned(qrCode: String) {
        _qrCode.postValue(qrCode)
    }

    fun clearQrCode() {
        _qrCode.postValue(null)
    }
}

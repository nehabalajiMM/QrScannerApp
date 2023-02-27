package com.example.qrscannerapp

import android.Manifest
import android.content.pm.PackageManager
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material.* // ktlint-disable no-wildcard-imports
import androidx.compose.runtime.* // ktlint-disable no-wildcard-imports
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.core.content.ContextCompat
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import com.example.qrscannerapp.ui.components.AlertDialogDisplay
import com.example.qrscannerapp.ui.components.BarcodeScannerAnalyzer
import com.example.qrscannerapp.ui.components.CameraPreview
import com.example.qrscannerapp.ui.theme.QrScannerAppTheme
import com.google.common.util.concurrent.ListenableFuture
import dagger.hilt.android.AndroidEntryPoint
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors

@androidx.camera.core.ExperimentalGetImage
@AndroidEntryPoint
class MainActivity : ComponentActivity() {
//    private lateinit var mFirebaseAnalytics: FirebaseAnalytics
//    private val viewModel: MainViewModel by viewModels()

    private lateinit var cameraProviderFuture: ListenableFuture<ProcessCameraProvider>
    private lateinit var cameraExecutor: ExecutorService
    private lateinit var analyzer: BarcodeScannerAnalyzer

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
//        mFirebaseAnalytics = FirebaseAnalytics.getInstance(this)
//
//        val cameraProviderFuture = ProcessCameraProvider.getInstance(this)

        cameraExecutor = Executors.newSingleThreadExecutor()
        cameraProviderFuture = ProcessCameraProvider.getInstance(this)
        analyzer = BarcodeScannerAnalyzer()

        setContent {
            QrScannerAppTheme {
                // A surface container using the 'background' color from the theme
//                val qrCode = viewModel.qrCode.observeAsState()
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colors.background
                ) {
                    var isPermissionGranted by remember { mutableStateOf<Boolean?>(null) }
//                    val executor = remember { ContextCompat.getMainExecutor(this) }
                    val launcher =
                        rememberLauncherForActivityResult(ActivityResultContracts.RequestPermission()) { isGranted ->
                            isPermissionGranted = isGranted
                        }
                    val lifecycleOwner = LocalLifecycleOwner.current
                    DisposableEffect(LocalLifecycleOwner.current) {
                        val observer = LifecycleEventObserver { _, event ->
                            if (event == Lifecycle.Event.ON_START) {
                                if (isPermissionGranted == null) {
                                    return@LifecycleEventObserver
                                }
                                isPermissionGranted = ContextCompat.checkSelfPermission(this@MainActivity, Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED
                            }
                        }
                        lifecycleOwner.lifecycle.addObserver(observer)
                        onDispose {
                            lifecycleOwner.lifecycle.removeObserver(observer)
                        }
                    }
                    when (isPermissionGranted) {
                        true -> {
                            CameraPreview(cameraProviderFuture, cameraExecutor, analyzer)
                        }
                        false -> AlertDialogDisplay(this)
                        null -> Button(onClick = { launcher.launch(Manifest.permission.CAMERA) }) {
                            Text(text = "Start!")
                        }
                    }
                }
//                Log.v("QR", qrCode.value.toString())
//                if (qrCode.value != null) {
//                    AlertDialog(
//                        onDismissRequest = { viewModel.clearQrCode() },
//                        text = {
//                            Text(qrCode.value.toString())
//                        },
//                        confirmButton = {
//                            Button(onClick = { viewModel.clearQrCode() }) {
//                                Text("OK")
//                            }
//                        }
//                    )
//                }
            }
        }
    }
}

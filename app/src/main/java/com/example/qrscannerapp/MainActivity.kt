package com.example.qrscannerapp

import android.Manifest
import android.content.pm.PackageManager
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import com.example.qrscannerapp.ui.components.AlertDialogDisplay
import com.example.qrscannerapp.ui.components.BarcodeScannerAnalyzer
import com.example.qrscannerapp.ui.components.CameraPreview
import com.example.qrscannerapp.ui.theme.QrScannerAppTheme
import com.example.qrscannerapp.ui.theme.Shapes
import com.google.common.util.concurrent.ListenableFuture
import com.google.firebase.analytics.FirebaseAnalytics
import dagger.hilt.android.AndroidEntryPoint
import java.util.concurrent.ExecutorService
import javax.inject.Inject

@androidx.camera.core.ExperimentalGetImage
@AndroidEntryPoint
class MainActivity : ComponentActivity() {

    private lateinit var mFirebaseAnalytics: FirebaseAnalytics

    @Inject
    lateinit var cameraProviderFuture: ListenableFuture<ProcessCameraProvider>

    @Inject
    lateinit var cameraExecutor: ExecutorService

    private lateinit var analyzer: BarcodeScannerAnalyzer

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        mFirebaseAnalytics = FirebaseAnalytics.getInstance(this)

        setContent {
            QrScannerAppTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colors.background
                ) {
                    var isPermissionGranted by remember { mutableStateOf<Boolean?>(null) }
                    val launcher =
                        rememberLauncherForActivityResult(ActivityResultContracts.RequestPermission()) { isGranted ->
                            isPermissionGranted = isGranted
                        }
                    var code by remember {
                        mutableStateOf("")
                    }
                    analyzer = BarcodeScannerAnalyzer {
                        code = it
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
                            Box(
                                modifier = Modifier.fillMaxSize()
                            ) {
                                CameraPreview(cameraProviderFuture, cameraExecutor, analyzer)
                                Text(
                                    text = code,
                                    textAlign = TextAlign.Center,
                                    fontSize = 20.sp,
                                    fontWeight = FontWeight.Bold,
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(32.dp)
                                        .align(Alignment.BottomCenter)
                                        .background(Color.White, Shapes.medium)
                                )
                            }
                        }
                        false -> AlertDialogDisplay(this)
                        null -> Button(onClick = { launcher.launch(Manifest.permission.CAMERA) }) {
                            Text(text = "Start!")
                        }
                    }
                }
            }
        }
    }
}

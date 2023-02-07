package com.example.qrscannerapp

import android.Manifest
import android.graphics.Color
import android.os.Bundle
import android.view.ViewGroup.LayoutParams.MATCH_PARENT
import android.widget.LinearLayout
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.camera.core.AspectRatio
import androidx.camera.core.CameraSelector
import androidx.camera.core.Preview
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.PreviewView
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material.Button
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Surface
import androidx.compose.material.Text
import androidx.compose.runtime.* // ktlint-disable no-wildcard-imports
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.content.ContextCompat
import androidx.lifecycle.LifecycleOwner
import com.example.qrscannerapp.ui.theme.QrScannerAppTheme
import com.google.common.util.concurrent.ListenableFuture

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val cameraProviderFuture = ProcessCameraProvider.getInstance(this)
        setContent {
            QrScannerAppTheme {
                // A surface container using the 'background' color from the theme
                Surface(modifier = Modifier.fillMaxSize(), color = MaterialTheme.colors.background) {
                    var isPermissionGranted by remember { mutableStateOf<Boolean?>(null) }
                    val launcher = rememberLauncherForActivityResult(ActivityResultContracts.RequestPermission()) { isGranted ->
                        isPermissionGranted = isGranted
                    }
                    when (isPermissionGranted) {
                        true -> CameraPreview(cameraProviderFuture)
                        false -> Text("permission pls")
                        null -> Button(onClick = { launcher.launch(Manifest.permission.CAMERA) }) {
                            Text(text = "Start!")
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun CameraPreview(cameraProviderFuture: ListenableFuture<ProcessCameraProvider>) {
    val lifecycleOwner = LocalLifecycleOwner.current
    AndroidView(
        factory = { context ->
            PreviewView(context).apply {
                setBackgroundColor(Color.BLUE)
                layoutParams = LinearLayout.LayoutParams(MATCH_PARENT, MATCH_PARENT)
                scaleType = PreviewView.ScaleType.FILL_START
                implementationMode = PreviewView.ImplementationMode.COMPATIBLE
                post {
                    cameraProviderFuture.addListener({
                        val cameraProvider = cameraProviderFuture.get()
                        bindPreview(
                            cameraProvider,
                            lifecycleOwner,
                            this
                        )
                    }, ContextCompat.getMainExecutor(context))
                }
            }
        }
    )
}

fun bindPreview(
    cameraProvider: ProcessCameraProvider,
    lifecycleOwner: LifecycleOwner,
    previewView: PreviewView
) {
    val preview: Preview = Preview.Builder()
        .setTargetAspectRatio(AspectRatio.RATIO_16_9)
        .build()

    val cameraSelector: CameraSelector = CameraSelector.Builder()
        .requireLensFacing(CameraSelector.LENS_FACING_BACK)
        .build()

    preview.setSurfaceProvider(previewView.surfaceProvider)

    cameraProvider.bindToLifecycle(lifecycleOwner, cameraSelector, preview)
}

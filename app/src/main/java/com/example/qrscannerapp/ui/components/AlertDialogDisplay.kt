package com.example.qrscannerapp.ui.components

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.provider.Settings
import androidx.compose.material.AlertDialog
import androidx.compose.material.Button
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.core.content.ContextCompat

@Composable
fun AlertDialogDisplay(context: Context) {
    AlertDialog(
        onDismissRequest = {
        },
        title = {
            Text(text = "Camera Permission Required")
        },
        text = {
            Text("Camera permission is required to use the app. Please give permission.")
        },
        confirmButton = {
            Button(
                onClick = {
                    val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS)
                    val uri = Uri.fromParts("package", context.packageName, null)
                    intent.data = uri
                    ContextCompat.startActivity(context, intent, null)
                }
            ) {
                Text("Give Permission")
            }
        }
    )
}

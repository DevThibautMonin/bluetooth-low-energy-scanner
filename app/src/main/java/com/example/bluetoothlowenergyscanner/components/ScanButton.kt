package com.example.bluetoothlowenergyscanner.components

import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable

@Composable
fun ScanButton(isScanning: Boolean, onClick: () -> Unit) {
    Button(onClick = onClick) {
        Text(if (isScanning) "Stop Scan" else "Start Scan")
    }
}

package com.example.bluetoothlowenergyscanner.components

import android.bluetooth.le.ScanResult
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

@Composable
fun DeviceList(devices: List<ScanResult>, onDeviceClicked: (ScanResult) -> Unit) {
    LazyColumn(
        modifier = Modifier
            .fillMaxWidth()
            .padding(20.dp),
        horizontalAlignment = Alignment.Start
    ) {
        items(devices.size) { device ->
            DeviceItem(scanResult = devices[device], onDeviceClicked = onDeviceClicked)
        }
    }
}

package com.example.bluetoothlowenergyscanner.components

import android.bluetooth.le.ScanResult
import android.util.SparseArray
import androidx.compose.foundation.Image
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import com.example.bluetoothlowenergyscanner.utils.Utils

private fun ByteArray.toHexString(): String =
    joinToString(separator = " ") { String.format("%02X", it) }

private fun extractManufacturerData(data: SparseArray<ByteArray>?): Pair<Int, String> {
    var manufacturerId = -1
    var manufacturerData = ""

    data?.let {
        for (i in 0 until it.size()) {
            manufacturerId = it.keyAt(i)
            manufacturerData = it.valueAt(i).toHexString()
        }
    }

    return Pair(manufacturerId, manufacturerData)
}

@Composable
fun DeviceItem(scanResult: ScanResult, onDeviceClicked: (ScanResult) -> Unit) {
    val manufacturerSpecificData = scanResult.scanRecord?.manufacturerSpecificData
    val (manufacturerId, manufacturerData) = extractManufacturerData(manufacturerSpecificData)

    Row(
        modifier = Modifier
            .padding(5.dp)
            .clickable(onClick = { onDeviceClicked(scanResult) })
            .border(1.dp, Color.Black, RoundedCornerShape(5.dp))
            .padding(20.dp)
            .fillMaxWidth()
    ) {
        Column {
            DisplayLogoBasedOnManufacturerId(manufacturerId = manufacturerId)
            Text(text = "Name : ${scanResult.device.name ?: "Unnamed"}")
            Text("Mac address : ${scanResult.device.address}")
            Text("UUID : ${scanResult.scanRecord?.serviceUuids?.joinToString() ?: "Unavailable"}")
            if (manufacturerId != -1) {
                Text("Manufacturer ID : $manufacturerId")
                Text("Manufacturer Data : $manufacturerData")
            }
        }
    }
}

@Composable
fun DisplayLogoBasedOnManufacturerId(manufacturerId: Int) {
    val logo = Utils.getLogoByManufacturerId(manufacturerId)
    if (logo != null) {
        Image(
            painter = painterResource(id = logo),
            contentDescription = null,
            modifier = Modifier.size(25.dp, 25.dp)
        )
    } else {
        Text("Logo non disponible")
    }
}

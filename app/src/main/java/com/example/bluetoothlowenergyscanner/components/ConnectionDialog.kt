package com.example.bluetoothlowenergyscanner.components

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import android.bluetooth.BluetoothGattService

@Composable
fun ConnectionDialog(
    servicesList: List<BluetoothGattService>,
    onDismiss: () -> Unit
) {
    AlertDialog(
        modifier = Modifier.fillMaxWidth(),
        onDismissRequest = onDismiss,
        title = { Text("Device services") },
        text = {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center,
                modifier = Modifier
                    .verticalScroll(rememberScrollState())
                    .fillMaxWidth()
                    .height(400.dp)
            ) {
                if (servicesList.isEmpty()) {
                    CircularProgressIndicator(modifier = Modifier.size(50.dp))
                    Spacer(modifier = Modifier.height(10.dp))
                    Text("Connecting", textAlign = TextAlign.Center)
                } else {
                    Column(
                        modifier = Modifier
                            .verticalScroll(rememberScrollState())
                    ) {
                        servicesList.forEach { service ->
                            Text(
                                "\nService : ${service.uuid} \n",
                                style = TextStyle(
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 16.sp
                                )
                            )

                            val characteristicsTable = service.characteristics.joinToString(
                                separator = "\n",
                            ) { it.uuid.toString() }
                            Text(
                                "Characteristics :",
                                style = TextStyle(
                                    fontWeight = FontWeight.SemiBold,
                                    fontSize = 14.sp
                                )
                            )
                            Text(characteristicsTable, style = TextStyle(fontSize = 12.sp))
                        }
                    }
                }
            }
        },
        confirmButton = {}
    )
}

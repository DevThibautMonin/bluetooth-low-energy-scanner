package com.example.bluetoothlowenergyscanner

import android.Manifest
import android.app.Activity
import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothGatt
import android.bluetooth.BluetoothGattCallback
import android.bluetooth.BluetoothGattService
import android.bluetooth.BluetoothManager
import android.bluetooth.BluetoothProfile
import android.bluetooth.le.ScanCallback
import android.bluetooth.le.ScanFilter
import android.bluetooth.le.ScanResult
import android.bluetooth.le.ScanSettings
import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.*
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.core.app.ActivityCompat
import com.example.bluetoothlowenergyscanner.components.ConnectionDialog
import com.example.bluetoothlowenergyscanner.components.DeviceList
import com.example.bluetoothlowenergyscanner.components.ScanButton
import com.example.bluetoothlowenergyscanner.utils.PermissionsUtils

class MainActivity : ComponentActivity() {

    private var servicesList by mutableStateOf<List<BluetoothGattService>>(emptyList())
    private var isDialogVisible by mutableStateOf(false)
    private var bluetoothGatt: BluetoothGatt? = null
    private val devicesFound = mutableStateListOf<ScanResult>()
    private var isScanning by mutableStateOf(false)
    private val scanFilter = ScanFilter.Builder().build()
    private val filters = ArrayList<ScanFilter>().apply { add(scanFilter) }
    private val scanSettings =
        ScanSettings.Builder().setScanMode(ScanSettings.SCAN_MODE_LOW_LATENCY).build()

    private val bleScanner by lazy {
        bluetoothAdapter.bluetoothLeScanner
    }

    private val gattCallback = object : BluetoothGattCallback() {
        override fun onConnectionStateChange(gatt: BluetoothGatt, status: Int, newState: Int) {
            val deviceAddress = gatt.device.address

            if (status == BluetoothGatt.GATT_SUCCESS) {
                if (newState == BluetoothProfile.STATE_CONNECTED) {
                    Log.w(
                        Constants.TAG_BLUETOOTH_GATT_CALLBACK,
                        "Successfully connected to $deviceAddress"
                    )
                    showToast("Connected to $deviceAddress")
                    bluetoothGatt = gatt
                    gatt.discoverServices()
                } else if (newState == BluetoothProfile.STATE_DISCONNECTED) {
                    Log.w(
                        Constants.TAG_BLUETOOTH_GATT_CALLBACK,
                        "Successfully disconnected from $deviceAddress"
                    )
                    showToast("Deconnected from $deviceAddress")
                    gatt.close()
                    bluetoothGatt = null
                    isDialogVisible = false
                }
            } else {
                Log.w(
                    Constants.TAG_BLUETOOTH_GATT_CALLBACK,
                    "Error $status encountered for $deviceAddress! Disconnecting..."
                )
                showToast("Error while connecting at : $deviceAddress")
                gatt.close()
                bluetoothGatt = null
                isDialogVisible = false
            }
        }

        override fun onServicesDiscovered(gatt: BluetoothGatt, status: Int) {
            with(gatt) {
                Log.w(
                    Constants.TAG_BLUETOOTH_GATT_CALLBACK,
                    "Discovered ${services.size} services for ${device.address}"
                )
                servicesList = services
            }
        }
    }

    private fun toggleScanning() {
        isScanning = !isScanning

        if (isScanning) {
            startBleScan()
        } else {
            stopBleScan()
        }
    }

    private val bluetoothAdapter: BluetoothAdapter by lazy {
        val bluetoothManager = getSystemService(Context.BLUETOOTH_SERVICE) as BluetoothManager
        bluetoothManager.adapter
    }

    private val scanCallback = object : ScanCallback() {
        override fun onScanResult(callbackType: Int, result: ScanResult?) {
            result?.let {
                val existingDevice =
                    devicesFound.find { it.device.address == result.device.address }

                if (existingDevice == null) {
                    devicesFound.add(it)
                }

                with(it.device) {
                    Log.i(
                        Constants.TAG_SCAN_CALLBACK,
                        "Found BLE device! Name: ${name ?: "Unnamed"}, address: $address, type : $type, uuid : $uuids"
                    )
                }
            }
        }

        override fun onScanFailed(errorCode: Int) {
            Log.e(Constants.TAG_SCAN_CALLBACK, "Scan failed with error: $errorCode")
        }

        override fun onBatchScanResults(results: List<ScanResult?>) {
            results.forEach { result ->
                with(result!!.device) {
                    Log.i(
                        Constants.TAG_SCAN_CALLBACK,
                        "Batch result: ${name ?: "Unnamed"}, address: $address"
                    )
                }
            }
        }
    }

    private val enableBluetoothLauncher =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            if (result.resultCode == Activity.RESULT_OK) {
                Log.d(Constants.TAG_BLUETOOTH, "Bluetooth enabled")
            } else {
                Log.d(Constants.TAG_BLUETOOTH, "Bluetooth disabled")
            }
        }

    private val permissionLauncher =
        registerForActivityResult(ActivityResultContracts.RequestMultiplePermissions()) { permissions ->
            val containsPermanentDenial = permissions.entries.any {
                !it.value &&
                        !ActivityCompat.shouldShowRequestPermissionRationale(this, it.key)
            }
            val containsDenial = permissions.values.any { !it }
            val allGranted = permissions.values.all { it }

            when {
                containsPermanentDenial -> {
                    // TODO: Gérer le refus permanent (par exemple, afficher une AlertDialog avec justification)
                }

                containsDenial -> {
                    requestRelevantRuntimePermissions()
                }

                allGranted && PermissionsUtils.hasRequiredRuntimePermissions(this) -> {
                    startBleScan()
                }

                else -> {
                    recreate()
                }
            }
        }

    override fun onResume() {
        super.onResume()
        if (!bluetoothAdapter.isEnabled) {
            PermissionsUtils.requestEnableBluetooth(this, enableBluetoothLauncher)
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        if (!packageManager.hasSystemFeature(PackageManager.FEATURE_BLUETOOTH_LE)) {
            Toast.makeText(this, "BLE not supported on this device.", Toast.LENGTH_LONG)
                .show()
            finish()
            return
        }

        setContent {
            if (isDialogVisible) {
                ConnectionDialog(servicesList = servicesList, onDismiss = { isDialogVisible = false })
            }
            Column(
                modifier = Modifier.fillMaxSize(),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                ScanButton(
                    isScanning = isScanning,
                    onClick = {
                        toggleScanning()
                    }
                )
                if (isScanning) {
                    LinearProgressIndicator(modifier = Modifier.fillMaxWidth())
                }
                Text("Total devices : ${devicesFound.size}")
                DeviceList(
                    devices = devicesFound,
                    onDeviceClicked = {
                        onDeviceClicked(it)
                    }
                )
            }
        }
    }

    private fun requestRelevantRuntimePermissions() {
        when {
            Build.VERSION.SDK_INT < Build.VERSION_CODES.S -> {
                permissionLauncher.launch(arrayOf(Manifest.permission.ACCESS_FINE_LOCATION))
            }

            Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> {
                permissionLauncher.launch(
                    arrayOf(
                        Manifest.permission.BLUETOOTH_SCAN,
                        Manifest.permission.BLUETOOTH_CONNECT
                    )
                )
            }
        }
    }

    private fun onDeviceClicked(result: ScanResult) {
        if (isScanning) {
            toggleScanning()
        }

        resetConnectionState()

        val device = result.device
        Log.w(Constants.TAG_SCAN_RESULT_ADAPTER, "Connecting to ${device.address}")
        device.connectGatt(this, false, gattCallback)
        isDialogVisible = true
    }

    private fun startBleScan() {
        PermissionsUtils.requestRelevantRuntimePermissions(this, permissionLauncher)
        bleScanner.startScan(filters, scanSettings, scanCallback)
    }

    private fun stopBleScan() {
        bleScanner.stopScan(scanCallback)
    }

    private fun resetConnectionState() {
        bluetoothGatt?.close()
        bluetoothGatt = null
        servicesList = emptyList()
    }

    fun showToast(message: String) {
        runOnUiThread {
            Toast.makeText(this@MainActivity, message, Toast.LENGTH_SHORT).show()
        }
    }

}

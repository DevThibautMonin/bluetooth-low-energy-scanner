package com.example.bluetoothlowenergyscanner.utils

import android.Manifest
import android.app.Activity
import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothManager
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import androidx.activity.result.ActivityResultLauncher
import androidx.core.content.ContextCompat

object PermissionsUtils {
    fun requestEnableBluetooth(activity: Activity, enableBluetoothLauncher: ActivityResultLauncher<Intent>) {
        val bluetoothAdapter: BluetoothAdapter by lazy {
            val bluetoothManager = activity.getSystemService(Context.BLUETOOTH_SERVICE) as BluetoothManager
            bluetoothManager.adapter
        }

        if (!bluetoothAdapter.isEnabled) {
            val enableBluetoothIntent = Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE)
            enableBluetoothLauncher.launch(enableBluetoothIntent)
        }
    }

    fun requestRelevantRuntimePermissions(activity: Activity, permissionLauncher: ActivityResultLauncher<Array<String>>) {
        if (!hasRequiredRuntimePermissions(activity)) {
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
    }

    private fun hasPermission(activity: Activity, permissionType: String): Boolean {
        return ContextCompat.checkSelfPermission(activity, permissionType) ==
                PackageManager.PERMISSION_GRANTED
    }

    fun hasRequiredRuntimePermissions(activity: Activity): Boolean {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            hasPermission(activity, Manifest.permission.BLUETOOTH_SCAN) &&
                    hasPermission(activity, Manifest.permission.BLUETOOTH_CONNECT)
        } else {
            hasPermission(activity, Manifest.permission.ACCESS_FINE_LOCATION)
        }
    }
}

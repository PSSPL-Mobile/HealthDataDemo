package com.psspl.healthdatademo.wear.presentation.presentation

import android.Manifest
import android.os.Build
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import com.psspl.healthdatademo.wear.presentation.presentation.ui.MainScreen
import com.psspl.healthdatademo.wear.presentation.theme.HealthDataDemoTheme
import com.psspl.healthdatademo.wear.presentation.theme.StringResources
import dagger.hilt.android.AndroidEntryPoint

/***
 * Name : MainActivity.kt
 * Author : Prakash Software Pvt Ltd
 * Date : 30 Jun 2025
 * Desc : Main activity for the Wear OS version of the HealthDataDemo app, handling permissions and UI setup.
 * */
@AndroidEntryPoint
class MainActivity : ComponentActivity() {

    /***
     * Initializes the activity by calling the superclass and requesting permissions.
     */
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        requestPermissions()
    }

    /***
     * Requests necessary permissions for Bluetooth and sensor operations on Android 12+.
     */
    private fun requestPermissions() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            val permissions = arrayOf(
                Manifest.permission.BODY_SENSORS,
                Manifest.permission.BLUETOOTH_SCAN,
                Manifest.permission.BLUETOOTH_CONNECT,
                Manifest.permission.BLUETOOTH_ADVERTISE,
            )

            /*** Launcher for requesting multiple permissions and handling the result. **/
            val requestPermissionLauncher =
                registerForActivityResult(ActivityResultContracts.RequestMultiplePermissions()) { permissionsMap ->
                    if (permissionsMap.values.all { it }) {
                        setContent { HealthDataDemoTheme { MainScreen() } }
                    } else {
                        // Handle permission denial
                        Toast.makeText(
                            this,
                            StringResources.permissionsRequiredForBluetooth,
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                }
            requestPermissionLauncher.launch(permissions)
        }
    }
}
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
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        requestPermissions()
    }

    private fun requestPermissions() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            val permissions = arrayOf(
                Manifest.permission.BODY_SENSORS,
                Manifest.permission.BLUETOOTH_SCAN,
                Manifest.permission.BLUETOOTH_CONNECT,
                Manifest.permission.BLUETOOTH_ADVERTISE,
            )

            val requestPermissionLauncher =
                registerForActivityResult(ActivityResultContracts.RequestMultiplePermissions()) { permissionsMap ->
                    if (permissionsMap.values.all { it }) {
                        // Permissions granted, proceed
                        Toast.makeText(
                            this,
                            "Permissions granted for Bluetooth",
                            Toast.LENGTH_SHORT
                        ).show()
                        setContent { HealthDataDemoTheme { MainScreen() } }
                    } else {
                        // Handle permission denial
                        Toast.makeText(
                            this,
                            "Permissions required for Bluetooth",
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                }
            requestPermissionLauncher.launch(permissions)
        }
    }
}
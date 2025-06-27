package com.psspl.healthdatademo.presentation.activity

import android.Manifest
import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothManager
import android.content.Intent
import android.content.pm.PackageManager
import android.location.LocationManager
import android.os.Build
import android.os.Bundle
import android.provider.Settings
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.annotation.RequiresApi
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.graphics.Color
import androidx.core.app.ActivityCompat
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import androidx.core.view.WindowCompat
import com.google.accompanist.systemuicontroller.rememberSystemUiController
import com.psspl.healthdatademo.presentation.navgraph.NavGraph
import com.psspl.healthdatademo.ui.theme.HealthDataDemoTheme
import com.psspl.healthdatademo.ui.theme.StringResources
import dagger.hilt.android.AndroidEntryPoint

/***
 * Name : MainActivity.kt
 * Author : Prakash Software Pvt Ltd
 * Date : 27 Jun 2025
 * Desc : Main activity for the HealthDataDemo app, handling permissions, Bluetooth, GPS, and navigation setup.
 * */
@RequiresApi(Build.VERSION_CODES.O)
@AndroidEntryPoint
class MainActivity : ComponentActivity() {

    /**
     * ViewModel instance for managing the activity's state, including splash screen condition
     * and navigation start destination, injected via Hilt.
     */
    private val viewModel by viewModels<MainViewModel>()

    /**
     * Launcher for requesting a single permission (e.g., BLUETOOTH_CONNECT).
     * Handles the result to proceed with Bluetooth enablement or show a toast if denied.
     */
    private val permissionLauncher =
        registerForActivityResult(ActivityResultContracts.RequestPermission()) { isGranted ->
            if (isGranted) {
                checkAndEnableBluetooth()
            } else {
                Toast.makeText(
                    this,
                    StringResources.bluetoothConnectPermissionRequired,
                    Toast.LENGTH_SHORT
                ).show()
            }
        }

    /**
     * Launcher for requesting Bluetooth enablement via system settings.
     * Proceeds with GPS enablement if the user enables Bluetooth, otherwise shows a toast.
     */
    private val enableBtLauncher =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            if (result.resultCode == RESULT_OK) {
                checkAndEnableGps()
            } else {
                Toast.makeText(this, StringResources.bluetoothRequired, Toast.LENGTH_SHORT).show()
            }
        }

    /**
     * Launcher for requesting GPS enablement via system settings.
     * Rechecks GPS status and proceeds with additional permissions if enabled, otherwise shows a toast.
     */
    private val enableGpsLauncher =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            // Recheck GPS status instead of relying on result code
            val locationManager = getSystemService(LOCATION_SERVICE) as LocationManager
            if (locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)) {
                requestBluetoothPermissions()
            } else {
                Toast.makeText(
                    this,
                    StringResources.gpsNotEnabled,
                    Toast.LENGTH_SHORT
                ).show()
            }
        }

    /**
     * Launcher for requesting multiple permissions (e.g., BLUETOOTH_SCAN, LOCATION).
     * Checks if all permissions are granted and shows appropriate toasts based on the result.
     */
    private val permissionMultiLauncher =
        registerForActivityResult(ActivityResultContracts.RequestMultiplePermissions()) { permissionsMap ->
            if (permissionsMap.values.all { it }) {
                Toast.makeText(this, StringResources.permissionsGranted, Toast.LENGTH_SHORT).show()
            } else {
                Toast.makeText(this, StringResources.permissionsRequired, Toast.LENGTH_SHORT)
                    .show()
            }
        }

    @OptIn(ExperimentalMaterial3Api::class)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        WindowCompat.setDecorFitsSystemWindows(window, false)

        installSplashScreen().apply {
            setKeepOnScreenCondition(condition = { viewModel.splashCondition.value })
        }

        setContent {
            HealthDataDemoTheme {
                val isSystemInDarkMode = isSystemInDarkTheme()
                val systemUiColor = rememberSystemUiController()

                SideEffect {
                    systemUiColor.setSystemBarsColor(
                        color = Color.Transparent,
                        darkIcons = !isSystemInDarkMode
                    )
                }

                NavGraph(startDestination = viewModel.startDestination.value)
            }
        }

        if (viewModel._splashCondition.value == false) {
            requestPermissions()
        }
    }

    /**
     * Requests initial permissions based on Android version, starting with Bluetooth connect permission.
     * Checks for Bluetooth support and triggers the permission flow.
     */
    private fun requestPermissions() {
        val bluetoothManager: BluetoothManager =
            getSystemService(BLUETOOTH_SERVICE) as BluetoothManager
        val bluetoothAdapter: BluetoothAdapter? = bluetoothManager.adapter
        if (bluetoothAdapter == null) {
            Toast.makeText(this, StringResources.bluetoothNotSupported, Toast.LENGTH_SHORT).show()
            return
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            if (ActivityCompat.checkSelfPermission(
                    this,
                    Manifest.permission.BLUETOOTH_CONNECT
                ) != PackageManager.PERMISSION_GRANTED
            ) {
                permissionLauncher.launch(Manifest.permission.BLUETOOTH_CONNECT)
            } else {
                checkAndEnableBluetooth()
            }
        } else {
            checkAndEnableBluetooth()
        }
    }

    /**
     * Checks if Bluetooth is enabled and prompts the user to enable it if necessary.
     * Proceeds to GPS check if Bluetooth is already enabled.
     */
    private fun checkAndEnableBluetooth() {
        val bluetoothManager = getSystemService(BluetoothManager::class.java)
        val bluetoothAdapter: BluetoothAdapter? = bluetoothManager?.adapter
        if (bluetoothAdapter != null && !bluetoothAdapter.isEnabled) {
            val enableBtIntent = Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE)
            enableBtLauncher.launch(enableBtIntent)
        } else {
            checkAndEnableGps()
        }
    }

    /**
     * Checks if GPS is enabled and prompts the user to enable it if necessary.
     * Proceeds to request additional permissions if GPS is already enabled.
     */
    private fun checkAndEnableGps() {
        val locationManager = getSystemService(LOCATION_SERVICE) as LocationManager
        if (!locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)) {
            val enableGpsIntent = Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS)
            enableGpsLauncher.launch(enableGpsIntent)
        } else {
            requestBluetoothPermissions()
        }
    }

    /**
     * Requests multiple Bluetooth-related permissions for Android 12 (API 31) and above.
     * Includes SCAN, CONNECT, and location permissions required for BLE operations.
     */
    private fun requestBluetoothPermissions() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            val permissions = arrayOf(
                Manifest.permission.BLUETOOTH_SCAN,
                Manifest.permission.BLUETOOTH_CONNECT,
                Manifest.permission.ACCESS_FINE_LOCATION,
                Manifest.permission.ACCESS_COARSE_LOCATION
            )
            permissionMultiLauncher.launch(permissions)
        }
    }
}
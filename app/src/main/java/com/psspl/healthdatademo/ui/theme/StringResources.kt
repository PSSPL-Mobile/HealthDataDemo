package com.psspl.healthdatademo.ui.theme
import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource
import com.psspl.healthdatademo.R

/***
 * Name : StringResources.kt
 * Author : Prakash Software Pvt Ltd
 * Date : 30 Jun 2025
 * Desc : An object containing string resource identifiers and utility functions to access localized strings
 * for the HealthDataDemo app, categorized by usage (e.g., app name, toasts, content descriptions).
 * */
object StringResources {
    // App Name
    /**
     * Resource ID for the app's name, used as the title in the UI.
     */
    val appName = R.string.app_name

    // Toast Messages
    /**
     * Resource ID for a toast message indicating Bluetooth is not supported on the device.
     */
    val bluetoothNotSupported = R.string.bluetooth_not_supported

    /**
     * Resource ID for a toast message prompting the user to enable Bluetooth.
     */
    val bluetoothRequired = R.string.bluetooth_required

    /**
     * Resource ID for a toast message indicating Bluetooth connect permission is required.
     */
    val bluetoothConnectPermissionRequired = R.string.bluetooth_connect_permission_required

    /**
     * Resource ID for a toast message confirming that all permissions are granted.
     */
    val permissionsGranted = R.string.permissions_granted

    /**
     * Resource ID for a toast message indicating that permissions are required.
     */
    val permissionsRequired = R.string.permissions_required

    /**
     * Resource ID for a toast message indicating GPS is not enabled.
     */
    val gpsNotEnabled = R.string.gps_not_enabled

    // Content Descriptions
    /**
     * Resource ID for the content description of the settings icon, used for accessibility.
     */
    val settingsContentDescription = R.string.settings_content_description

    /**
     * Resource ID for the content description of the history icon, used for accessibility.
     */
    val history = R.string.history

    /**
     * Resource ID for the content description of the heart icon, used for accessibility.
     */
    val heartIcon = R.string.heart_icon

    // Ble Scan Screen
    /**
     * Resource ID for a formatted string displaying the current heart rate, accepting a value argument.
     */
    val heartRate = R.string.heart_rate_with_args

    /**
     * Resource ID for a string instructing the user to connect a device to start tracking health data.
     */
    val connectYourDeviceToStart = R.string.connect_your_device_to_start

    /**
     * Resource ID for a toast message confirming disconnection from the watch.
     */
    val disconnectedFromWatch = R.string.disconnected_from_watch

    /**
     * Resource ID for the text on the disconnect button.
     */
    val disconnect = R.string.disconnect

    /**
     * Resource ID for a toast message indicating the app is scanning for devices.
     */
    val scanningForDevices = R.string.scanning_for_devices

    /**
     * Resource ID for the text on the scan button to search for a watch.
     */
    val scanForWatch = R.string.scan_for_watch

    /**
     * Resource ID for a formatted string indicating connection to a device, accepting a device name argument.
     */
    val connectingTo = R.string.connecting_to

    /**
     * Resource ID for a default device name when the actual name is unavailable.
     */
    val heartRateDevice = R.string.heart_rate_device

    /**
     * Resource ID for the text on the acknowledge button in the alert dialog.
     */
    val acknowledge = R.string.acknowledge

    /**
     * Resource ID for the alert title or message prefix in the UI.
     */
    val alert = R.string.alert

    // Setting Screen
    /**
     * Resource ID for a string enabling notifications in the settings screen.
     */
    val enableNotifications = R.string.enable_notifications

    // Route
    /**
     * String constant for the BLE scan and connect screen route in navigation.
     */
    val bleScanAndConnectScreen = "bleScanAndConnectScreen"

    /**
     * String constant for the history screen route in navigation.
     */
    val historyScreen = "historyScreen"

    /**
     * String constant for the settings screen route in navigation.
     */
    val settingScreen = "settingScreen"

    /**
     * String constant for the app start navigation route in navigation.
     */
    val appStartNavigation = "appStartNavigation"

    // Algo used for decrypt and encrypt.
    val transformationAlgo = "AES"

    //Secret key for decrypt and encrypt.
    val secretKey = "MySecretKey12345".toByteArray()

    /**
     * Composable function to retrieve a localized string resource with optional format arguments.
     * @param resId The resource ID of the string to retrieve.
     * @param formatArgs Variable number of arguments to format the string, if applicable.
     * @return The localized string with applied format arguments.
     */
    @Composable
    fun getString(resId: Int, vararg formatArgs: Any): String {
        return stringResource(id = resId, formatArgs = formatArgs)
    }
}
package com.psspl.healthdatademo.ui.theme

import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource
import com.psspl.healthdatademo.R

object StringResources {
    // App Name
    val appName = R.string.app_name

    // Static

    // Toast Messages
    val bluetoothNotSupported = R.string.bluetooth_not_supported
    val bluetoothRequired = R.string.bluetooth_required
    val bluetoothConnectPermissionRequired = R.string.bluetooth_connect_permission_required
    val permissionsGranted = R.string.permissions_granted
    val permissionsRequired = R.string.permissions_required
    val gpsNotEnabled = R.string.gps_not_enabled

    // Content Descriptions
    val settingsContentDescription = R.string.settings_content_description
    val history = R.string.history
    val heartIcon = R.string.heart_icon

    // Ble scan screen
    val heartRate = R.string.heart_rate_with_args

    //Ble scan and connect Screen

    //Setting Screen
    val enableNotifications = R.string.enable_notifications

    //Route
    val bleScanAndConnectScreen = "bleScanAndConnectScreen"
    val historyScreen = "historyScreen"
    val settingScreen = "settingScreen"
    val appStartNavigation = "appStartNavigation"

    // Composable functions for string resources with arguments (if needed)
    @Composable
    fun getString(resId: Int, vararg formatArgs: Any): String {
        return stringResource(id = resId, formatArgs = formatArgs)
    }
}
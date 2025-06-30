
package com.psspl.healthdatademo.wear.presentation.theme
import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource
import com.psspl.healthdatademo.wear.R

/***
 * Name : StringResources.kt
 * Author : Prakash Software Pvt Ltd
 * Date : 30 Jun 2025
 * Desc : An object containing string resource identifiers and utility functions to access localized strings
 * for the Wear OS version of the HealthDataDemo app, categorized by usage (e.g., toast messages).
 * */
object StringResources {
    /**
     * Resource ID for a string representing the heart rate monitor feature or title.
     */
    val heartRateMonitor = R.string.heart_rate_monitor

    /**
     * Resource ID for a string indicating a high stress level, used in alerts or notifications.
     */
    val highStress = R.string.high_stress

    /**
     * Resource ID for a string indicating a low stress level, used in alerts or notifications.
     */
    val lowStress = R.string.low_stress

    /**
     * Resource ID for a string indicating a successful connection to a device.
     */
    val connected = R.string.connected

    /**
     * Resource ID for a string indicating a disconnection from a device.
     */
    val disconnected = R.string.disconnected

    /**
     * Resource ID for the text on the acknowledge button in alert dialogs.
     */
    val acknowledge = R.string.acknowledge

    /***
     * Resource ID for show permission require msg.
     */
    val permissionsRequiredForBluetooth = R.string.permissions_required_for_bluetooth

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
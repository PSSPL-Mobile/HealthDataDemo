package com.psspl.healthdatademo.wear.presentation.theme

import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource
import com.psspl.healthdatademo.wear.R

object StringResources {
    // Toast Messages
    val heartRateMonitor = R.string.heart_rate_monitor

    val highStress = R.string.high_stress
    val lowStress = R.string.low_stress

    val connected = R.string.connected
    val disconnected = R.string.disconnected

    val acknowledge = R.string.acknowledge

    // Composable functions for string resources with arguments (if needed)
    @Composable
    fun getString(resId: Int, vararg formatArgs: Any): String {
        return stringResource(id = resId, formatArgs = formatArgs)
    }
}
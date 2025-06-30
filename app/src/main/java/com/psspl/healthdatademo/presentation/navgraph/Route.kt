package com.psspl.healthdatademo.presentation.navgraph

import com.psspl.healthdatademo.ui.theme.StringResources

/***
 * Name : Route.kt
 * Author : Prakash Software Pvt Ltd
 * Date : 27 Jun 2025
 * Desc : Defines navigation routes used throughout the app.
 *        @param route The string identifier for the navigation destination.
 **/
sealed class Route(val route: String) {
    // Route for BLE scan and connect screen (main landing screen)
    data object BleScanAndConnectScreen : Route(route = StringResources.bleScanAndConnectScreen)

    // Route for the settings screen
    data object SettingScreen : Route(route = StringResources.settingScreen)

    // Route for the heart rate history screen
    data object HistoryScreen : Route(route = StringResources.historyScreen)

    // Root route for the app's startup navigation graph
    data object AppStartNavigation : Route(route = StringResources.appStartNavigation)
}

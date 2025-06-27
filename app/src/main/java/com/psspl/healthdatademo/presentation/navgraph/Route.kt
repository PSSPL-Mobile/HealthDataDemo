package com.psspl.healthdatademo.presentation.navgraph

import com.psspl.healthdatademo.ui.theme.StringResources

sealed class Route(val route: String) {
    data object BleScanAndConnectScreen : Route(route = StringResources.bleScanAndConnectScreen)

    data object SettingScreen : Route(route = StringResources.settingScreen)

    data object HistoryScreen : Route(route = StringResources.historyScreen)

    data object AppStartNavigation : Route(route = StringResources.appStartNavigation)
}
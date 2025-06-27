package com.psspl.healthdatademo.presentation.navgraph

import androidx.compose.runtime.Composable
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navigation
import com.psspl.healthdatademo.presentation.blescanconnect.ui.BleScanAndConnectScreen
import com.psspl.healthdatademo.presentation.blescanconnect.BleScanConnectViewModel
import com.psspl.healthdatademo.presentation.history.HistoryScreen
import com.psspl.healthdatademo.presentation.history.HistoryViewModel
import com.psspl.healthdatademo.presentation.setting.SettingScreen
import com.psspl.healthdatademo.presentation.setting.SettingViewModel
import com.psspl.healthdatademo.ui.theme.StringResources

@Composable
fun NavGraph(startDestination: String) {
    val navController = rememberNavController()
    NavHost(navController = navController, startDestination = startDestination) {
        navigation(
            route = Route.AppStartNavigation.route,
            startDestination = Route.BleScanAndConnectScreen.route
        ) {
            composable(route = Route.BleScanAndConnectScreen.route) {
                val viewModel: BleScanConnectViewModel = hiltViewModel()
                BleScanAndConnectScreen(viewModel, navigateTo = {
                    if (it == StringResources.settingScreen) {
                        navigateToSettings(navController)
                    } else {
                        navigateToHistory(navController)
                    }
                })
            }
            composable(route = Route.SettingScreen.route) {
                val viewModel: SettingViewModel = hiltViewModel()
                SettingScreen(viewModel)
            }
            composable(route = Route.HistoryScreen.route) {
                val viewModel: HistoryViewModel = hiltViewModel()
                HistoryScreen(viewModel)
            }
        }
    }
}

fun navigateToSettings(navController: NavController) {
    navController.navigate(route = Route.SettingScreen.route)
}

fun navigateToHistory(navController: NavController) {
    navController.navigate(route = Route.HistoryScreen.route)
}

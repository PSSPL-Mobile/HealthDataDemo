package com.psspl.healthdatademo.presentation.navgraph

import androidx.compose.runtime.Composable
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navigation
import com.psspl.healthdatademo.presentation.blescanconnect.BleScanConnectViewModel
import com.psspl.healthdatademo.presentation.blescanconnect.ui.BleScanAndConnectScreen
import com.psspl.healthdatademo.presentation.history.HistoryScreen
import com.psspl.healthdatademo.presentation.history.HistoryViewModel
import com.psspl.healthdatademo.presentation.setting.SettingScreen
import com.psspl.healthdatademo.presentation.setting.SettingViewModel
import com.psspl.healthdatademo.ui.theme.StringResources

/***
 * Name : PhoneWearApplication.kt
 * Author : Prakash Software Pvt Ltd
 * Date : 30 Jun 2025
 * Desc : Main navigation graph for the app.
 * @param startDestination The route that should be launched first.
 * */
@Composable
fun NavGraph(startDestination: String) {
    val navController = rememberNavController() // Controller to handle navigation stack

    // Top-level navigation host that maps routes to screens
    NavHost(
        navController = navController,
        startDestination = startDestination // Starting point of the graph
    ) {
        // Define a nested navigation graph for app startup flow
        navigation(
            route = Route.AppStartNavigation.route,
            startDestination = Route.BleScanAndConnectScreen.route
        ) {
            // BLE scan/connect screen route
            composable(route = Route.BleScanAndConnectScreen.route) {
                val viewModel: BleScanConnectViewModel = hiltViewModel() // Inject ViewModel
                BleScanAndConnectScreen(
                    viewModel,
                    navigateTo = { screen ->
                        // Navigate based on button click from BLE screen
                        if (screen == StringResources.settingScreen) {
                            navigateToSettings(navController)
                        } else {
                            navigateToHistory(navController)
                        }
                    }
                )
            }

            // Settings screen route
            composable(route = Route.SettingScreen.route) {
                val viewModel: SettingViewModel = hiltViewModel()
                SettingScreen(viewModel)
            }

            // History screen route
            composable(route = Route.HistoryScreen.route) {
                val viewModel: HistoryViewModel = hiltViewModel()
                HistoryScreen(viewModel)
            }
        }
    }
}

/**
 * Navigates to the settings screen.
 * @param navController The controller to perform navigation.
 */
fun navigateToSettings(navController: NavController) {
    navController.navigate(route = Route.SettingScreen.route)
}

/**
 * Navigates to the history screen.
 * @param navController The controller to perform navigation.
 */
fun navigateToHistory(navController: NavController) {
    navController.navigate(route = Route.HistoryScreen.route)
}

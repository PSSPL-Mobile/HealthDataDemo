package com.psspl.healthdatademo.presentation.setting

import android.Manifest
import androidx.annotation.RequiresPermission
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.psspl.healthdatademo.ui.theme.StringResources
import com.psspl.healthdatademo.ui.theme.cardBg
import com.psspl.healthdatademo.ui.theme.screenBg

/***
 * Name : PhoneWearApplication.kt
 * Author : Prakash Software Pvt Ltd
 * Date : 30 Jun 2025
 * Desc : Settings screen showing user preferences like notification toggle.
 * @param viewModel ViewModel providing setting state and toggle function.
 * */
@RequiresPermission(Manifest.permission.BLUETOOTH_CONNECT)
@Composable
fun SettingScreen(viewModel: SettingViewModel = hiltViewModel()) {

    // Observing the notification toggle state from DataStore via ViewModel
    val isNotificationEnabled = viewModel.isNotificationEnabled.collectAsState()

    Scaffold(
        topBar = { ShowSettingAppBar() }, // App bar at the top
        containerColor = screenBg,        // Set background color for Scaffold
        content = { padding ->
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .background(screenBg)
                    .padding(padding)
                    .padding(vertical = 10.dp, horizontal = 15.dp) // Outer spacing
            ) {
                // Notification setting card
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = cardBg),
                    shape = RoundedCornerShape(12.dp),
                    elevation = CardDefaults.cardElevation(4.dp)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp, vertical = 5.dp), // Inner padding
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        // Label for the switch
                        Text(
                            text = StringResources.getString(StringResources.enableNotifications),
                            color = Color.White,
                            style = MaterialTheme.typography.bodyMedium
                        )
                        // Toggle switch for notification
                        Switch(
                            checked = isNotificationEnabled.value, // Current value from ViewModel
                            onCheckedChange = { viewModel.toggleNotification(it) }, // Update value
                            colors = SwitchDefaults.colors(
                                checkedThumbColor = Color.White,
                                checkedTrackColor = Color(0xFF4C5FE4) // Custom blue track
                            )
                        )
                    }
                }
            }
        }
    )
}

/**
 * App bar composable for the settings screen.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ShowSettingAppBar() {
    TopAppBar(
        title = {
            Text(
                text = StringResources.getString(resId = StringResources.settingsContentDescription), // "Settings"
                color = MaterialTheme.colorScheme.onPrimary
            )
        },
        colors = TopAppBarDefaults.topAppBarColors(containerColor = screenBg)
    )
}
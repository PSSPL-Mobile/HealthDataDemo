package com.psspl.healthdatademo.presentation.setting

import android.Manifest
import androidx.annotation.RequiresPermission
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
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

@RequiresPermission(Manifest.permission.BLUETOOTH_CONNECT)
@Composable
fun SettingScreen(viewModel: SettingViewModel = hiltViewModel()) {
    val isNotificationEnabled = viewModel.isNotificationEnabled.collectAsState()

    Scaffold(
        topBar = { ShowSettingAppBar() },
        containerColor = screenBg,
        content = { padding ->
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .background(screenBg)
                    .padding(padding)
                    .padding(vertical = 10.dp, horizontal = 15.dp)
            ) {
                Card(
                    modifier = Modifier
                        .fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = cardBg),
                    shape = RoundedCornerShape(12.dp),
                    elevation = CardDefaults.cardElevation(4.dp)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp, vertical = 5.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            text = StringResources.getString(StringResources.enableNotifications),
                            color = Color.White,
                            style = MaterialTheme.typography.bodyMedium
                        )
                        Switch(
                            checked = isNotificationEnabled.value,
                            onCheckedChange = { viewModel.toggleNotification(it) },
                            colors = SwitchDefaults.colors(
                                checkedThumbColor = Color.White,
                                checkedTrackColor = Color(0xFF4C5FE4)
                            )
                        )
                    }
                }
            }
        }
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ShowSettingAppBar() {
    TopAppBar(
        title = {
            Text(
                text = StringResources.getString(resId = StringResources.settingsContentDescription),
                color = MaterialTheme.colorScheme.onPrimary
            )
        },
        colors = TopAppBarDefaults.topAppBarColors(containerColor = screenBg),
    )
}

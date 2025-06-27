package com.psspl.healthdatademo.presentation.blescanconnect.ui

import android.Manifest
import android.content.pm.PackageManager
import android.widget.Toast
import androidx.annotation.RequiresPermission
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.core.app.ActivityCompat
import androidx.hilt.navigation.compose.hiltViewModel
import com.psspl.healthdatademo.R
import com.psspl.healthdatademo.presentation.blescanconnect.BleScanConnectViewModel
import com.psspl.healthdatademo.ui.theme.StringResources
import com.psspl.healthdatademo.ui.theme.cardBg
import com.psspl.healthdatademo.ui.theme.screenBg

/***
 * Name : BleScanAndConnectScreen.kt
 * Author : Prakash Software Pvt Ltd
 * Date : 27 Jun 2025
 * Desc : Composable screen for scanning and connecting to BLE devices to monitor heart rate data.
 * @param viewModel The BleScanConnectViewModel instance, injected via Hilt, defaults to hiltViewModel().
 * @param navigateTo A lambda function to navigate to another screen, provided by the navigation host.
 **/
@RequiresPermission(Manifest.permission.BLUETOOTH_CONNECT)
@Composable
fun BleScanAndConnectScreen(
    viewModel: BleScanConnectViewModel = hiltViewModel(),
    navigateTo: (String) -> Unit
) {
    /**
     * Local context for accessing Android resources and showing toasts.
     */
    val context = LocalContext.current

    /**
     * Current heart rate data collected from the ViewModel, observed as a state.
     */
    val heartRateData = viewModel.heartRateData.collectAsState().value

    /**
     * Current alert status from the ViewModel, observed as a state.
     */
    val isAlert = viewModel.isAlert.collectAsState().value

    /**
     * Current connection status to a BLE device, observed as a state.
     */
    val isConnected = viewModel.connectionState.collectAsState().value

    /**
     * List of discovered BLE devices, observed as a state.
     */
    val discoveredDevices = viewModel.discoveredDevices.collectAsState().value

    Scaffold(
        topBar = { ShowAppBar(navigateTo) },
        content = { padding ->
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(screenBg) // Deep blue background
                    .padding(padding)
            ) {
                Column(modifier = Modifier.fillMaxSize()) {
                    // Centered Watch Icon
                    Icon(
                        painter = painterResource(id = R.drawable.ic_watch),
                        contentDescription = "Watch Icon",
                        tint = Color(0xFFB9CBE0),
                        modifier = Modifier
                            .size(100.dp)
                            .align(Alignment.CenterHorizontally)
                    )

                    Spacer(modifier = Modifier.height(24.dp))

                    // Status Text
                    Text(
                        text = if (isConnected && heartRateData != null)
                            StringResources.getString(
                                StringResources.heartRate,
                                heartRateData.heartRate
                            )
                        else
                            "Disconnected or No Data",
                        color = Color.White,
                        style = MaterialTheme.typography.titleLarge,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.fillMaxWidth()
                    )

                    Text(
                        text = "Connect your device to start tracking your health data",
                        color = Color(0xFFB9CBE0),
                        style = MaterialTheme.typography.bodyMedium,
                        textAlign = TextAlign.Center,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 8.dp)
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    if (isConnected) {
                        // Scan Button
                        Button(
                            onClick = {
                                if (ActivityCompat.checkSelfPermission(
                                        context,
                                        Manifest.permission.BLUETOOTH_CONNECT
                                    ) != PackageManager.PERMISSION_GRANTED
                                ) {
                                    return@Button
                                }
                                viewModel.disconnect()
                                Toast.makeText(
                                    context,
                                    "Disconnected from watch",
                                    Toast.LENGTH_SHORT
                                )
                                    .show()
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF558CF6)),
                            modifier = Modifier
                                .align(Alignment.CenterHorizontally)
                                .padding(vertical = 8.dp)
                        ) {
                            Text(text = "Disconnect", color = Color.White)
                        }

                        // Heart Rate Waveform Graph
                        Spacer(Modifier.height(10.dp))
                        val heartRateHistory = viewModel.heartRateHistory.collectAsState().value
                        ECGStyleGraph(dataPoints = heartRateHistory)
                    } else {
                        // Scan Button
                        Button(
                            onClick = {
                                viewModel.startScanning()
                                Toast.makeText(
                                    context,
                                    "Scanning for devices...",
                                    Toast.LENGTH_SHORT
                                ).show()
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF558CF6)),
                            modifier = Modifier
                                .align(Alignment.CenterHorizontally)
                                .padding(vertical = 8.dp)
                        ) {
                            Text(text = "Scan for Watch", color = Color.White)
                        }
                    }

                    Spacer(modifier = Modifier.height(40.dp))

                    if (discoveredDevices.isNotEmpty() && !isConnected) {
                        LazyColumn(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 10.dp)
                        ) {
                            items(discoveredDevices.size) { index ->
                                val device = discoveredDevices[index]
                                Card(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clickable(onClick = {
                                            viewModel.stopScanning()
                                            viewModel.connectToWearDevice(device)
                                            Toast.makeText(
                                                context,
                                                "Connecting to ${device.name ?: "Heart Rate Device"}",
                                                Toast.LENGTH_SHORT
                                            ).show()
                                        }),
                                    colors = CardDefaults.cardColors(containerColor = cardBg)
                                ) {
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        modifier = Modifier.padding(16.dp)
                                    ) {
                                        Icon(
                                            painter = painterResource(id = R.drawable.ic_heart), // R
                                            contentDescription = "Heart Icon",
                                            tint = Color(0xFF8AB4F8),
                                        )
                                        Spacer(modifier = Modifier.width(10.dp))
                                        Text(
                                            text = device.name
                                                ?: "Heart Rate Device (${device.address})",
                                            color = Color.White,
                                            style = MaterialTheme.typography.bodyLarge
                                        )
                                    }
                                }
                                Spacer(modifier = Modifier.height(10.dp))
                            }
                        }
                    }

                    if (isAlert) {
                        AlertDialog(
                            onDismissRequest = {
                                viewModel.dismissAlert()
                            },
                            title = {
                                Text(
                                    text = "Mindfulness Prompt",
                                    style = MaterialTheme.typography.bodyLarge,
                                    textAlign = TextAlign.Center
                                )
                            },
                            text = {
                                Text(
                                    text = heartRateData?.alertMsg.toString(),
                                    style = MaterialTheme.typography.bodyMedium,
                                    textAlign = TextAlign.Center
                                )
                            },
                            confirmButton = {
                                Button(
                                    onClick = {
                                        viewModel.dismissAlert()
                                    },
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(8.dp)
                                ) {
                                    Text("Acknowledge")
                                }
                            },
                            dismissButton = null,
                            modifier = Modifier.padding(16.dp)
                        )
                    }
                }
            }
        }
    )
}

/**
 * Composable function to display the top app bar with navigation icons.
 * @param navigateTo A lambda function to navigate to another screen, provided by the navigation host.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ShowAppBar(navigateTo: (String) -> Unit) {
    TopAppBar(
        title = {
            Text(
                text = StringResources.getString(resId = StringResources.appName),
                color = MaterialTheme.colorScheme.onPrimary
            )
        },
        colors = TopAppBarDefaults.topAppBarColors(containerColor = screenBg),
        actions = {
            Icon(
                painter = painterResource(id = R.drawable.ic_history), // Replace with your resource name // Replace with Image if using asset
                contentDescription = StringResources.getString(resId = StringResources.history),
                tint = MaterialTheme.colorScheme.onPrimary,
                modifier = Modifier
                    .padding(5.dp)
                    .clickable(onClick = {
                        navigateTo("")
                    }),
            )
            Icon(
                painter = painterResource(id = R.drawable.ic_setting), // Replace with your resource name // Replace with Image if using asset
                contentDescription = StringResources.getString(resId = StringResources.settingsContentDescription),
                tint = MaterialTheme.colorScheme.onPrimary,
                modifier = Modifier
                    .padding(5.dp)
                    .clickable(onClick = {
                        navigateTo(StringResources.settingScreen)
                    }),
            )
        }
    )
}
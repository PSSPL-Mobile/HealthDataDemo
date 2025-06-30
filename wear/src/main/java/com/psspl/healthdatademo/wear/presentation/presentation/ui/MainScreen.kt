package com.psspl.healthdatademo.wear.presentation.presentation.ui

import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.core.app.ActivityCompat
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.wear.compose.material.Button
import androidx.wear.compose.material.MaterialTheme
import androidx.wear.compose.material.Text
import com.psspl.healthdatademo.wear.presentation.presentation.MainViewModel
import com.psspl.healthdatademo.wear.presentation.service.HeartRateService
import com.psspl.healthdatademo.wear.presentation.theme.StringResources
import com.psspl.healthdatademo.wear.presentation.theme.green
import com.psspl.healthdatademo.wear.presentation.theme.screenBg
import com.psspl.healthdatademo.wear.presentation.theme.white

/***
 * Name : MainScreen.kt
 * Author : Prakash Software Pvt Ltd
 * Date : 30 Jun 2025
 * Desc : Contains composable functions for the main screen and mindfulness prompt UI in the Wear OS version
 * of the HealthDataDemo app, integrating with ViewModel and theme resources.
 * */
@SuppressLint("BatteryLife")
@Composable
fun MainScreen(
    /*** ViewModel instance for managing UI state, injected via Hilt, defaults to hiltViewModel(). **/
    viewModel: MainViewModel = hiltViewModel(),
    /*** Context for the current composition, defaults to LocalContext.current. **/
    context: Context = LocalContext.current
) {
    /*** Current heart rate value collected from the ViewModel. **/
    val heartRate = viewModel.heartRate.collectAsState().value

    /*** Current connection state collected from the ViewModel. **/
    val isConnected = viewModel.connectionState.collectAsState().value

    /*** Launches an effect to start the heart rate service if BLE permission is granted. **/
    LaunchedEffect(Unit) {
        if (ActivityCompat.checkSelfPermission(
                context,
                android.Manifest.permission.BLUETOOTH_CONNECT
            ) == PackageManager.PERMISSION_GRANTED
        ) {
            val intent = Intent(context, HeartRateService::class.java)
            context.startForegroundService(intent)
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(color = screenBg),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            StringResources.getString(StringResources.heartRateMonitor),
            textAlign = TextAlign.Center,
            color = green,
            style = MaterialTheme.typography.body1,
        )
        Text(
            text = if (heartRate > 80)
                StringResources.getString(StringResources.highStress)
            else
                StringResources.getString(StringResources.lowStress),
            style = MaterialTheme.typography.body2,
            textAlign = TextAlign.Center,
            color = white
        )
        Text(
            text = "Status : ${
                if (isConnected)
                    StringResources.getString(StringResources.connected)
                else
                    StringResources.getString(StringResources.disconnected)
            }",
            style = MaterialTheme.typography.body2,
            textAlign = TextAlign.Center,
            color = white
        )
    }

    /*** Composes the mindfulness prompt UI based on the ViewModel state. **/
    MindfulnessPrompt(viewModel)
}

/***
 * ViewModel instance for managing mindfulness prompt state.
 */
@Composable
private fun MindfulnessPrompt(
    /*** ViewModel instance for managing mindfulness prompt state. **/
    viewModel: MainViewModel
) {
    /*** Current visibility state of the mindfulness prompt collected from the ViewModel. **/
    val showMindfulnessPrompt = viewModel.showMindfulnessPrompt.collectAsState().value

    /*** Current message for the mindfulness prompt collected from the ViewModel. **/
    val mindfulnessMessage = viewModel.mindfulnessMessage.collectAsState().value

    if (showMindfulnessPrompt) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(MaterialTheme.colors.surface.copy(alpha = 0.9f)),
            contentAlignment = Alignment.Center
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp)
                    .background(
                        MaterialTheme.colors.background,
                        shape = RoundedCornerShape(8.dp)
                    ).padding(16.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = mindfulnessMessage,
                    style = MaterialTheme.typography.body1,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.padding(bottom = 8.dp),
                    color = MaterialTheme.colors.onBackground
                )
                Button(
                    onClick = { viewModel.dismissMindfulnessPrompt() },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(StringResources.getString(StringResources.acknowledge))
                }
            }
        }
    } else {
        viewModel.dismissMindfulnessPrompt()
    }
}
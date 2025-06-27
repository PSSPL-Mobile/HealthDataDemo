package com.psspl.healthdatademo.presentation.history

import android.Manifest
import androidx.annotation.RequiresPermission
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.psspl.healthdatademo.R
import com.psspl.healthdatademo.ui.theme.StringResources
import com.psspl.healthdatademo.ui.theme.cardBg
import com.psspl.healthdatademo.ui.theme.screenBg
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

/***
 * Name : HistoryScreen.kt
 * Author : Prakash Software Pvt Ltd
 * Date : 27 Jun 2025
 * Desc : Composable screen that displays a list of heart rate alerts from local database.
 **/
@RequiresPermission(Manifest.permission.BLUETOOTH_CONNECT)
@Composable
fun HistoryScreen(viewModel: HistoryViewModel = hiltViewModel()) {

    // Collect heart rate alert data from ViewModel's StateFlow
    val heartRateAlerts = viewModel.heartRateAlerts.collectAsState().value

    Scaffold(
        topBar = { ShowHistoryAppBar() },
        containerColor = screenBg,
        content = { padding ->
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .background(screenBg)
                    .padding(padding)
                    .padding(horizontal = 16.dp)
            ) {
                // LazyColumn to show each heart rate alert as a card
                LazyColumn(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 10.dp)
                ) {
                    items(heartRateAlerts) { alert ->
                        val dateFormat = SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault()) // Format timestamp
                        val formattedDate = dateFormat.format(Date(alert.timestamp)) // Convert timestamp to readable date

                        HistoryCard(
                            bpm = alert.heartRate,
                            message = alert.alertMsg,
                            time = formattedDate
                        )
                    }
                }
            }
        }
    )
}

/**
 * A composable card to display single alert data including BPM, message, and time.
 * @param bpm Heart rate value in BPM.
 * @param message Alert reason or note.
 * @param time Formatted string of the time alert was recorded.
 */
@Composable
fun HistoryCard(bpm: Int, message: String, time: String) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 6.dp), // Space between each card
        colors = CardDefaults.cardColors(containerColor = cardBg),
        shape = RoundedCornerShape(12.dp), // Rounded corners for modern look
        elevation = CardDefaults.cardElevation(4.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 16.dp), // Inner spacing
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Column {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    // Heart icon before BPM value
                    Icon(
                        painter = painterResource(id = R.drawable.ic_heart),
                        contentDescription = StringResources.getString(StringResources.heartIcon),
                        tint = Color(0xFF8AB4F8),
                        modifier = Modifier.padding(end = 6.dp)
                    )
                    Text(
                        text = "$bpm BPM", // Display BPM
                        color = Color.White,
                        style = MaterialTheme.typography.titleMedium
                    )
                }

                Spacer(Modifier.height(10.dp)) // Gap between BPM and message

                // Display alert message
                Text(
                    text = message,
                    color = Color(0xFF8AB4F8),
                    style = MaterialTheme.typography.bodySmall
                )
            }

            // Display timestamp at the end of row
            Text(
                text = time,
                color = Color(0xFF8AB4F8),
                style = MaterialTheme.typography.bodySmall,
                textAlign = TextAlign.End
            )
        }
    }
}

/**
 * Top app bar for the History screen.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ShowHistoryAppBar() {
    TopAppBar(
        title = {
            Text(
                text = StringResources.getString(resId = StringResources.history),
                color = MaterialTheme.colorScheme.onPrimary
            )
        },
        colors = TopAppBarDefaults.topAppBarColors(containerColor = screenBg),
    )
}
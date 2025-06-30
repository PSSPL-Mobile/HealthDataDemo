package com.psspl.healthdatademo.wear.presentation.service

import android.Manifest
import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Intent
import android.content.pm.PackageManager
import android.os.CountDownTimer
import android.util.Log
import androidx.core.app.ActivityCompat
import androidx.core.app.NotificationCompat
import androidx.lifecycle.LifecycleService
import com.psspl.healthdatademo.wear.R
import com.psspl.healthdatademo.wear.presentation.data.bluetooth.BleServerManager
import com.psspl.healthdatademo.wear.presentation.data.model.HeartRateData
import com.psspl.healthdatademo.wear.presentation.domain.repository.HeartRateRepository
import com.psspl.healthdatademo.wear.presentation.domain.usecase.SendHeartRateUseCase
import com.psspl.healthdatademo.wear.presentation.presentation.MainActivity
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.launch
import javax.inject.Inject

/***
 * Name : HeartRateService.kt
 * Author : Prakash Software Pvt Ltd
 * Date : 30 Jun 2025
 * Desc : A LifecycleService responsible for managing heart rate monitoring in the background for the Wear OS version
 * of the HealthDataDemo app. It handles BLE server operations, collects heart rate data, and provides notifications.
 * */
@AndroidEntryPoint
class HeartRateService : LifecycleService() {

    /*** Tag for logging messages related to this service. **/
    var TAG = "HeartRateService"

    /*** Injected repository for accessing heart rate data, initialized via Hilt. **/
    @Inject
    lateinit var heartRateRepository: HeartRateRepository

    /*** Injected use case for sending heart rate data, initialized via Hilt. **/
    @Inject
    lateinit var sendHeartRateUseCase: SendHeartRateUseCase

    /*** Injected manager for handling BLE server operations, initialized via Hilt. **/
    @Inject
    lateinit var bleServerManager: BleServerManager

    /*** Injected manager for handling heart rate data and alerts, initialized via Hilt. **/
    @Inject
    lateinit var heartRateManager: HeartRateManager

    /*** Timer for monitoring heart rate changes over a period, nullable to allow cancellation. **/
    private var countDownTimer: CountDownTimer? = null

    /*** Coroutine scope for IO operations, used for asynchronous tasks in the service. **/
    private val coroutineScope = CoroutineScope(Dispatchers.IO)

    /*** Current heart rate data collected from the repository, nullable until data is available. **/
    var heartRateData: HeartRateData? = null

    /*** Flow of the latest heart rate data from the repository, nullable until initialized. **/
    var latestHeartRateData: Flow<HeartRateData>? = null

    /***
     * Initializes the foreground service, BLE server, and heart rate collection.
     * Returns START_STICKY to ensure service restarts on crash.
     **/
    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        super.onStartCommand(intent, flags, startId)
        createNotificationChannel()
        startForeground(1, buildNotification())
        startBleServer()
        collectHeartRate()
        coroutineScope.launch {
            latestHeartRateData = heartRateRepository.getHeartRate()
        }
        Log.e(TAG, "Service started")
        return START_STICKY
    }

    /***
     * Creates a notification channel for the foreground service to display its status.
     */
    private fun createNotificationChannel() {
        val channel = NotificationChannel(
            "heart_rate_channel",
            "Heart Rate Service",
            NotificationManager.IMPORTANCE_DEFAULT
        ).apply { setShowBadge(false) }
        val manager = getSystemService(NotificationManager::class.java)
        manager.createNotificationChannel(channel)
    }

    /***
     * Builds a notification for the foreground service with title, text, and intent to return to the app.
     */
    private fun buildNotification(): Notification {
        val intent = Intent(this, MainActivity::class.java)
        val pendingIntent = PendingIntent.getActivity(
            this, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        return NotificationCompat.Builder(this, "heart_rate_channel")
            .setContentTitle("Heart Rate Monitoring")
            .setContentText("Running in background... Tap to return")
            .setSmallIcon(R.drawable.splash_icon)
            .setContentIntent(pendingIntent)
            .setOngoing(true)
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .build()
    }

    /***
     * Starts the BLE server to advertise heart rate data, checking for BLUETOOTH_CONNECT permission.
     */
    private fun startBleServer() {
        if (ActivityCompat.checkSelfPermission(
                this,
                Manifest.permission.BLUETOOTH_CONNECT
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            return
        }
        bleServerManager.startServer()
    }

    /***
     * Collects heart rate data from the repository and updates the service state, sending data via use case.
     */
    fun collectHeartRate() {
        coroutineScope.launch {
            heartRateRepository.getHeartRate().collect {
                heartRateManager.apply {
                    this@HeartRateService.heartRateData = it
                    this@HeartRateService.heartRateData?.isAlert = false
                    this@HeartRateService.heartRateData?.alertMsg = _mindfulnessMessage.value
                    this@HeartRateService.heartRateData?.let {
                        sendHeartRateUseCase(it)
                        heartRateManager.updateHeartRate(it)
                        Log.e(TAG, "Value:${heartRateManager._heartRate.value}")
                    }
                }
            }
        }
    }

    /***
     * Starts or resets a countdown timer to monitor heart rate over 60 seconds, checking for high BPM.
     */
    private fun startOrResetTimer() {
        countDownTimer?.cancel()

        if (countDownTimer == null) {
            countDownTimer = object : CountDownTimer(60000, 1000) {
                override fun onTick(millisUntilFinished: Long) {
                    Log.e(TAG, "Timer tick: $millisUntilFinished")
                    heartRateManager.apply {
                        if (heartRateManager._heartRate.value > 80) {
                            Log.e(TAG, "Constant High")
                            isConstantBpmHigh.value = true
                        } else {
                            Log.e(TAG, "Constant Low")
                            isConstantBpmHigh.value = false
                        }
                    }
                }

                override fun onFinish() {
                    Log.e(TAG, "Timer finished, checking alert")
                    heartRateManager.apply {
                        coroutineScope.launch {
                            if (isConstantBpmHigh.value == true) {
                                _showMindfulnessPrompt.value = true
                                isConstantBpmHigh.value = false
                            } else {
                                Log.e(TAG, "No Alert")
                            }
                            stopTimer()
                        }
                    }
                }
            }
        }

        countDownTimer?.start()
    }

    /***
     * Stops the current timer, delays for 2 seconds, and restarts it to continue monitoring.
     */
    private suspend fun stopTimer() {
        countDownTimer?.cancel()
        delay(2000)
        startOrResetTimer() // Restart timer
    }
}
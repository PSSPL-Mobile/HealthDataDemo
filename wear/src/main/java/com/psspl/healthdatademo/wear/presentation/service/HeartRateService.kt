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

@AndroidEntryPoint
class HeartRateService : LifecycleService() {

    var TAG = "HeartRateService"

    @Inject
    lateinit var heartRateRepository: HeartRateRepository

    @Inject
    lateinit var sendHeartRateUseCase: SendHeartRateUseCase

    @Inject
    lateinit var bleServerManager: BleServerManager

    @Inject
    lateinit var heartRateManager: HeartRateManager

    private var countDownTimer: CountDownTimer? = null
    private val coroutineScope = CoroutineScope(Dispatchers.IO)

    var heartRateData: HeartRateData? = null

    var latestHeartRateData: Flow<HeartRateData>? = null

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

    private fun createNotificationChannel() {
        val channel = NotificationChannel(
            "heart_rate_channel",
            "Heart Rate Service",
            NotificationManager.IMPORTANCE_DEFAULT
        ).apply { setShowBadge(false) }
        val manager = getSystemService(NotificationManager::class.java)
        manager.createNotificationChannel(channel)
    }

    private fun buildNotification(): Notification {
        val intent = Intent(this, MainActivity::class.java) // Removed flags
        val pendingIntent = PendingIntent.getActivity(
            this, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        return NotificationCompat.Builder(this, "heart_rate_channel")
            .setContentTitle("Heart Rate Monitoring")
            .setContentText("Running in background... Tap to return")
            .setSmallIcon(R.drawable.splash_icon)
            .setContentIntent(pendingIntent)
            .setOngoing(true) // Prevents user from dismissing
            .setPriority(NotificationCompat.PRIORITY_HIGH) // Ensures visibility
            .build()
    }

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

    private suspend fun stopTimer() {
        countDownTimer?.cancel()
        delay(2000)
        startOrResetTimer() // Restart timer
    }
}
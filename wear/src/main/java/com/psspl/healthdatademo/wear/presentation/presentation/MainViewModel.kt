package com.psspl.healthdatademo.wear.presentation.presentation

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.util.Log
import androidx.core.app.ActivityCompat
import androidx.lifecycle.ViewModel
import com.psspl.healthdatademo.wear.presentation.data.bluetooth.BleServerManager
import com.psspl.healthdatademo.wear.presentation.domain.repository.HeartRateRepository
import com.psspl.healthdatademo.wear.presentation.service.HeartRateManager
import com.psspl.healthdatademo.wear.presentation.service.HeartRateService
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.StateFlow
import javax.inject.Inject

@HiltViewModel
class MainViewModel @Inject constructor(
    private val heartRateRepository: HeartRateRepository,
    private val bleServerManager: BleServerManager,
    private val heartRateManager: HeartRateManager
) : ViewModel() {

    var TAG = "MainViewModel"

    val connectionState: StateFlow<Boolean> = bleServerManager.connectionState

    // Mindfulness prompt visibility and message
    val heartRate: StateFlow<Int> = heartRateManager.heartRate
    val showMindfulnessPrompt: StateFlow<Boolean> = heartRateManager.showMindfulnessPrompt
    val mindfulnessMessage: StateFlow<String> = heartRateManager.mindfulnessMessage

    init {
        Log.e(TAG, "checkSelfPermission true at ${System.currentTimeMillis()}")
    }

    // Function to dismiss the mindfulness prompt
    fun dismissMindfulnessPrompt() {
        heartRateManager.triggerMindfulnessPrompt(false, "Take 5 deep breaths")
        Log.e(TAG, "Mindfulness prompt dismissed at ${System.currentTimeMillis()}")
    }

    override fun onCleared() {
        super.onCleared()
        if (ActivityCompat.checkSelfPermission(
                bleServerManager.context,
                Manifest.permission.BLUETOOTH_CONNECT
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            Log.e(TAG, "onCleared() permission not granted at ${System.currentTimeMillis()}")
            return
        }
        val intent = Intent(bleServerManager.context, HeartRateService::class.java)
        bleServerManager.context.stopService(intent)
    }
}
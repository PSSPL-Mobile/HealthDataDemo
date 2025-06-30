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

/***
 * Name : MainViewModel.kt
 * Author : Prakash Software Pvt Ltd
 * Date : 30 Jun 2025
 * Desc : A ViewModel for the main screen of the Wear OS version of the HealthDataDemo app, handling BLE connection state
 * and heart rate data, with Hilt dependency injection.
 * */
@HiltViewModel
class MainViewModel @Inject constructor(
    /*** Repository for accessing heart rate data, injected via Hilt. **/
    private val heartRateRepository: HeartRateRepository,
    /*** Manager for handling BLE server operations, injected via Hilt. **/
    private val bleServerManager: BleServerManager,
    /*** Manager for handling heart rate data and alerts, injected via Hilt. **/
    private val heartRateManager: HeartRateManager
) : ViewModel() {

    /*** Tag for logging messages related to this ViewModel. **/
    var TAG = "MainViewModel"

    /*** State flow exposing the current BLE connection state from the BLE server manager. **/
    val connectionState: StateFlow<Boolean> = bleServerManager.connectionState

    /*** State flow exposing the current heart rate value from the heart rate manager. **/
    val heartRate: StateFlow<Int> = heartRateManager.heartRate

    /*** State flow exposing the visibility of the mindfulness prompt from the heart rate manager. **/
    val showMindfulnessPrompt: StateFlow<Boolean> = heartRateManager.showMindfulnessPrompt

    /*** State flow exposing the mindfulness message from the heart rate manager. **/
    val mindfulnessMessage: StateFlow<String> = heartRateManager.mindfulnessMessage

    init {
        /*** Initializes the ViewModel with a log entry for permission check. **/
        Log.e(TAG, "checkSelfPermission true at ${System.currentTimeMillis()}")
    }

    /***
     * Dismisses the mindfulness prompt by resetting its state and logging the action.
     */
    fun dismissMindfulnessPrompt() {
        heartRateManager.triggerMindfulnessPrompt(false, "Take 5 deep breaths")
        Log.e(TAG, "Mindfulness prompt dismissed at ${System.currentTimeMillis()}")
    }

    /***
     * Cleans up resources when the ViewModel is cleared, stopping the heart rate service if permissions allow.
     */
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
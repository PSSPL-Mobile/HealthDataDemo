package com.psspl.healthdatademo.presentation.blescanconnect

import android.Manifest
import android.bluetooth.BluetoothDevice
import androidx.annotation.RequiresPermission
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.psspl.healthdatademo.data.bluetooth.BleClientManager
import com.psspl.healthdatademo.data.datastore.MyDataStore
import com.psspl.healthdatademo.data.model.HeartRateData
import com.psspl.healthdatademo.data.room.HeartRateAlertEntity
import com.psspl.healthdatademo.di.AppDatabase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

/***
 * Name : BleScanConnectViewModel.kt
 * Author : Prakash Software Pvt Ltd
 * Date : 27 Jun 2025
 * Desc : Stores business logic for BleScanAndConnectScreen.kt.
 **/
@HiltViewModel
class BleScanConnectViewModel @Inject constructor(
    private val bleClientManager: BleClientManager, // BLE client for scanning/connection
    private val db: AppDatabase, // Room database instance
    val dataStore: DataStore<Preferences> // DataStore for persistent settings
) : ViewModel() {

    // BLE heart rate data (live stream from device)
    val heartRateData: StateFlow<HeartRateData?> = bleClientManager.heartRateData

    // BLE connection state (true if connected)
    val connectionState: StateFlow<Boolean> = bleClientManager.connectionState

    // List of devices found during BLE scan
    val discoveredDevices: StateFlow<List<BluetoothDevice>> = bleClientManager.discoveredDevices

    // Whether an alert should be shown on UI
    private val _isAlert = MutableStateFlow(false)
    val isAlert: StateFlow<Boolean> = _isAlert.asStateFlow()

    // Realtime history of ECG-like waveform data
    private val _heartRateHistory = MutableStateFlow<List<Float>>(emptyList())
    val heartRateHistory: StateFlow<List<Float>> = _heartRateHistory.asStateFlow()

    // Maximum number of points to keep in waveform history
    private val maxVisiblePoints = 300

    init {
        // Observe heart rate updates and process accordingly
        viewModelScope.launch {
            bleClientManager.heartRateData.collect { data ->
                val bpm = data?.heartRate ?: 0 // Get BPM or 0 if null
                updateECGPattern(bpm) // Update ECG waveform

                if (data?.isAlert == true && bpm > 0) {
                    val isEnabled = MyDataStore.getNotificationEnabledFlow(dataStore).first() // Check if alerts are enabled
                    if (isEnabled) {
                        _isAlert.value = true // Trigger alert in UI
                    }
                    insertHeartRateAlert(bpm, data.alertMsg, data.timestamp) // Save alert to DB
                }
            }
        }
    }

    /**
     * Smoothly append ECG waveform points based on BPM.
     * @param bpm Heart rate in beats per minute.
     */
    private fun updateECGPattern(bpm: Int) {
        val pattern = generateEcgPattern(bpm) // Generate ECG wave points for 1 beat

        viewModelScope.launch {
            for (point in pattern) {
                _heartRateHistory.update { current ->
                    val updated = current + point // Add new point
                    if (updated.size > maxVisiblePoints) {
                        updated.takeLast(maxVisiblePoints) // Trim excess
                    } else updated
                }
                delay(100L) // Delay to simulate live drawing
            }
        }
    }

    /**
     * Builds a heartbeat waveform pattern based on BPM.
     * @param bpm Heart rate in beats per minute.
     * @return A list of normalized waveform points (0f to 1f).
     */
    private fun generateEcgPattern(bpm: Int): List<Float> {
        if (bpm == 0) return List(30) { 0.5f } // Return flatline if bpm is 0

        // Length of one heartbeat cycle in number of points
        val cycleLength = (60f / bpm * 30f).toInt().coerceIn(30, 70)

        // Base PQRST waveform pattern (simplified)
        val basePattern = listOf(
            0.5f, 0.52f, 0.48f, 0.5f, 0.2f, 0.9f,
            0.3f, 0.7f, 1f, 0.7f, 0.4f, 0.5f, 0.52f, 0.48f
        ).let {
            if (cycleLength > it.size) it + List(cycleLength - it.size) { 0.5f } // Pad with baseline
            else it.take(cycleLength) // Trim if too long
        }

        // Scale amplitude based on bpm (higher bpm = taller waveform)
        val amplitude = ((bpm - 60f) / 40f).coerceIn(0.4f, 1.2f)
        val scaledPattern = basePattern.map { 0.5f + (it - 0.5f) * amplitude }

        // Interpolate between points for smoothness
        val interpolated = mutableListOf<Float>()
        for (i in 1 until scaledPattern.size) {
            val a = scaledPattern[i - 1]
            val b = scaledPattern[i]
            interpolated.add(a) // Original point
            interpolated.add((a + b) / 2f) // Midpoint
        }
        interpolated.add(scaledPattern.last()) // Ensure last point added

        return interpolated
    }

    /**
     * Resets alert visibility state.
     */
    fun dismissAlert() {
        _isAlert.value = false
    }

    /**
     * Inserts a heart rate alert into the local database.
     * @param heartRate The BPM that triggered the alert.
     * @param alertMsg Description of the alert condition.
     * @param timestamp Epoch timestamp of the alert event.
     */
    suspend fun insertHeartRateAlert(heartRate: Int, alertMsg: String, timestamp: Long) {
        db.heartRateAlertDao().insert(
            HeartRateAlertEntity(
                heartRate = heartRate,
                alertMsg = alertMsg,
                timestamp = timestamp
            )
        )
    }

    /**
     * Starts BLE device discovery.
     * @Requires android.permission.BLUETOOTH_SCAN
     */
    @RequiresPermission(Manifest.permission.BLUETOOTH_SCAN)
    fun startScanning() {
        bleClientManager.scanForDevices()
    }

    /**
     * Stops BLE device discovery.
     * @Requires android.permission.BLUETOOTH_SCAN
     */
    @RequiresPermission(Manifest.permission.BLUETOOTH_SCAN)
    fun stopScanning() {
        bleClientManager.stopScanning()
    }

    /**
     * Connects to the specified BLE device.
     * @param device The Bluetooth device to connect to.
     * @Requires android.permission.BLUETOOTH_SCAN and BLUETOOTH_CONNECT
     */
    @RequiresPermission(allOf = [Manifest.permission.BLUETOOTH_SCAN, Manifest.permission.BLUETOOTH_CONNECT])
    fun connectToWearDevice(device: BluetoothDevice) {
        bleClientManager.connectToDevice(device)
    }

    /**
     * Disconnects the currently connected BLE device.
     * @Requires android.permission.BLUETOOTH_CONNECT
     */
    @RequiresPermission(Manifest.permission.BLUETOOTH_CONNECT)
    fun disconnect() {
        bleClientManager.disconnect()
    }

    /**
     * Automatically disconnects BLE on ViewModel cleanup.
     * @Requires android.permission.BLUETOOTH_CONNECT
     */
    @RequiresPermission(Manifest.permission.BLUETOOTH_CONNECT)
    override fun onCleared() {
        bleClientManager.disconnect()
        super.onCleared()
    }
}

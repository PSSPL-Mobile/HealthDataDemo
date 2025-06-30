package com.psspl.healthdatademo.wear.presentation.data.bluetooth

import android.Manifest
import androidx.annotation.RequiresPermission
import com.psspl.healthdatademo.wear.presentation.data.model.HeartRateData
import com.psspl.healthdatademo.wear.presentation.data.sensor.HeartRateSensorManager
import com.psspl.healthdatademo.wear.presentation.domain.repository.HeartRateRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

/***
 * Name : HeartRateRepositoryImpl.kt
 * Author : Prakash Software Pvt Ltd
 * Date : 30 Jun 2025
 * Desc : Implements the heart rate repository for the Wear OS version of the HealthDataDemo app, integrating sensor and BLE data.
 */
class HeartRateRepositoryImpl @Inject constructor(
    /*** Manager for accessing heart rate sensor data, injected via Hilt. **/
    private val sensorManager: HeartRateSensorManager,
    /*** Manager for handling BLE server operations, injected via Hilt. **/
    private val bleServerManager: BleServerManager
) : HeartRateRepository {

    /***
     * Retrieves a flow of heart rate data from the sensor manager.
     */
    override suspend fun getHeartRate(): Flow<HeartRateData> {
        return sensorManager.getHeartRateFlow()
    }

    /***
     * Sends the provided heart rate data via BLE, requiring BLUETOOTH_CONNECT permission.
     */
    @RequiresPermission(Manifest.permission.BLUETOOTH_CONNECT)
    override suspend fun sendHeartRate(heartRate: HeartRateData) {
        bleServerManager.sendHeartRate(heartRate)
    }
}
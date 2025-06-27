package com.psspl.healthdatademo.wear.presentation.data.bluetooth

import android.Manifest
import androidx.annotation.RequiresPermission
import com.psspl.healthdatademo.wear.presentation.data.model.HeartRateData
import com.psspl.healthdatademo.wear.presentation.data.sensor.HeartRateSensorManager
import com.psspl.healthdatademo.wear.presentation.domain.repository.HeartRateRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class HeartRateRepositoryImpl @Inject constructor(
    private val sensorManager: HeartRateSensorManager,
    private val bleServerManager: BleServerManager
) : HeartRateRepository {

    override suspend fun getHeartRate(): Flow<HeartRateData> {
        return sensorManager.getHeartRateFlow()
    }

    @RequiresPermission(Manifest.permission.BLUETOOTH_CONNECT)
    override suspend fun sendHeartRate(heartRate: HeartRateData) {
        bleServerManager.sendHeartRate(heartRate)
    }
}
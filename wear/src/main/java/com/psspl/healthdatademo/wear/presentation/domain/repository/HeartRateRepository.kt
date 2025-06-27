package com.psspl.healthdatademo.wear.presentation.domain.repository

import com.psspl.healthdatademo.wear.presentation.data.model.HeartRateData
import kotlinx.coroutines.flow.Flow

interface HeartRateRepository {
    suspend fun getHeartRate(): Flow<HeartRateData>
    suspend fun sendHeartRate(heartRate: HeartRateData)
}
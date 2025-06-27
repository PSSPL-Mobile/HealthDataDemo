package com.psspl.healthdatademo.domain.repository
import com.psspl.healthdatademo.data.model.HeartRateData
import kotlinx.coroutines.flow.Flow

/***
 * Name : HeartRateRepository.kt
 * Author : Prakash Software Pvt Ltd
 * Date : 27 Jun 2025
 * Desc : Interface defining the contract for heart rate data repository operations.
 * */
interface HeartRateRepository {
    /**
     * Provides a flow of heart rate data received from the data source.
     * @return A Flow emitting HeartRateData objects.
     */
    fun receiveHeartRate(): Flow<HeartRateData>
}
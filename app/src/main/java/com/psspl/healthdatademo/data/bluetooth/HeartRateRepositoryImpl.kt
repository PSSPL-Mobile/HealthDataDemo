package com.psspl.healthdatademo.data.bluetooth

import com.psspl.healthdatademo.data.model.HeartRateData
import com.psspl.healthdatademo.domain.repository.HeartRateRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.filterNotNull
import javax.inject.Inject

/***
 * Name : HeartRateRepositoryImpl.kt
 * Author : Prakash Software Pvt Ltd
 * Date : 27 Jun 2025
 * Desc : Implementation of the HeartRateRepository for managing heart rate data from BLE.
 * */
class HeartRateRepositoryImpl @Inject constructor(
    private val bleClientManager: BleClientManager // BLE client manager for heart rate data source
) : HeartRateRepository {

    /**
     * Provides a flow of heart rate data, filtering out null values from the BLE client manager.
     * @return A Flow emitting non-null HeartRateData objects.
     */
    override fun receiveHeartRate(): Flow<HeartRateData> {
        return bleClientManager.heartRateData.filterNotNull()
    }
}
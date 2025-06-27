package com.psspl.healthdatademo.domain.usecase
import com.psspl.healthdatademo.data.model.HeartRateData
import com.psspl.healthdatademo.domain.repository.HeartRateRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

/***
 * Name : ReceiveHeartRateUseCase.kt
 * Author : Prakash Software Pvt Ltd
 * Date : 27 Jun 2025
 * Desc : Use case for retrieving heart rate data from the repository as a flow.
 * */
class ReceiveHeartRateUseCase @Inject constructor(
    private val repository: HeartRateRepository // Repository instance for heart rate data access
) {
    /**
     * Invokes the use case to retrieve a flow of heart rate data.
     * @return A Flow emitting HeartRateData objects from the repository.
     */
    operator fun invoke(): Flow<HeartRateData> {
        return repository.receiveHeartRate()
    }
}
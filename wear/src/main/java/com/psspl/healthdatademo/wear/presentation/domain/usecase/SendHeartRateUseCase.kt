package com.psspl.healthdatademo.wear.presentation.domain.usecase

import com.psspl.healthdatademo.wear.presentation.data.model.HeartRateData
import com.psspl.healthdatademo.wear.presentation.domain.repository.HeartRateRepository
import javax.inject.Inject

/***
 * Name : SendHeartRateUseCase.kt
 * Author : Prakash Software Pvt Ltd
 * Date : 30 Jun 2025
 * Desc : A use case class responsible for sending heart rate data to the repository in the Wear OS version
 * of the HealthDataDemo app, utilizing dependency injection.
 */
class SendHeartRateUseCase @Inject constructor(
    /*** Repository instance for sending heart rate data, injected via Hilt. **/
    private val repository: HeartRateRepository
) {
    /***
     * Invokes the use case to send the provided heart rate data to the repository.
     */
    suspend operator fun invoke(heartRate: HeartRateData) {
        repository.sendHeartRate(heartRate)
    }
}
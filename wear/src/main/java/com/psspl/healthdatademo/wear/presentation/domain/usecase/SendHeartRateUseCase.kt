package com.psspl.healthdatademo.wear.presentation.domain.usecase

import com.psspl.healthdatademo.wear.presentation.data.model.HeartRateData
import com.psspl.healthdatademo.wear.presentation.domain.repository.HeartRateRepository
import javax.inject.Inject

class SendHeartRateUseCase @Inject constructor(
    private val repository: HeartRateRepository
) {
    suspend operator fun invoke(heartRate: HeartRateData) {
        repository.sendHeartRate(heartRate)
    }
}
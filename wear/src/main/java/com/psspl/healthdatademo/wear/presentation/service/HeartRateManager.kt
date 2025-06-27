package com.psspl.healthdatademo.wear.presentation.service

import com.psspl.healthdatademo.wear.presentation.data.model.HeartRateData
import jakarta.inject.Inject
import jakarta.inject.Singleton
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

@Singleton
class HeartRateManager @Inject constructor() {

    var _heartRate = MutableStateFlow(0)
    var heartRate: StateFlow<Int> = _heartRate

    var _showMindfulnessPrompt = MutableStateFlow(false)
    val showMindfulnessPrompt: StateFlow<Boolean> = _showMindfulnessPrompt

    val _mindfulnessMessage = MutableStateFlow("Take 5 deep breaths")
    val mindfulnessMessage: StateFlow<String> = _mindfulnessMessage

    val isConstantBpmHigh = MutableStateFlow(false)

    fun updateHeartRate(heartRateData: HeartRateData) {
        _heartRate.value = heartRateData.bpm
    }

    fun triggerMindfulnessPrompt(isAlert: Boolean, message: String) {
        _showMindfulnessPrompt.value = isAlert
        _mindfulnessMessage.value = message
    }
}
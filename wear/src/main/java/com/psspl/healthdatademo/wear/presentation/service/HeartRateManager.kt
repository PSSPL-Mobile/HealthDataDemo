package com.psspl.healthdatademo.wear.presentation.service

import com.psspl.healthdatademo.wear.presentation.data.model.HeartRateData
import jakarta.inject.Inject
import jakarta.inject.Singleton
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

/***
 * Name : HeartRateManager.kt
 * Author : Prakash Software Pvt Ltd
 * Date : 30 Jun 2025
 * Desc : Manages heart rate data and mindfulness prompts for the Wear OS version of the HealthDataDemo app.
 * */
@Singleton
class HeartRateManager @Inject constructor() {

    /*** Mutable state flow holding the current heart rate value, initialized to 0. **/
    var _heartRate = MutableStateFlow(0)

    /*** Read-only state flow exposing the current heart rate value to observers. **/
    var heartRate: StateFlow<Int> = _heartRate

    /*** Mutable state flow indicating whether to show a mindfulness prompt, initialized to false. **/
    var _showMindfulnessPrompt = MutableStateFlow(false)

    /*** Read-only state flow exposing the mindfulness prompt visibility to observers. **/
    val showMindfulnessPrompt: StateFlow<Boolean> = _showMindfulnessPrompt

    /*** Mutable state flow holding the mindfulness message, initialized to "Take 5 deep breaths". **/
    val _mindfulnessMessage = MutableStateFlow("Take 5 deep breaths")

    /*** Read-only state flow exposing the mindfulness message to observers. **/
    val mindfulnessMessage: StateFlow<String> = _mindfulnessMessage

    /*** Mutable state flow indicating if the heart rate is consistently high, initialized to false. **/
    val isConstantBpmHigh = MutableStateFlow(false)

    /*** Updates the heart rate value based on the provided HeartRateData. **/
    fun updateHeartRate(heartRateData: HeartRateData) {
        _heartRate.value = heartRateData.bpm
    }

    /***
     * Triggers a mindfulness prompt by setting its visibility and message based on input parameters.
     ***/
    fun triggerMindfulnessPrompt(isAlert: Boolean, message: String) {
        _showMindfulnessPrompt.value = isAlert
        _mindfulnessMessage.value = message
    }
}
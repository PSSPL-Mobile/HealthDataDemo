package com.psspl.healthdatademo.wear.presentation.data.model

/***
 * Name : HeartRateData.kt
 * Author : Prakash Software Pvt Ltd
 * Date : 30 Jun 2025
 * Desc : Defines the data model for heart rate information in the Wear OS version of the HealthDataDemo app.
 */
data class HeartRateData(
    /*** The current heart rate in beats per minute (BPM), mutable for updates. **/
    var bpm: Int,
    /*** The timestamp of the heart rate measurement in milliseconds, mutable for updates. **/
    var timestamp: Long,
    /*** Flag indicating if an alert is triggered, defaults to false, mutable for updates. **/
    var isAlert: Boolean = false,
    /*** Message associated with the alert, mutable for updates. **/
    var alertMsg: String
)
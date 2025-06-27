/***
 * Name : HeartRateData.kt
 * Author : Prakash Software Pvt Ltd
 * Date : 27 Jun 2025
 * Desc : Data class representing heart rate information with alert status and message.
 * */
package com.psspl.healthdatademo.data.model

data class HeartRateData(
    val heartRate: Int = 0, // Heart rate value in beats per minute (BPM), defaults to 0
    val timestamp: Long, // Timestamp of when the heart rate was recorded
    var isAlert: Boolean, // Indicates if the heart rate triggers an alert
    var alertMsg: String // Message associated with the alert, if any
)
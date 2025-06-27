/***
 * Name : HeartRateAlertEntity.kt
 * Author : Prakash Software Pvt Ltd
 * Date : 27 Jun 2025
 * Desc : Room entity class representing heart rate alerts stored in the database.
 * */
package com.psspl.healthdatademo.data.room

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "heart_rate_alerts")
data class HeartRateAlertEntity(
    @PrimaryKey(autoGenerate = true) val id: Int = 0, // Unique identifier for each alert, auto-incremented
    val heartRate: Int, // Heart rate value in beats per minute (BPM) triggering the alert
    val alertMsg: String, // Message associated with the alert
    val timestamp: Long // Timestamp when the alert was recorded
)
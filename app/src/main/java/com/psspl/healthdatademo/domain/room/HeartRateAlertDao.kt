package com.psspl.healthdatademo.domain.room

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import com.psspl.healthdatademo.data.room.HeartRateAlertEntity

/***
 * Name : HeartRateAlertDao.kt
 * Author : Prakash Software Pvt Ltd
 * Date : 27 Jun 2025
 * Desc : Room DAO interface for managing heart rate alert data operations.
 * */
@Dao
interface HeartRateAlertDao {
    /**
     * Inserts a new heart rate alert into the database.
     * @param alert The HeartRateAlertEntity to insert.
     */
    @Insert
    suspend fun insert(alert: HeartRateAlertEntity)

    /**
     * Retrieves all heart rate alerts from the database, ordered by timestamp in descending order.
     * @return A list of HeartRateAlertEntity objects.
     */
    @Query("SELECT * FROM heart_rate_alerts ORDER BY timestamp DESC")
    suspend fun getAllHeartRateAlerts(): List<HeartRateAlertEntity>
}
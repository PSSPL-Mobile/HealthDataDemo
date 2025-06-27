
/***
 * Name : AppDatabase.kt
 * Author : Prakash Software Pvt Ltd
 * Date : 27 Jun 2025
 * Desc : Room database class for managing heart rate alert data storage.
 * */
package com.psspl.healthdatademo.di
import androidx.room.Database
import androidx.room.RoomDatabase
import com.psspl.healthdatademo.data.room.HeartRateAlertEntity
import com.psspl.healthdatademo.domain.room.HeartRateAlertDao

@Database(entities = [HeartRateAlertEntity::class], version = 1)
abstract class AppDatabase : RoomDatabase() {
    /**
     * Provides an abstract method to access the HeartRateAlertDao for database operations.
     * @return The HeartRateAlertDao instance.
     */
    abstract fun heartRateAlertDao(): HeartRateAlertDao
}
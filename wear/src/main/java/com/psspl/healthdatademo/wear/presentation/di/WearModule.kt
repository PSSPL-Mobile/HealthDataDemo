package com.psspl.healthdatademo.wear.presentation.di

import android.content.Context
import com.psspl.healthdatademo.wear.presentation.data.bluetooth.BleServerManager
import com.psspl.healthdatademo.wear.presentation.data.bluetooth.HeartRateRepositoryImpl
import com.psspl.healthdatademo.wear.presentation.data.sensor.HeartRateSensorManager
import com.psspl.healthdatademo.wear.presentation.domain.repository.HeartRateRepository
import com.psspl.healthdatademo.wear.presentation.service.HeartRateManager
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

/***
 * Name : WearModule.kt
 * Author : Prakash Software Pvt Ltd
 * Date : 30 Jun 2025
 * Desc : Defines the dependency injection module for the Wear OS version of the HealthDataDemo app.
 */
@Module
@InstallIn(SingletonComponent::class)
object WearModule {

    /***
     *  Provides a singleton instance of HeartRateSensorManager with the application context.
     */
    @Provides
    @Singleton
    fun provideHeartRateSensorManager(@ApplicationContext context: Context): HeartRateSensorManager {
        return HeartRateSensorManager(context)
    }

    /***
     * Provides a singleton instance of BleServerManager with the application context.
     */
    @Provides
    @Singleton
    fun provideBleServerManager(
        @ApplicationContext context: Context
    ): BleServerManager {
        return BleServerManager(context)
    }

    /***
     * Provides a singleton instance of HeartRateRepository using sensor and BLE managers.
     */
    @Provides
    @Singleton
    fun provideHeartRateRepository(
        sensorManager: HeartRateSensorManager,
        bleServerManager: BleServerManager
    ): HeartRateRepository {
        return HeartRateRepositoryImpl(sensorManager, bleServerManager)
    }

    /***
     * Provides a singleton instance of HeartRateManager.
     */
    @Provides
    @Singleton
    fun provideHeartRateManager(): HeartRateManager {
        return HeartRateManager()
    }
}
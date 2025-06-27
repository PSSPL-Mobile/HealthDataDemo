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

@Module
@InstallIn(SingletonComponent::class)
object WearModule {

    @Provides
    @Singleton
    fun provideHeartRateSensorManager(@ApplicationContext context: Context): HeartRateSensorManager {
        return HeartRateSensorManager(context)
    }

    @Provides
    @Singleton
    fun provideBleServerManager(
        @ApplicationContext context: Context
    ): BleServerManager {
        return BleServerManager(context)
    }

    @Provides
    @Singleton
    fun provideHeartRateRepository(
        sensorManager: HeartRateSensorManager,
        bleServerManager: BleServerManager
    ): HeartRateRepository {
        return HeartRateRepositoryImpl(sensorManager, bleServerManager)
    }

    @Provides
    @Singleton
    fun provideHeartRateManager(): HeartRateManager {
        return HeartRateManager()
    }
}
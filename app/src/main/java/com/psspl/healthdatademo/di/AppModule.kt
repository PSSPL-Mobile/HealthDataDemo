package com.psspl.healthdatademo.di

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.preferencesDataStore
import androidx.room.Room
import com.psspl.healthdatademo.data.bluetooth.BleClientManager
import com.psspl.healthdatademo.data.bluetooth.HeartRateRepositoryImpl
import com.psspl.healthdatademo.domain.repository.HeartRateRepository
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "settings") // Extension delegate for DataStore instance

/***
 * Name : AppModule.kt
 * Author : Prakash Software Pvt Ltd
 * Date : 27 Jun 2025
 * Desc : Dagger Hilt module for providing dependencies related to BLE, repository, database, and DataStore.
 * */
@Module
@InstallIn(SingletonComponent::class)
object AppModule {

    /**
     * Provides a singleton instance of BleClientManager.
     * @param context Application context for BLE operations.
     * @return A new BleClientManager instance.
     */
    @Provides
    @Singleton
    fun provideBleClientManager(
        @ApplicationContext context: Context
    ): BleClientManager {
        return BleClientManager(context)
    }

    /**
     * Provides a singleton instance of HeartRateRepository.
     * @param bleClientManager The BLE client manager dependency.
     * @return A new HeartRateRepositoryImpl instance.
     */
    @Provides
    @Singleton
    fun provideHeartRateRepository(
        bleClientManager: BleClientManager
    ): HeartRateRepository {
        return HeartRateRepositoryImpl(bleClientManager)
    }

    /**
     * Provides a singleton instance of AppDatabase.
     * @param context Application context for database creation.
     * @return A Room database instance named "health_data_db".
     */
    @Provides
    @Singleton
    fun provideAppDatabase(@ApplicationContext context: Context): AppDatabase {
        return Room.databaseBuilder(
            context,
            AppDatabase::class.java,
            "health_data_db"
        ).build()
    }

    /**
     * Provides a singleton instance of DataStore for preferences.
     * @param context Application context for accessing the DataStore.
     * @return The DataStore instance configured with "settings" name.
     */
    @Singleton
    @Provides
    fun provideDataStore(@ApplicationContext context: Context): DataStore<Preferences> {
        return context.dataStore
    }
}
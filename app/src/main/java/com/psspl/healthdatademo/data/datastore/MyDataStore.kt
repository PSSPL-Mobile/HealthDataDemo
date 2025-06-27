package com.psspl.healthdatademo.data.datastore

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

/***
 * Name : MyDataStore.kt
 * Author : Prakash Software Pvt Ltd
 * Date : 27 Jun 2025
 * Desc : Utility object for managing data storage preferences, specifically notification settings.
 **/
object MyDataStore {

    private val NOTIFICATION_ENABLED_KEY =
        booleanPreferencesKey("notification_enabled") // Key for storing notification enabled state

    /**
     * Retrieves a Flow of the notification enabled state from the DataStore.
     * @param dataStore The DataStore instance to read from.
     * @return A Flow emitting the boolean value of notification_enabled.
     */
    fun getNotificationEnabledFlow(dataStore: DataStore<Preferences>): Flow<Boolean> {
        return dataStore.data.map { preferences ->
            preferences[NOTIFICATION_ENABLED_KEY] == true
        }
    }

    /**
     * Updates the notification enabled state in the DataStore.
     * @param dataStore The DataStore instance to write to.
     * @param enabled The boolean value to set for notification_enabled.
     */
    suspend fun setNotificationEnabled(dataStore: DataStore<Preferences>, enabled: Boolean) {
        dataStore.edit { preferences ->
            preferences[NOTIFICATION_ENABLED_KEY] = enabled
        }
    }
}
package com.psspl.healthdatademo.presentation.setting

import androidx.datastore.preferences.core.Preferences
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.psspl.healthdatademo.data.datastore.MyDataStore
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

/***
 * Name : SettingViewModel.kt
 * Author : Prakash Software Pvt Ltd
 * Date : 27 Jun 2025
 * ViewModel responsible for managing app settings like notification preferences.
 * @param dataStore DataStore<Preferences> instance injected via Hilt for persistence.
 **/
@HiltViewModel
class SettingViewModel @Inject constructor(
    private val dataStore: androidx.datastore.core.DataStore<Preferences>
) : ViewModel() {

    // Backing state for notification toggle
    private val _isNotificationEnabled = MutableStateFlow(false)

    // Public immutable state exposed to UI
    val isNotificationEnabled = _isNotificationEnabled.asStateFlow()

    init {
        // Load the notification toggle value from DataStore on ViewModel initialization
        viewModelScope.launch {
            MyDataStore.getNotificationEnabledFlow(dataStore).collect { enabled ->
                _isNotificationEnabled.value =
                    enabled // Update internal state based on DataStore value
            }
        }
    }

    /**
     * Called when user toggles the notification switch.
     * @param enabled The new state of the notification toggle (true = on, false = off)
     */
    fun toggleNotification(enabled: Boolean) {
        viewModelScope.launch {
            MyDataStore.setNotificationEnabled(dataStore, enabled) // Persist change to DataStore
            _isNotificationEnabled.value = enabled // Update internal state for immediate UI update
        }
    }
}
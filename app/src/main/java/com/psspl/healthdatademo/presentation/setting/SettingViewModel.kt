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

@HiltViewModel
class SettingViewModel @Inject constructor(
    private val dataStore: androidx.datastore.core.DataStore<Preferences>
) : ViewModel() {
    private val _isNotificationEnabled = MutableStateFlow(false)
    val isNotificationEnabled = _isNotificationEnabled.asStateFlow()

    init {
        // Load initial values from DataStore
        viewModelScope.launch {
            MyDataStore.getNotificationEnabledFlow(dataStore).collect { enabled ->
                _isNotificationEnabled.value = enabled
            }
        }
    }

    fun toggleNotification(enabled: Boolean) {
        viewModelScope.launch {
            MyDataStore.setNotificationEnabled(dataStore, enabled)
            _isNotificationEnabled.value = enabled
        }
    }
}
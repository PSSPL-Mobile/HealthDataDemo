package com.psspl.healthdatademo.presentation.history

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.psspl.healthdatademo.data.room.HeartRateAlertEntity
import com.psspl.healthdatademo.di.AppDatabase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

/***
 * Name : HistoryViewModel.kt
 * Author : Prakash Software Pvt Ltd
 * Date : 27 Jun 2025
 * Desc : Stores business logic.
 **/
@HiltViewModel
class HistoryViewModel @Inject constructor(
    private val db: AppDatabase // Room database for accessing alert records
) : ViewModel() {

    // Backing state flow holding the list of heart rate alert entries
    private val _heartRateAlerts = MutableStateFlow<List<HeartRateAlertEntity>>(emptyList())

    // Public read-only state flow for UI to observe alert data
    val heartRateAlerts: StateFlow<List<HeartRateAlertEntity>> = _heartRateAlerts

    // Trigger initial load of alerts when ViewModel is created
    init {
        loadHeartRateAlerts()
    }

    /**
     * Loads all stored heart rate alerts from the database
     * and updates the internal state flow.
     */
    private fun loadHeartRateAlerts() {
        viewModelScope.launch(Dispatchers.Main) {
            // Query all heart rate alerts from the local Room database
            _heartRateAlerts.value = db.heartRateAlertDao().getAllHeartRateAlerts()
        }
    }
}
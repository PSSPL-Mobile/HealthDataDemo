package com.psspl.healthdatademo.presentation.activity

import android.os.Build
import androidx.annotation.RequiresApi
import androidx.compose.runtime.State
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import com.psspl.healthdatademo.presentation.navgraph.Route
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.time.delay
import java.time.Duration
import javax.inject.Inject

/***
 * Name : MainViewModel.kt
 * Author : Prakash Software Pvt Ltd
 * Date : 27 Jun 2025
 * Desc : ViewModel for managing the state of the MainActivity, including splash screen and navigation.
 * */
@RequiresApi(Build.VERSION_CODES.O)
@HiltViewModel
class MainViewModel @Inject constructor() : ViewModel() {

    /**
     * Mutable state holding the splash screen condition, initially set to true.
     * Used to control the visibility of the splash screen in the activity.
     */
    var _splashCondition = mutableStateOf(true)

    /**
     * Read-only state exposing the splash condition to the UI, derived from _splashCondition.
     */
    val splashCondition: State<Boolean> = _splashCondition

    /**
     * Mutable state holding the initial navigation destination route, initially set to the app start route.
     * Used to determine the starting screen in the navigation graph.
     */
    private val _startDestination = mutableStateOf(Route.AppStartNavigation.route)

    /**
     * Read-only state exposing the navigation start destination to the UI, derived from _startDestination.
     */
    val startDestination: State<String> = _startDestination

    init {
        /**
         * Initialization block that sets the initial navigation route and controls the splash screen delay.
         * Runs in a blocking coroutine to delay the splash screen dismissal for 2 seconds.
         */
        runBlocking {
            _startDestination.value = Route.AppStartNavigation.route
            val delayDuration = Duration.ofSeconds(2) // Delay duration of 2 seconds
            delay(delayDuration)
            _splashCondition.value = false // Dismiss splash screen after delay
        }
    }
}
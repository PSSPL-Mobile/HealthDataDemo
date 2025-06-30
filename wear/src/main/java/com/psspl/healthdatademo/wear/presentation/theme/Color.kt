package com.psspl.healthdatademo.wear.presentation.theme
import androidx.compose.ui.graphics.Color

/***
 * Name : Color.kt
 * Author : Prakash Software Pvt Ltd
 * Date : 30 Jun 2025
 * Desc : Contains color definitions for the Wear OS version of the HealthDataDemo app, including standard
 * and custom colors used across the theme.
 * */

/**
 * A grey color with an RGB value of (0xFF333333) used for text or background elements.
 */
val grey = Color(0xFF333333)

/**
 * A blue color with an RGB value of (0xFF3F51B5) used as a primary accent or highlight.
 */
val blue = Color(0xFF3F51B5)

/**
 * A green color with an RGB value of (0xFFFFC107) used for positive indicators or highlights.
 */
val green = Color(0xFFFFC107)

/**
 * A white color with an RGB value of (0xFFFFFFFF) used for text or foreground elements.
 */
val white = Color(0xFFFFFFFF)

/**
 * A deep blue color with an RGB value of (0xFF1B2A4E) used as the background for screens.
 * This variable is mutable, allowing potential runtime changes.
 */
var screenBg = Color(0xFF1B2A4E)
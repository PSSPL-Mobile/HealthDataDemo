package com.psspl.healthdatademo.wear.presentation.theme
import androidx.compose.runtime.Composable
import androidx.wear.compose.material.MaterialTheme

/***
 * Name : HealthDataDemoTheme.kt
 * Author : Prakash Software Pvt Ltd
 * Date : 30 Jun 2025
 * Desc : Composable function to apply the theme to the Wear OS app's content.
 * This is a basic implementation that can be customized with colors, typography, and shapes.
 * @param content The composable content to be themed.
 * */
@Composable
fun HealthDataDemoTheme(content: @Composable () -> Unit) {
    /**
     * Empty theme to customize for your app.
     * See: https://developer.android.com/jetpack/compose/designsystems/custom
     */
    MaterialTheme(
        content = content
    )
}
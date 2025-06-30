/***
 * Name : Theme.kt
 * Author : Prakash Software Pvt Ltd
 * Date : 30 Jun 2025
 * Desc : Defines the theme for the HealthDataDemo app, including color schemes and typography.
 * */

/**
 * Provides a theme configuration for the HealthDataDemo app, supporting dynamic and static color schemes
 * based on system settings and Android version.
 */
package com.psspl.healthdatademo.ui.theme

import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalContext

/**
 * Predefined dark color scheme with custom primary, secondary, and tertiary colors.
 */
private val DarkColorScheme = darkColorScheme(
    primary = Purple80,
    secondary = PurpleGrey80,
    tertiary = Pink80
)

/**
 * Predefined light color scheme with custom primary, secondary, and tertiary colors.
 */
private val LightColorScheme = lightColorScheme(
    primary = Purple40,
    secondary = PurpleGrey40,
    tertiary = Pink40
)

/**
 * Composable function to apply the theme to the app's content, supporting dynamic colors and dark/light modes.
 * @param darkTheme Boolean indicating whether to use dark theme, defaults to system setting.
 * @param dynamicColor Boolean enabling dynamic color support, defaults to true and requires Android 12+.
 * @param content The composable content to be themed.
 */
@Composable
fun HealthDataDemoTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    dynamicColor: Boolean = true,
    content: @Composable () -> Unit
) {
    /**
     * Determines the appropriate color scheme based on dynamic color support, Android version, and theme preference.
     * Uses dynamic color schemes on Android 12+ if enabled, falling back to static schemes otherwise.
     */
    val colorScheme = when {
        dynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> {
            val context = LocalContext.current
            if (darkTheme) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
        }
        darkTheme -> DarkColorScheme
        else -> LightColorScheme
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content,
    )
}
package com.example.wanderstate.ui.theme

import android.app.Activity
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.ColorScheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat
import com.example.wanderstate.data.preferences.ThemePreferences

/**
 * Get the appropriate ColorScheme based on selected theme and dark mode.
 *
 * @param themeName The selected color theme: "vintage_green", "ocean_blue", "sunset_orange", or "rosewood_pink"
 * @param isDark Whether to use dark mode colors
 * @return The corresponding ColorScheme
 */
fun getColorSchemeForTheme(themeName: String, isDark: Boolean): ColorScheme {
    return when (themeName) {
        "ocean_blue" -> {
            if (isDark) OceanBlueDarkColorScheme else OceanBlueLightColorScheme
        }
        "sunset_orange" -> {
            if (isDark) SunsetOrangeDarkColorScheme else SunsetOrangeLightColorScheme
        }
        "rosewood_pink" -> {
            if (isDark) RosewoodPinkDarkColorScheme else RosewoodPinkLightColorScheme
        }
        else -> { // "vintage_green" is default
            if (isDark) VintageGreenDarkColorScheme else VintageGreenLightColorScheme
        }
    }
}

/**
 * Main theme composable for WanderState app.
 * Supports dynamic theme switching for both dark/light mode and color themes.
 *
 * @param darkTheme Whether to use dark theme (defaults to system setting)
 * @param content The composable content to wrap with the theme
 */
@Composable
fun WanderStateTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    val context = LocalContext.current
    val themePrefs = remember { ThemePreferences(context) }

    // Observe color theme selection from preferences
    val selectedColorTheme by themePrefs.colorTheme.collectAsState(initial = ThemePreferences.THEME_VINTAGE_GREEN)

    // Get the appropriate color scheme based on selection and dark mode
    val colorScheme = getColorSchemeForTheme(selectedColorTheme, darkTheme)

    val view = LocalView.current
    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as Activity).window
            window.statusBarColor = colorScheme.background.toArgb()
            WindowCompat.getInsetsController(window, view).isAppearanceLightStatusBars = !darkTheme
        }
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = AppTypography,
        shapes = WanderStateShapes,
        content = content
    )
}

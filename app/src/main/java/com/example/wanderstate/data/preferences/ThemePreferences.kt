package com.example.wanderstate.data.preferences

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Theme mode options for the app.
 */
enum class ThemeMode {
    LIGHT,
    DARK,
    SYSTEM;

    companion object {
        fun fromString(value: String): ThemeMode {
            return when (value.uppercase()) {
                "LIGHT" -> LIGHT
                "DARK" -> DARK
                "SYSTEM" -> SYSTEM
                else -> SYSTEM // Default to system
            }
        }
    }
}

/**
 * DataStore extension for theme preferences.
 */
private val Context.themeDataStore: DataStore<Preferences> by preferencesDataStore(name = "theme_preferences")

/**
 * Manages theme preferences using DataStore.
 * Provides persistent storage for user's theme selection.
 */
@Singleton
class ThemePreferences @Inject constructor(
    @ApplicationContext private val context: Context
) {
    private val dataStore = context.themeDataStore

    companion object {
        private val THEME_MODE_KEY = stringPreferencesKey("theme_mode")
        private val COLOR_THEME_KEY = stringPreferencesKey("color_theme")

        // Available color themes
        const val THEME_VINTAGE_GREEN = "vintage_green"
        const val THEME_OCEAN_BLUE = "ocean_blue"
        const val THEME_SUNSET_ORANGE = "sunset_orange"
        const val THEME_ROSEWOOD_PINK = "rosewood_pink"
    }

    /**
     * Flow of the current theme mode.
     * Defaults to SYSTEM if not set.
     */
    val themeMode: Flow<ThemeMode> = dataStore.data
        .map { preferences ->
            val themeModeString = preferences[THEME_MODE_KEY] ?: ThemeMode.SYSTEM.name
            ThemeMode.fromString(themeModeString)
        }

    /**
     * Flow of the current color theme.
     * Defaults to vintage_green if not set.
     */
    val colorTheme: Flow<String> = dataStore.data
        .map { preferences ->
            preferences[COLOR_THEME_KEY] ?: THEME_VINTAGE_GREEN
        }

    /**
     * Save the selected theme mode.
     */
    suspend fun setThemeMode(mode: ThemeMode) {
        dataStore.edit { preferences ->
            preferences[THEME_MODE_KEY] = mode.name
        }
    }

    /**
     * Save the selected color theme.
     * @param themeName One of: "vintage_green", "ocean_blue", or "sunset_orange"
     */
    suspend fun setColorTheme(themeName: String) {
        dataStore.edit { preferences ->
            preferences[COLOR_THEME_KEY] = themeName
        }
    }
}

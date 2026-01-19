package com.example.wanderstate.ui.viewmodel

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.wanderstate.data.dao.PhotoDao
import com.example.wanderstate.data.preferences.ThemeMode
import com.example.wanderstate.data.preferences.ThemePreferences
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.io.File
import javax.inject.Inject
import kotlin.math.roundToInt

/**
 * ViewModel for Settings screen.
 * Provides app statistics and configuration options.
 */
@HiltViewModel
class SettingsViewModel @Inject constructor(
    @ApplicationContext private val context: Context,
    private val photoDao: PhotoDao,
    private val themePreferences: ThemePreferences
) : ViewModel() {

    /**
     * Current theme mode from preferences.
     */
    val currentTheme: StateFlow<ThemeMode> = themePreferences.themeMode
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = ThemeMode.SYSTEM
        )

    /**
     * Current color theme from preferences.
     */
    val currentColorTheme: StateFlow<String> = themePreferences.colorTheme
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = ThemePreferences.THEME_VINTAGE_GREEN
        )

    /**
     * All photos in the database.
     */
    val allPhotos = photoDao.getAll()
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    /**
     * Total number of photos in the database.
     */
    val photoCount: StateFlow<Int> = photoDao.getAll()
        .map { photos -> photos.size }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = 0
        )

    /**
     * Total storage used by photos in human-readable format.
     * Calculates the sum of all photo file sizes.
     */
    val storageUsed: StateFlow<String> = photoDao.getAll()
        .map { photos ->
            val totalBytes = photos.sumOf { photo ->
                try {
                    val file = File(photo.uri)
                    if (file.exists()) file.length() else 0L
                } catch (e: Exception) {
                    0L
                }
            }
            formatBytes(totalBytes)
        }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = "Calculating..."
        )

    /**
     * Format bytes into human-readable format (B, KB, MB, GB).
     */
    private fun formatBytes(bytes: Long): String {
        if (bytes < 1024) return "$bytes B"

        val kb = bytes / 1024.0
        if (kb < 1024) return "${kb.roundToInt()} KB"

        val mb = kb / 1024.0
        if (mb < 1024) return "${String.format("%.1f", mb)} MB"

        val gb = mb / 1024.0
        return "${String.format("%.2f", gb)} GB"
    }

    /**
     * Get app version from BuildConfig (placeholder for now).
     */
    fun getAppVersion(): String {
        return "1.0.0"
    }

    /**
     * Set the app theme mode and persist to preferences.
     */
    fun setTheme(mode: ThemeMode) {
        viewModelScope.launch {
            themePreferences.setThemeMode(mode)
        }
    }

    /**
     * Set the app color theme and persist to preferences.
     * @param themeName One of: "vintage_green", "ocean_blue", or "sunset_orange"
     */
    fun setColorTheme(themeName: String) {
        viewModelScope.launch {
            themePreferences.setColorTheme(themeName)
        }
    }

    /**
     * Get cache size in human-readable format.
     */
    fun getCacheSize(): String {
        val cacheDir = context.cacheDir ?: return "0 B"
        val totalBytes = cacheDir.walkTopDown()
            .filter { it.isFile }
            .map { it.length() }
            .sum()
        return formatBytes(totalBytes)
    }

    /**
     * Clear app cache (temporary files, exported images, etc.).
     * Returns true if successful, false otherwise.
     */
    suspend fun clearCache(): Boolean {
        return try {
            context.cacheDir?.let { cacheDir ->
                cacheDir.listFiles()?.forEach { file ->
                    file.deleteRecursively()
                }
                true
            } ?: false
        } catch (e: Exception) {
            e.printStackTrace()
            false
        }
    }
}

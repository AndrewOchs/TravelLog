package com.example.wanderstate.ui.theme

import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.ui.graphics.Color

// ============================================================================
// VINTAGE GREEN THEME (Default)
// ============================================================================

// Primary Colors - Forest Green
val VintageGreenPrimary = Color(0xFF4D982A)
val VintageGreenSecondary = Color(0xFF7CB877)
val VintageGreenTertiary = Color(0xFF4A7C3A)

// Backgrounds
val VintageGreenLightBg = Color(0xFFF5F1E8)
val VintageGreenLightSurface = Color(0xFFE8DCC4)
val VintageGreenDarkBg = Color(0xFF1C1C1C)
val VintageGreenDarkSurface = Color(0xFF2B2B2B)

// Map colors for Vintage Green
val VintageMapLight = Color(0xFFB2DFAF)   // 1-10 photos
val VintageMapMedium = Color(0xFF7CB877)  // 11-25 photos
val VintageMapDark = Color(0xFF4A7C3A)    // 26+ photos

// ============================================================================
// OCEAN BLUE THEME
// ============================================================================

// Primary Colors - Ocean Blue
val OceanBluePrimary = Color(0xFF2B7A9B)
val OceanBlueSecondary = Color(0xFF5FA3C4)
val OceanBlueTertiary = Color(0xFF1E5873)

// Backgrounds
val OceanBlueLightBg = Color(0xFFF0F4F8)
val OceanBlueLightSurface = Color(0xFFE3EBF0)
val OceanBlueDarkBg = Color(0xFF1A2332)
val OceanBlueDarkSurface = Color(0xFF243447)

// Map colors for Ocean Blue
val OceanMapLight = Color(0xFFA8D5E8)   // 1-10 photos
val OceanMapMedium = Color(0xFF6BAFD1)  // 11-25 photos
val OceanMapDark = Color(0xFF3D8BB0)    // 26+ photos

// ============================================================================
// SUNSET ORANGE THEME
// ============================================================================

// Primary Colors - Sunset Orange
val SunsetOrangePrimary = Color(0xFFD4753E)
val SunsetOrangeSecondary = Color(0xFFE09B6F)
val SunsetOrangeTertiary = Color(0xFFA85A2E)

// Backgrounds
val SunsetOrangeLightBg = Color(0xFFFFF5EC)
val SunsetOrangeLightSurface = Color(0xFFF5E8DC)
val SunsetOrangeDarkBg = Color(0xFF2A1F1A)
val SunsetOrangeDarkSurface = Color(0xFF3D2E24)

// Map colors for Sunset Orange
val SunsetMapLight = Color(0xFFF4C8A8)   // 1-10 photos
val SunsetMapMedium = Color(0xFFE5A878)  // 11-25 photos
val SunsetMapDark = Color(0xFFD17E48)    // 26+ photos

// ============================================================================
// ROSEWOOD PINK THEME
// ============================================================================

// Primary Colors - Rosewood Pink
val RosewoodPinkPrimary = Color(0xFFC75B7A)
val RosewoodPinkSecondary = Color(0xFFD98BA6)
val RosewoodPinkTertiary = Color(0xFF9D4562)

// Backgrounds
val RosewoodPinkLightBg = Color(0xFFFFF0F5)
val RosewoodPinkLightSurface = Color(0xFFF8E8ED)
val RosewoodPinkDarkBg = Color(0xFF2A1A20)
val RosewoodPinkDarkSurface = Color(0xFF3D2830)

// Map colors for Rosewood Pink
val RosewoodMapLight = Color(0xFFF4C4D4)   // 1-10 photos
val RosewoodMapMedium = Color(0xFFE89BB0)  // 11-25 photos
val RosewoodMapDark = Color(0xFFD66B8A)    // 26+ photos

// ============================================================================
// COMMON COLORS
// ============================================================================

// Error Colors
val ErrorLight = Color(0xFFBA1A1A)
val ErrorDark = Color(0xFFFFB4AB)
val OnErrorLight = Color(0xFFFFFFFF)
val OnErrorDark = Color(0xFF690005)

// Outline Colors
val OutlineLight = Color(0xFF79747E)
val OutlineDark = Color(0xFF938F99)

// ============================================================================
// COLOR SCHEMES
// ============================================================================

// Vintage Green Light Scheme
val VintageGreenLightColorScheme = lightColorScheme(
    primary = VintageGreenPrimary,
    onPrimary = Color.White,
    primaryContainer = Color(0xFFB8E6A8),
    onPrimaryContainer = Color(0xFF0E2F00),

    secondary = VintageGreenSecondary,
    onSecondary = Color.White,
    secondaryContainer = Color(0xFFD4EFD0),
    onSecondaryContainer = Color(0xFF1E3A1B),

    tertiary = VintageMapMedium,          // Map medium green (11-25 photos)
    onTertiary = Color.White,
    tertiaryContainer = VintageMapLight,  // Map light green (1-10 photos)
    onTertiaryContainer = Color(0xFF152E10),

    background = VintageGreenLightBg,
    onBackground = Color(0xFF1C1B1F),

    surface = VintageGreenLightSurface,
    onSurface = Color(0xFF1C1B1F),
    surfaceVariant = Color(0xFFE2E1E3),
    onSurfaceVariant = Color(0xFF45464F),

    error = ErrorLight,
    onError = OnErrorLight,
    errorContainer = Color(0xFFF9DEDC),
    onErrorContainer = Color(0xFF410E0B),

    outline = OutlineLight,
    outlineVariant = Color(0xFFCAC4D0),

    inverseSurface = Color(0xFF313033),
    inverseOnSurface = Color(0xFFF4EFF4),
    inversePrimary = Color(0xFF9CD789)
)

// Vintage Green Dark Scheme
val VintageGreenDarkColorScheme = darkColorScheme(
    primary = VintageMapDark,             // Map dark green (26+ photos) - darkest for consistency
    onPrimary = Color.White,
    primaryContainer = Color(0xFF2D6B0A),
    onPrimaryContainer = Color(0xFFB8E6A8),

    secondary = Color(0xFFB8D0B4),
    onSecondary = Color(0xFF243B23),
    secondaryContainer = Color(0xFF3A5138),
    onSecondaryContainer = Color(0xFFD4EFD0),

    tertiary = VintageMapMedium,          // Map medium green (11-25 photos)
    onTertiary = Color.White,
    tertiaryContainer = VintageMapLight,  // Map light green (1-10 photos)
    onTertiaryContainer = Color(0xFF0F3A08),

    background = VintageGreenDarkBg,
    onBackground = Color(0xFFE6E1E5),

    surface = VintageGreenDarkSurface,
    onSurface = Color(0xFFE6E1E5),
    surfaceVariant = Color(0xFF45464F),
    onSurfaceVariant = Color(0xFFC6C5D0),

    error = ErrorDark,
    onError = OnErrorDark,
    errorContainer = Color(0xFF93000A),
    onErrorContainer = Color(0xFFF9DEDC),

    outline = OutlineDark,
    outlineVariant = Color(0xFF45464F),

    inverseSurface = Color(0xFFE6E1E5),
    inverseOnSurface = Color(0xFF313033),
    inversePrimary = VintageGreenPrimary
)

// Ocean Blue Light Scheme
val OceanBlueLightColorScheme = lightColorScheme(
    primary = OceanBluePrimary,
    onPrimary = Color.White,
    primaryContainer = Color(0xFFB8D8EB),
    onPrimaryContainer = Color(0xFF001F2A),

    secondary = OceanBlueSecondary,
    onSecondary = Color.White,
    secondaryContainer = Color(0xFFD4E9F5),
    onSecondaryContainer = Color(0xFF1A3A48),

    tertiary = OceanMapMedium,          // Map medium blue (11-25 photos)
    onTertiary = Color.White,
    tertiaryContainer = OceanMapLight,  // Map light blue (1-10 photos)
    onTertiaryContainer = Color(0xFF0A2530),

    background = OceanBlueLightBg,
    onBackground = Color(0xFF1A1C1E),

    surface = OceanBlueLightSurface,
    onSurface = Color(0xFF1A1C1E),
    surfaceVariant = Color(0xFFDDE3EA),
    onSurfaceVariant = Color(0xFF41484D),

    error = ErrorLight,
    onError = OnErrorLight,
    errorContainer = Color(0xFFF9DEDC),
    onErrorContainer = Color(0xFF410E0B),

    outline = OutlineLight,
    outlineVariant = Color(0xFFC1C7CE),

    inverseSurface = Color(0xFF2F3033),
    inverseOnSurface = Color(0xFFF0F0F3),
    inversePrimary = Color(0xFF88C9E8)
)

// Ocean Blue Dark Scheme
val OceanBlueDarkColorScheme = darkColorScheme(
    primary = OceanMapDark,             // Map dark blue (26+ photos) - darkest for consistency
    onPrimary = Color.White,
    primaryContainer = Color(0xFF0A5168),
    onPrimaryContainer = Color(0xFFB8D8EB),

    secondary = Color(0xFFB8D4E4),
    onSecondary = Color(0xFF233B4A),
    secondaryContainer = Color(0xFF3A5261),
    onSecondaryContainer = Color(0xFFD4E9F5),

    tertiary = OceanMapMedium,          // Map medium blue (11-25 photos)
    onTertiary = Color.White,
    tertiaryContainer = OceanMapLight,  // Map light blue (1-10 photos)
    onTertiaryContainer = Color(0xFF003B4F),

    background = OceanBlueDarkBg,
    onBackground = Color(0xFFE1E2E5),

    surface = OceanBlueDarkSurface,
    onSurface = Color(0xFFE1E2E5),
    surfaceVariant = Color(0xFF41484D),
    onSurfaceVariant = Color(0xFFC1C7CE),

    error = ErrorDark,
    onError = OnErrorDark,
    errorContainer = Color(0xFF93000A),
    onErrorContainer = Color(0xFFF9DEDC),

    outline = OutlineDark,
    outlineVariant = Color(0xFF41484D),

    inverseSurface = Color(0xFFE1E2E5),
    inverseOnSurface = Color(0xFF2F3033),
    inversePrimary = OceanBluePrimary
)

// Sunset Orange Light Scheme
val SunsetOrangeLightColorScheme = lightColorScheme(
    primary = SunsetOrangePrimary,
    onPrimary = Color.White,
    primaryContainer = Color(0xFFFCDCC8),
    onPrimaryContainer = Color(0xFF2D1508),

    secondary = SunsetOrangeSecondary,
    onSecondary = Color.White,
    secondaryContainer = Color(0xFFFEE8D5),
    onSecondaryContainer = Color(0xFF3A2418),

    tertiary = SunsetMapMedium,          // Map medium orange (11-25 photos)
    onTertiary = Color.White,
    tertiaryContainer = SunsetMapLight,  // Map light orange (1-10 photos)
    onTertiaryContainer = Color(0xFF281408),

    background = SunsetOrangeLightBg,
    onBackground = Color(0xFF201B16),

    surface = SunsetOrangeLightSurface,
    onSurface = Color(0xFF201B16),
    surfaceVariant = Color(0xFFF0E0D0),
    onSurfaceVariant = Color(0xFF504539),

    error = ErrorLight,
    onError = OnErrorLight,
    errorContainer = Color(0xFFF9DEDC),
    onErrorContainer = Color(0xFF410E0B),

    outline = OutlineLight,
    outlineVariant = Color(0xFFD3C4B4),

    inverseSurface = Color(0xFF36302A),
    inverseOnSurface = Color(0xFFF8EFE7),
    inversePrimary = Color(0xFFFFB68D)
)

// Sunset Orange Dark Scheme
val SunsetOrangeDarkColorScheme = darkColorScheme(
    primary = SunsetMapDark,             // Map dark orange (26+ photos) - darkest for consistency
    onPrimary = Color.White,
    primaryContainer = Color(0xFF9A5A2E),
    onPrimaryContainer = Color(0xFFFCDCC8),

    secondary = Color(0xFFE5C0A5),
    onSecondary = Color(0xFF432C1D),
    secondaryContainer = Color(0xFF5C4132),
    onSecondaryContainer = Color(0xFFFEE8D5),

    tertiary = SunsetMapMedium,          // Map medium orange (11-25 photos)
    onTertiary = Color.White,
    tertiaryContainer = SunsetMapLight,  // Map light orange (1-10 photos)
    onTertiaryContainer = Color(0xFF3E2716),

    background = SunsetOrangeDarkBg,
    onBackground = Color(0xFFEBE0D9),

    surface = SunsetOrangeDarkSurface,
    onSurface = Color(0xFFEBE0D9),
    surfaceVariant = Color(0xFF504539),
    onSurfaceVariant = Color(0xFFD3C4B4),

    error = ErrorDark,
    onError = OnErrorDark,
    errorContainer = Color(0xFF93000A),
    onErrorContainer = Color(0xFFF9DEDC),

    outline = OutlineDark,
    outlineVariant = Color(0xFF504539),

    inverseSurface = Color(0xFFEBE0D9),
    inverseOnSurface = Color(0xFF36302A),
    inversePrimary = SunsetOrangePrimary
)

// Rosewood Pink Light Scheme
val RosewoodPinkLightColorScheme = lightColorScheme(
    primary = RosewoodPinkPrimary,
    onPrimary = Color.White,
    primaryContainer = Color(0xFFFFDAE3),
    onPrimaryContainer = Color(0xFF3E0E1C),

    secondary = RosewoodPinkSecondary,
    onSecondary = Color.White,
    secondaryContainer = Color(0xFFFCE4ED),
    onSecondaryContainer = Color(0xFF3A1F2C),

    tertiary = RosewoodMapMedium,          // Map medium pink (11-25 photos)
    onTertiary = Color.White,
    tertiaryContainer = RosewoodMapLight,  // Map light pink (1-10 photos)
    onTertiaryContainer = Color(0xFF31001D),

    background = RosewoodPinkLightBg,
    onBackground = Color(0xFF201A1B),

    surface = RosewoodPinkLightSurface,
    onSurface = Color(0xFF201A1B),
    surfaceVariant = Color(0xFFF4DDE4),
    onSurfaceVariant = Color(0xFF524345),

    error = ErrorLight,
    onError = OnErrorLight,
    errorContainer = Color(0xFFF9DEDC),
    onErrorContainer = Color(0xFF410E0B),

    outline = OutlineLight,
    outlineVariant = Color(0xFFD7C2C7),

    inverseSurface = Color(0xFF362F30),
    inverseOnSurface = Color(0xFFFCEDEE),
    inversePrimary = Color(0xFFFFB0C8)
)

// Rosewood Pink Dark Scheme
val RosewoodPinkDarkColorScheme = darkColorScheme(
    primary = RosewoodMapDark,             // Map dark pink (26+ photos) - darkest for consistency
    onPrimary = Color.White,
    primaryContainer = Color(0xFF9E3454),
    onPrimaryContainer = Color(0xFFFFDAE3),

    secondary = Color(0xFFE6BDD0),
    onSecondary = Color(0xFF43263A),
    secondaryContainer = Color(0xFF5C3D50),
    onSecondaryContainer = Color(0xFFFCE4ED),

    tertiary = RosewoodMapMedium,          // Map medium pink (11-25 photos)
    onTertiary = Color.White,
    tertiaryContainer = RosewoodMapLight,  // Map light pink (1-10 photos)
    onTertiaryContainer = Color(0xFF48002F),

    background = RosewoodPinkDarkBg,
    onBackground = Color(0xFFECE0E1),

    surface = RosewoodPinkDarkSurface,
    onSurface = Color(0xFFECE0E1),
    surfaceVariant = Color(0xFF524345),
    onSurfaceVariant = Color(0xFFD7C2C7),

    error = ErrorDark,
    onError = OnErrorDark,
    errorContainer = Color(0xFF93000A),
    onErrorContainer = Color(0xFFF9DEDC),

    outline = OutlineDark,
    outlineVariant = Color(0xFF524345),

    inverseSurface = Color(0xFFECE0E1),
    inverseOnSurface = Color(0xFF362F30),
    inversePrimary = RosewoodPinkPrimary
)

// ============================================================================
// MAP COLOR HELPER DATA CLASS
// ============================================================================

/**
 * Data class to hold map visualization colors for each theme
 */
data class MapColors(
    val lightGreen: Color,   // 1-10 photos
    val mediumGreen: Color,  // 11-25 photos
    val darkGreen: Color     // 26+ photos
)

/**
 * Get map colors for a specific theme
 */
fun getMapColors(themeName: String): MapColors {
    return when (themeName) {
        "ocean_blue" -> MapColors(
            lightGreen = OceanMapLight,
            mediumGreen = OceanMapMedium,
            darkGreen = OceanMapDark
        )
        "sunset_orange" -> MapColors(
            lightGreen = SunsetMapLight,
            mediumGreen = SunsetMapMedium,
            darkGreen = SunsetMapDark
        )
        "rosewood_pink" -> MapColors(
            lightGreen = RosewoodMapLight,
            mediumGreen = RosewoodMapMedium,
            darkGreen = RosewoodMapDark
        )
        else -> MapColors( // "vintage_green" is default
            lightGreen = VintageMapLight,
            mediumGreen = VintageMapMedium,
            darkGreen = VintageMapDark
        )
    }
}

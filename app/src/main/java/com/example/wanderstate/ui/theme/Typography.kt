package com.example.wanderstate.ui.theme

import androidx.compose.material3.Typography
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp
import com.example.wanderstate.R

/**
 * WanderState Typography System - Vintage Nature/Travel Theme
 *
 * Display/Headline/Title: Playfair Display - Elegant serif for headers
 * Body Text: Lato - Clean, readable sans-serif
 * Handwritten Accents: Amatic SC - Casual handwritten style for captions
 */

// Define font families
val PlayfairDisplay = FontFamily(
    Font(R.font.playfair_display)
)

val PlayfairDisplayBold = FontFamily(
    Font(R.font.playfair_display_bold)
)

val PlayfairDisplayExtraBold = FontFamily(
    Font(R.font.playfair_display_extrabold)
)

val LatoRegular = FontFamily(
    Font(R.font.lato)
)

val LatoBold = FontFamily(
    Font(R.font.lato_bold)
)

val HandwrittenFont = FontFamily(
    Font(R.font.amatic_sc_bold)
)

val OswaldBold = FontFamily(
    Font(R.font.oswald)
)

val CaveatRegular = FontFamily(
    Font(R.font.caveat)
)

val CaveatBold = FontFamily(
    Font(R.font.caveat_bold)
)

// Material 3 Typography Scale - Vintage Nature/Travel Theme
val AppTypography = Typography(
    // Display styles - Map title, hero sections
    displayLarge = TextStyle(
        fontFamily = PlayfairDisplayExtraBold,
        fontWeight = FontWeight.ExtraBold,
        fontSize = 32.sp,
        lineHeight = 40.sp,
        letterSpacing = 1.sp
    ),
    displayMedium = TextStyle(
        fontFamily = PlayfairDisplayExtraBold,
        fontWeight = FontWeight.ExtraBold,
        fontSize = 28.sp,
        lineHeight = 36.sp,
        letterSpacing = 0.8.sp
    ),
    displaySmall = TextStyle(
        fontFamily = PlayfairDisplayExtraBold,
        fontWeight = FontWeight.ExtraBold,
        fontSize = 24.sp,
        lineHeight = 32.sp,
        letterSpacing = 0.5.sp
    ),

    // Headline styles - State names, screen titles
    headlineLarge = TextStyle(
        fontFamily = PlayfairDisplayBold,
        fontWeight = FontWeight.Bold,
        fontSize = 28.sp,
        lineHeight = 36.sp,
        letterSpacing = 0.5.sp
    ),
    headlineMedium = TextStyle(
        fontFamily = PlayfairDisplayBold,
        fontWeight = FontWeight.Bold,
        fontSize = 24.sp,
        lineHeight = 32.sp,
        letterSpacing = 0.sp
    ),
    headlineSmall = TextStyle(
        fontFamily = PlayfairDisplayBold,
        fontWeight = FontWeight.Bold,
        fontSize = 22.sp,
        lineHeight = 28.sp,
        letterSpacing = 0.sp
    ),

    // Title styles - Section headers, card titles
    titleLarge = TextStyle(
        fontFamily = PlayfairDisplay,
        fontWeight = FontWeight.Normal,
        fontSize = 22.sp,
        lineHeight = 28.sp,
        letterSpacing = 0.sp
    ),
    titleMedium = TextStyle(
        fontFamily = PlayfairDisplay,
        fontWeight = FontWeight.Normal,
        fontSize = 16.sp,
        lineHeight = 24.sp,
        letterSpacing = 0.15.sp
    ),
    titleSmall = TextStyle(
        fontFamily = PlayfairDisplay,
        fontWeight = FontWeight.Normal,
        fontSize = 14.sp,
        lineHeight = 20.sp,
        letterSpacing = 0.1.sp
    ),

    // Body styles - Journal entries, descriptions, main content
    bodyLarge = TextStyle(
        fontFamily = LatoRegular,
        fontWeight = FontWeight.Normal,
        fontSize = 16.sp,
        lineHeight = 24.sp,
        letterSpacing = 0.5.sp
    ),
    bodyMedium = TextStyle(
        fontFamily = LatoRegular,
        fontWeight = FontWeight.Normal,
        fontSize = 14.sp,
        lineHeight = 20.sp,
        letterSpacing = 0.25.sp
    ),
    bodySmall = TextStyle(
        fontFamily = LatoRegular,
        fontWeight = FontWeight.Normal,
        fontSize = 12.sp,
        lineHeight = 16.sp,
        letterSpacing = 0.4.sp
    ),

    // Label styles - Buttons, tabs
    labelLarge = TextStyle(
        fontFamily = LatoBold,
        fontWeight = FontWeight.Bold,
        fontSize = 14.sp,
        lineHeight = 20.sp,
        letterSpacing = 0.5.sp
    ),
    labelMedium = TextStyle(
        fontFamily = LatoBold,
        fontWeight = FontWeight.Bold,
        fontSize = 12.sp,
        lineHeight = 16.sp,
        letterSpacing = 0.3.sp
    ),
    labelSmall = TextStyle(
        fontFamily = LatoBold,
        fontWeight = FontWeight.Bold,
        fontSize = 11.sp,
        lineHeight = 16.sp,
        letterSpacing = 0.3.sp
    )
)

// Custom handwritten style for photo captions and special text
val HandwrittenStyle = TextStyle(
    fontFamily = HandwrittenFont,
    fontWeight = FontWeight.Bold,
    fontSize = 18.sp,
    lineHeight = 24.sp,
    letterSpacing = 0.sp
)

// Custom stats number style for displaying numeric statistics
val StatsNumberStyle = TextStyle(
    fontFamily = OswaldBold,
    fontWeight = FontWeight.Bold,
    fontSize = 48.sp,
    lineHeight = 56.sp,
    letterSpacing = 0.sp
)

// Custom journal entry style for export photo journal text
val JournalEntryStyle = TextStyle(
    fontFamily = CaveatRegular,
    fontWeight = FontWeight.Normal,
    fontSize = 18.sp,
    lineHeight = 24.sp,
    letterSpacing = 0.sp
)

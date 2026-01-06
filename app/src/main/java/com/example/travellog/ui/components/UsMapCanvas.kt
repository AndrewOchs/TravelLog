package com.example.travellog.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.*
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.*
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.drawIntoCanvas
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.drawText
import androidx.compose.ui.text.rememberTextMeasurer
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.travellog.data.models.UsState
import kotlin.math.sqrt

/**
 * Interactive US map canvas with improved visuals and travel journal aesthetic.
 *
 * @param states List of US states with photo counts
 * @param onStateClick Callback when a state is clicked
 * @param modifier Optional modifier
 */
@Composable
fun UsMapCanvas(
    states: List<UsState>,
    onStateClick: (UsState) -> Unit,
    modifier: Modifier = Modifier
) {
    val textMeasurer = rememberTextMeasurer()
    val primaryColor = MaterialTheme.colorScheme.primary
    val secondaryColor = MaterialTheme.colorScheme.secondary
    val tertiaryColor = MaterialTheme.colorScheme.tertiary
    val backgroundColor = MaterialTheme.colorScheme.background
    val onBackgroundColor = MaterialTheme.colorScheme.onBackground

    // Warm earth tones
    val warmTaupe = Color(0xFFD4C4B0)
    val lightGreen = Color(0xFF8FB996)
    val mediumGreen = Color(0xFF5A8F69)
    val darkGreen = primaryColor

    var selectedState by remember { mutableStateOf<UsState?>(null) }

    Canvas(
        modifier = modifier
            .fillMaxSize()
            .pointerInput(states) {
                detectTapGestures { offset ->
                    val canvasWidth = size.width.toFloat()
                    val canvasHeight = size.height.toFloat()

                    states.forEach { state ->
                        val stateX = state.centerX * canvasWidth
                        val stateY = state.centerY * canvasHeight
                        val baseRadius = 22f
                        val stateRadius = baseRadius * state.size.scale

                        val distance = sqrt(
                            (offset.x - stateX) * (offset.x - stateX) +
                                    (offset.y - stateY) * (offset.y - stateY)
                        )

                        if (distance <= stateRadius) {
                            selectedState = state
                            onStateClick(state)
                        }
                    }
                }
            }
    ) {
        val canvasWidth = size.width
        val canvasHeight = size.height

        // Draw subtle background texture (topographic-inspired lines)
        drawBackgroundTexture()

        // Draw simplified US border outline
        drawUsBorder(canvasWidth, canvasHeight, warmTaupe.copy(alpha = 0.4f))

        // Draw inset boxes for Alaska and Hawaii
        drawInsetBox(0.02f, 0.82f, 0.15f, 0.16f, canvasWidth, canvasHeight, warmTaupe)
        drawInsetBox(0.17f, 0.88f, 0.12f, 0.10f, canvasWidth, canvasHeight, warmTaupe)

        // Find max photo count for color scaling
        val maxPhotoCount = states.maxOfOrNull { it.photoCount } ?: 1

        // Draw each state (states without photos first, then with photos for layering)
        val statesWithoutPhotos = states.filter { it.photoCount == 0 }
        val statesWithPhotos = states.filter { it.photoCount > 0 }

        (statesWithoutPhotos + statesWithPhotos).forEach { state ->
            drawState(
                state = state,
                canvasWidth = canvasWidth,
                canvasHeight = canvasHeight,
                maxPhotoCount = maxPhotoCount,
                isSelected = selectedState == state,
                warmTaupe = warmTaupe,
                lightGreen = lightGreen,
                mediumGreen = mediumGreen,
                darkGreen = darkGreen,
                secondaryColor = secondaryColor,
                onBackgroundColor = onBackgroundColor,
                backgroundColor = backgroundColor,
                textMeasurer = textMeasurer
            )
        }

        // Draw subtle compass rose in bottom right
        drawCompassRose(canvasWidth, canvasHeight, warmTaupe)
    }
}

/**
 * Draw subtle background texture with topographic-inspired lines.
 */
private fun DrawScope.drawBackgroundTexture() {
    val lineColor = Color(0xFFE8DED0).copy(alpha = 0.3f)
    val lineSpacing = 60f

    for (i in 0 until (size.width / lineSpacing).toInt()) {
        val x = i * lineSpacing
        drawLine(
            color = lineColor,
            start = Offset(x, 0f),
            end = Offset(x, size.height),
            strokeWidth = 0.5f
        )
    }

    for (i in 0 until (size.height / lineSpacing).toInt()) {
        val y = i * lineSpacing
        drawLine(
            color = lineColor,
            start = Offset(0f, y),
            end = Offset(size.width, y),
            strokeWidth = 0.5f
        )
    }
}

/**
 * Draw simplified US border outline.
 */
private fun DrawScope.drawUsBorder(
    canvasWidth: Float,
    canvasHeight: Float,
    borderColor: Color
) {
    val path = Path().apply {
        // Simplified continental US border (very approximate)
        moveTo(0.05f * canvasWidth, 0.30f * canvasHeight) // West coast start

        // West coast
        lineTo(0.06f * canvasWidth, 0.12f * canvasHeight) // WA
        lineTo(0.12f * canvasWidth, 0.12f * canvasHeight) // Northern border start

        // Northern border
        lineTo(0.32f * canvasWidth, 0.12f * canvasHeight) // MT/ND area
        lineTo(0.50f * canvasWidth, 0.16f * canvasHeight) // MN bump
        lineTo(0.64f * canvasWidth, 0.22f * canvasHeight) // Great Lakes
        lineTo(0.90f * canvasWidth, 0.18f * canvasHeight) // Maine

        // East coast
        lineTo(0.88f * canvasWidth, 0.38f * canvasHeight) // New England
        lineTo(0.82f * canvasWidth, 0.48f * canvasHeight) // Mid-Atlantic
        lineTo(0.78f * canvasWidth, 0.78f * canvasHeight) // Florida

        // Gulf coast
        lineTo(0.52f * canvasWidth, 0.72f * canvasHeight) // Louisiana
        lineTo(0.28f * canvasWidth, 0.75f * canvasHeight) // Texas coast

        // Southern border
        lineTo(0.18f * canvasWidth, 0.60f * canvasHeight) // Arizona
        lineTo(0.08f * canvasWidth, 0.50f * canvasHeight) // California

        close()
    }

    drawPath(
        path = path,
        color = borderColor,
        style = Stroke(width = 2f, pathEffect = PathEffect.dashPathEffect(floatArrayOf(10f, 5f)))
    )
}

/**
 * Draw inset box for Alaska or Hawaii.
 */
private fun DrawScope.drawInsetBox(
    x: Float, y: Float, width: Float, height: Float,
    canvasWidth: Float, canvasHeight: Float,
    borderColor: Color
) {
    val left = x * canvasWidth
    val top = y * canvasHeight
    val boxWidth = width * canvasWidth
    val boxHeight = height * canvasHeight

    drawRoundRect(
        color = borderColor.copy(alpha = 0.2f),
        topLeft = Offset(left, top),
        size = Size(boxWidth, boxHeight),
        cornerRadius = androidx.compose.ui.geometry.CornerRadius(8f, 8f),
        style = Stroke(width = 1.5f, pathEffect = PathEffect.dashPathEffect(floatArrayOf(5f, 3f)))
    )
}

/**
 * Draw a single state with all visual effects.
 */
private fun DrawScope.drawState(
    state: UsState,
    canvasWidth: Float,
    canvasHeight: Float,
    maxPhotoCount: Int,
    isSelected: Boolean,
    warmTaupe: Color,
    lightGreen: Color,
    mediumGreen: Color,
    darkGreen: Color,
    secondaryColor: Color,
    onBackgroundColor: Color,
    backgroundColor: Color,
    textMeasurer: androidx.compose.ui.text.TextMeasurer
) {
    val centerX = state.centerX * canvasWidth
    val centerY = state.centerY * canvasHeight
    val baseRadius = 22f
    val stateRadius = baseRadius * state.size.scale

    // Determine state color based on photo count
    val stateColor = when {
        state.photoCount == 0 -> warmTaupe // Warm taupe for no photos
        else -> {
            // Gradient from light green to dark green
            val ratio = state.photoCount.toFloat() / maxPhotoCount.toFloat()
            when {
                ratio < 0.33f -> lightGreen
                ratio < 0.67f -> mediumGreen
                else -> darkGreen
            }
        }
    }

    // Draw glow effect for states with photos
    if (state.photoCount > 0) {
        drawCircle(
            color = stateColor.copy(alpha = 0.15f),
            radius = stateRadius + 8f,
            center = Offset(centerX, centerY),
            blendMode = BlendMode.Plus
        )
        drawCircle(
            color = stateColor.copy(alpha = 0.1f),
            radius = stateRadius + 14f,
            center = Offset(centerX, centerY),
            blendMode = BlendMode.Plus
        )
    }

    // Draw drop shadow
    drawCircle(
        color = Color.Black.copy(alpha = 0.15f),
        radius = stateRadius,
        center = Offset(centerX + 2f, centerY + 3f)
    )

    // Draw state circle with soft edges
    drawCircle(
        color = stateColor,
        radius = stateRadius,
        center = Offset(centerX, centerY)
    )

    // Add subtle highlight
    drawCircle(
        color = Color.White.copy(alpha = 0.1f),
        radius = stateRadius * 0.6f,
        center = Offset(centerX - stateRadius * 0.2f, centerY - stateRadius * 0.2f)
    )

    // Draw border for selected state
    if (isSelected) {
        drawCircle(
            color = secondaryColor,
            radius = stateRadius + 2f,
            center = Offset(centerX, centerY),
            style = Stroke(width = 3f)
        )
    }

    // Draw state abbreviation
    val textColor = if (state.photoCount == 0) {
        onBackgroundColor.copy(alpha = 0.7f)
    } else {
        Color.White
    }

    val fontSize = when (state.size) {
        com.example.travellog.data.models.StateSize.TINY -> 9.sp
        com.example.travellog.data.models.StateSize.SMALL -> 10.sp
        else -> 11.sp
    }

    val textStyle = TextStyle(
        color = textColor,
        fontSize = fontSize,
        fontWeight = androidx.compose.ui.text.font.FontWeight.SemiBold
    )

    val textLayoutResult = textMeasurer.measure(
        text = state.code,
        style = textStyle
    )

    drawText(
        textLayoutResult = textLayoutResult,
        topLeft = Offset(
            centerX - textLayoutResult.size.width / 2,
            centerY - textLayoutResult.size.height / 2
        )
    )

    // Draw photo count badge for states with photos
    if (state.photoCount > 0) {
        val badgeRadius = 11f
        val badgeX = centerX + stateRadius - badgeRadius
        val badgeY = centerY - stateRadius + badgeRadius

        // Badge shadow
        drawCircle(
            color = Color.Black.copy(alpha = 0.2f),
            radius = badgeRadius,
            center = Offset(badgeX + 1f, badgeY + 2f)
        )

        // Badge background
        drawCircle(
            color = secondaryColor,
            radius = badgeRadius,
            center = Offset(badgeX, badgeY)
        )

        // Badge border
        drawCircle(
            color = backgroundColor,
            radius = badgeRadius,
            center = Offset(badgeX, badgeY),
            style = Stroke(width = 1.5f)
        )

        // Badge count text
        val countText = if (state.photoCount > 99) "99+" else state.photoCount.toString()
        val badgeTextStyle = TextStyle(
            color = Color.White,
            fontSize = 8.sp,
            fontWeight = androidx.compose.ui.text.font.FontWeight.Bold
        )

        val badgeTextLayoutResult = textMeasurer.measure(
            text = countText,
            style = badgeTextStyle
        )

        drawText(
            textLayoutResult = badgeTextLayoutResult,
            topLeft = Offset(
                badgeX - badgeTextLayoutResult.size.width / 2,
                badgeY - badgeTextLayoutResult.size.height / 2
            )
        )
    }
}

/**
 * Draw subtle compass rose in bottom right corner.
 */
private fun DrawScope.drawCompassRose(
    canvasWidth: Float,
    canvasHeight: Float,
    color: Color
) {
    val centerX = canvasWidth * 0.92f
    val centerY = canvasHeight * 0.90f
    val radius = 20f

    // Draw N, S, E, W points
    val compassColor = color.copy(alpha = 0.4f)

    // North arrow
    val northPath = Path().apply {
        moveTo(centerX, centerY - radius)
        lineTo(centerX - 4f, centerY - radius + 10f)
        lineTo(centerX + 4f, centerY - radius + 10f)
        close()
    }
    drawPath(northPath, color = compassColor)

    // Small circle in center
    drawCircle(
        color = compassColor,
        radius = 3f,
        center = Offset(centerX, centerY)
    )
}

package com.example.wanderstate.ui.components

import android.content.Context
import android.graphics.Matrix
import android.graphics.Paint
import android.graphics.Path
import android.graphics.PathMeasure
import android.graphics.RectF
import android.graphics.Region
import android.util.Log
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.gestures.detectTransformGestures
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.*
import androidx.compose.ui.graphics.drawscope.drawIntoCanvas
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalContext
import com.caverock.androidsvg.SVG
import com.andrewochs.wanderstate.R
import com.example.wanderstate.data.models.UsState

private const val TAG = "SvgUsMapRenderer"

/**
 * Data class representing a single state path from the SVG with its metadata.
 */
data class StatePath(
    val code: String,
    val path: Path,
    val bounds: RectF,
    val region: Region
)

/**
 * Parses the US map SVG and extracts individual state paths with their IDs.
 *
 * @param context Android context for accessing resources
 * @return Map of state code to StatePath data
 */
fun parseUsMapSvg(context: Context): Map<String, StatePath> {
    val statePathsMap = mutableMapOf<String, StatePath>()

    try {
        // Load SVG from raw resources
        val inputStream = context.resources.openRawResource(R.raw.us_map)
        val svg = SVG.getFromInputStream(inputStream)

        // AndroidSVG doesn't provide direct access to individual path elements
        // We'll need to parse the SVG XML manually to extract state paths
        inputStream.close()

        // Reopen stream for XML parsing
        val xmlInputStream = context.resources.openRawResource(R.raw.us_map)
        val xmlContent = xmlInputStream.bufferedReader().use { it.readText() }
        xmlInputStream.close()

        // Parse state paths from XML
        parseStatePaths(xmlContent, statePathsMap)

    } catch (e: Exception) {
        Log.e(TAG, "Error parsing SVG", e)
    }

    return statePathsMap
}

/**
 * Parses state path elements from SVG XML content.
 */
private fun parseStatePaths(xmlContent: String, statePathsMap: MutableMap<String, StatePath>) {
    // Regex to find <path> elements with id attribute and class="state"
    // Note: We use <path> only here, not <g>, because Michigan is handled separately
    val pathPattern = """<path[^>]*id="([A-Z]{2})"[^>]*class="state"[^>]*d="([^"]+)"""".toRegex()

    pathPattern.findAll(xmlContent).forEach { matchResult ->
        val stateCode = matchResult.groupValues[1]
        val pathData = matchResult.groupValues[2]

        // Skip Michigan as it's handled separately in parseMichiganPaths
        if (stateCode == "MI") {
            return@forEach
        }

        try {
            // Convert SVG path data to Android Path
            val path = parseSvgPath(pathData)

            // Calculate bounds
            val bounds = RectF()
            path.computeBounds(bounds, true)

            // Create region for hit testing
            val region = Region()
            val clipRegion = Region(
                bounds.left.toInt(),
                bounds.top.toInt(),
                bounds.right.toInt(),
                bounds.bottom.toInt()
            )
            region.setPath(path, clipRegion)

            statePathsMap[stateCode] = StatePath(
                code = stateCode,
                path = path,
                bounds = bounds,
                region = region
            )
        } catch (e: Exception) {
            Log.w(TAG, "Failed to parse path for $stateCode", e)
        }
    }

    // Handle Michigan separately as it's a <g> group with nested paths
    parseMichiganPaths(xmlContent, statePathsMap)
}

/**
 * Special handling for Michigan which is a group element with multiple paths.
 */
private fun parseMichiganPaths(xmlContent: String, statePathsMap: MutableMap<String, StatePath>) {
    val miGroupPattern = """<g[^>]*id="MI"[^>]*class="state"[^>]*>(.*?)</g>""".toRegex(RegexOption.DOT_MATCHES_ALL)
    val miMatch = miGroupPattern.find(xmlContent)

    if (miMatch != null) {
        val groupContent = miMatch.groupValues[1]
        val pathDataPattern = """\bd="([^"]+)"""".toRegex()

        // Combine all paths in the Michigan group using UNION operation
        var combinedPath: Path? = null
        pathDataPattern.findAll(groupContent).forEach { pathMatch ->
            val pathData = pathMatch.groupValues[1]

            try {
                val path = parseSvgPath(pathData)

                // Check if parsed path is empty
                val pathBounds = RectF()
                path.computeBounds(pathBounds, true)

                // Skip empty paths - check both isEmpty and zero bounds
                val hasZeroBounds = pathBounds.width() == 0f && pathBounds.height() == 0f
                if (path.isEmpty || hasZeroBounds) {
                    return@forEach
                }

                if (combinedPath == null) {
                    // First valid path - use it as the base
                    combinedPath = path
                } else {
                    // Merge subsequent paths using UNION operation
                    val tempPath = Path()
                    tempPath.op(combinedPath!!, path, Path.Op.UNION)
                    combinedPath = tempPath
                }
            } catch (e: Exception) {
                Log.e(TAG, "Failed to parse MI subpath", e)
            }
        }

        if (combinedPath != null && !combinedPath!!.isEmpty) {
            val bounds = RectF()
            combinedPath!!.computeBounds(bounds, true)

            // Set fill type to WINDING for multi-contour path
            // WINDING properly fills all segments regardless of overlap
            // (EVEN_ODD would toggle fill on/off and make segments disappear)
            combinedPath!!.fillType = Path.FillType.WINDING

            // Create region for hit testing
            val region = Region()
            val clipRegion = Region(
                bounds.left.toInt(),
                bounds.top.toInt(),
                bounds.right.toInt(),
                bounds.bottom.toInt()
            )
            region.setPath(combinedPath!!, clipRegion)

            statePathsMap["MI"] = StatePath(
                code = "MI",
                path = combinedPath!!,
                bounds = bounds,
                region = region
            )
        } else {
            Log.w(TAG, "Michigan combined path is empty or null!")
        }
    } else {
        Log.w(TAG, "Michigan <g> group not found in SVG!")
    }
}

/**
 * Converts SVG path data string to Android Path object.
 *
 * This is a simplified parser that handles the common SVG path commands:
 * M (moveto), L (lineto), C (curveto), Z (closepath)
 */
private fun parseSvgPath(pathData: String): Path {
    val path = Path()

    // Set fill type to WINDING for proper multi-segment rendering
    // WINDING fills all closed contours, EVEN_ODD would toggle and cause disappearing segments
    path.fillType = Path.FillType.WINDING

    // Split by commands (M, L, C, Z, etc.)
    val commands = pathData.trim().split(Regex("(?=[MLCZmlcz])"))

    var commandCount = mutableMapOf<Char, Int>()

    commands.forEach { command ->
        if (command.isBlank()) return@forEach

        val cmd = command[0]
        commandCount[cmd] = commandCount.getOrDefault(cmd, 0) + 1

        val coords = command.substring(1).trim()
            .split(Regex("\\s+|,"))
            .filter { it.isNotBlank() }
            .mapNotNull { it.toFloatOrNull() }

        when (cmd) {
            'M', 'm' -> {
                if (coords.size >= 2) {
                    if (cmd == 'M') {
                        path.moveTo(coords[0], coords[1])
                    } else {
                        path.rMoveTo(coords[0], coords[1])
                    }
                }
            }
            'L', 'l' -> {
                var i = 0
                while (i + 1 < coords.size) {
                    if (cmd == 'L') {
                        path.lineTo(coords[i], coords[i + 1])
                    } else {
                        path.rLineTo(coords[i], coords[i + 1])
                    }
                    i += 2
                }
            }
            'C', 'c' -> {
                var i = 0
                while (i + 5 < coords.size) {
                    if (cmd == 'C') {
                        path.cubicTo(
                            coords[i], coords[i + 1],
                            coords[i + 2], coords[i + 3],
                            coords[i + 4], coords[i + 5]
                        )
                    } else {
                        path.rCubicTo(
                            coords[i], coords[i + 1],
                            coords[i + 2], coords[i + 3],
                            coords[i + 4], coords[i + 5]
                        )
                    }
                    i += 6
                }
            }
            'Z', 'z' -> {
                path.close()
            }
            else -> {
                Log.w(TAG, "Unsupported SVG command: $cmd in path")
            }
        }
    }

    return path
}

/**
 * Composable that renders the SVG US map with dynamic state coloring and tap detection.
 *
 * @param states List of US states with photo counts
 * @param onStateClick Callback when a state is tapped
 * @param scale Current zoom scale
 * @param offset Current pan offset
 * @param onScaleChange Callback for scale changes
 * @param onOffsetChange Callback for offset changes
 * @param onDoubleTap Callback for double-tap gesture
 * @param lightGreen Color for states with 1-10 photos
 * @param mediumGreen Color for states with 11-25 photos
 * @param darkGreen Color for states with 26+ photos
 * @param modifier Optional modifier
 */
@Composable
fun SvgUsMapRenderer(
    states: List<UsState>,
    onStateClick: (UsState) -> Unit,
    scale: Float,
    offset: Offset,
    onScaleChange: (Float) -> Unit,
    onOffsetChange: (Offset) -> Unit,
    onDoubleTap: () -> Unit,
    lightGreen: Color,
    mediumGreen: Color,
    darkGreen: Color,
    modifier: Modifier = Modifier,
    minScale: Float = 1f,
    maxScale: Float = 3f
) {
    val context = LocalContext.current

    // Theme-adaptive border color - matches MapScreen background exactly
    val borderColor = MaterialTheme.colorScheme.background

    // Parse SVG paths once
    val statePathsMap = remember {
        parseUsMapSvg(context)
    }

    // Create a map of state code to photo count and color
    val stateDataMap = remember(states) {
        states.associateBy { it.code }
    }

    // Color for states without photos
    val grayColor = MaterialTheme.colorScheme.surfaceVariant

    // SVG dimensions
    val svgWidth = 959f
    val svgHeight = 593f

    // Capture current values for gesture detection
    val currentScale by rememberUpdatedState(scale)
    val currentOffset by rememberUpdatedState(offset)

    Canvas(
        modifier = modifier
            .graphicsLayer(
                scaleX = scale,
                scaleY = scale,
                translationX = offset.x,
                translationY = offset.y,
                clip = false
            )
            .pointerInput(Unit) {
                // Pinch-to-zoom and drag-to-pan gestures
                detectTransformGestures { _, pan, zoom, _ ->
                    val newScale = (currentScale * zoom).coerceIn(minScale, maxScale)
                    val newOffset = currentOffset + pan

                    onScaleChange(newScale)
                    onOffsetChange(newOffset)
                }
            }
            .pointerInput(Unit) {
                // Tap detection for state selection
                detectTapGestures(
                    onDoubleTap = { onDoubleTap() },
                    onTap = { tapOffset ->
                        // tapOffset is already in canvas space due to modifier order
                        // Just need to convert from canvas coordinates to SVG coordinates

                        val canvasWidth = size.width.toFloat()
                        val canvasHeight = size.height.toFloat()

                        // Calculate SVG scaling (same as used in rendering)
                        val fitScaleX = canvasWidth / svgWidth
                        val fitScaleY = canvasHeight / svgHeight
                        val fitScale = minOf(fitScaleX, fitScaleY)

                        val scaledWidth = svgWidth * fitScale
                        val scaledHeight = svgHeight * fitScale
                        val canvasOffsetX = (canvasWidth - scaledWidth) / 2f
                        val canvasOffsetY = (canvasHeight - scaledHeight) / 2f

                        // Convert canvas tap to SVG coordinates
                        val svgX = (tapOffset.x - canvasOffsetX) / fitScale
                        val svgY = (tapOffset.y - canvasOffsetY) / fitScale

                        // Check which state path contains this point
                        statePathsMap.forEach { (code, statePath) ->
                            // First check if tap is within bounding box (optimization)
                            if (statePath.bounds.contains(svgX, svgY)) {
                                // Then check if actually inside the path using Region
                                if (statePath.region.contains(svgX.toInt(), svgY.toInt())) {
                                    // Find state data and trigger callback if it has photos
                                    val stateData = stateDataMap[code]
                                    if (stateData != null && stateData.photoCount > 0) {
                                        onStateClick(stateData)
                                    }
                                    return@detectTapGestures
                                }
                            }
                        }
                    }
                )
            }
    ) {
        drawIntoCanvas { canvas ->
            // Calculate scale to fit SVG to canvas
            val canvasWidth = size.width
            val canvasHeight = size.height
            val scaleX = canvasWidth / svgWidth
            val scaleY = canvasHeight / svgHeight
            val fitScale = minOf(scaleX, scaleY)

            // Calculate centered position
            val scaledWidth = svgWidth * fitScale
            val scaledHeight = svgHeight * fitScale
            val offsetX = (canvasWidth - scaledWidth) / 2f
            val offsetY = (canvasHeight - scaledHeight) / 2f

            val paint = android.graphics.Paint().apply {
                isAntiAlias = true
                style = android.graphics.Paint.Style.FILL
            }

            // Save canvas state
            canvas.nativeCanvas.save()

            // Apply centering offset and scaling to fit canvas
            canvas.nativeCanvas.translate(offsetX, offsetY)
            canvas.nativeCanvas.scale(fitScale, fitScale)

            // Draw each state with appropriate color
            statePathsMap.forEach { (code, statePath) ->
                val stateData = stateDataMap[code]

                // Determine color based on photo count
                val color = if (stateData != null && stateData.photoCount > 0) {
                    when {
                        stateData.photoCount in 1..10 -> lightGreen
                        stateData.photoCount in 11..25 -> mediumGreen
                        else -> darkGreen
                    }
                } else {
                    grayColor
                }

                // Set paint color
                paint.color = color.toArgb()

                // Draw the state path
                canvas.nativeCanvas.drawPath(statePath.path, paint)

                // Draw stroke (border) with theme-adaptive color
                val strokePaint = android.graphics.Paint().apply {
                    style = android.graphics.Paint.Style.STROKE
                    strokeWidth = 0.75f
                    setColor(borderColor.toArgb())  // Theme-adaptive border color
                    isAntiAlias = true
                }

                canvas.nativeCanvas.drawPath(statePath.path, strokePaint)
            }

            // Restore canvas state
            canvas.nativeCanvas.restore()
        }
    }
}

/**
 * Extension function for RectF to string.
 */
private fun RectF.toShortString(): String =
    "(${left.toInt()}, ${top.toInt()}, ${right.toInt()}, ${bottom.toInt()})"

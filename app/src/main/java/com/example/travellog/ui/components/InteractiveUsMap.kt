package com.example.travellog.ui.components

import android.content.res.Configuration
import android.util.Log
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.spring
import androidx.compose.foundation.Image
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.gestures.detectTransformGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CenterFocusWeak
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clipToBounds
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.graphics.BlendMode
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.example.travellog.R
import com.example.travellog.data.models.UsState
import kotlinx.coroutines.launch

/**
 * Interactive US map with visual progress indicators and list-based state selection.
 *
 * Shows travel progress through color-coded state overlays:
 * - 0 photos: gray (base map)
 * - 1-5 photos: light green
 * - 6-15 photos: medium green
 * - 16+ photos: dark green
 *
 * @param states List of US states with photo counts
 * @param onStateClick Callback when a state is selected from the list
 * @param modifier Optional modifier
 */
// Zoom scale constants - consistent for both portrait and landscape
private const val MIN_SCALE = 1f
private const val MAX_SCALE = 3f

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun InteractiveUsMap(
    states: List<UsState>,
    onStateClick: (UsState) -> Unit,
    modifier: Modifier = Modifier
) {
    val configuration = LocalConfiguration.current
    val isLandscape = configuration.orientation == Configuration.ORIENTATION_LANDSCAPE
    val scope = rememberCoroutineScope()

    var showStateList by remember { mutableStateOf(false) }
    var searchQuery by remember { mutableStateOf("") }

    // Zoom and pan state - use Offset for pan to update together
    var scale by remember { mutableStateOf(MIN_SCALE) }
    var offset by remember { mutableStateOf(Offset.Zero) }

    // Reset function with animation
    fun resetZoom() {
        Log.d("MapZoom", "Reset zoom called: currentScale=$scale, offset=$offset")
        scope.launch {
            // Animate scale back to 1
            val scaleAnimatable = Animatable(scale)
            val offsetXAnimatable = Animatable(offset.x)
            val offsetYAnimatable = Animatable(offset.y)

            launch {
                scaleAnimatable.animateTo(MIN_SCALE, spring())
                scale = scaleAnimatable.value
                Log.d("MapZoom", "Reset zoom complete: scale=$scale")
            }
            launch {
                offsetXAnimatable.animateTo(0f, spring())
                offsetYAnimatable.animateTo(0f, spring())
                offset = Offset(offsetXAnimatable.value, offsetYAnimatable.value)
            }
        }
    }

    // Nature theme colors for photo count overlays
    // Brighter colors to account for gray base map when using ColorFilter tinting
    val lightGreen = Color(0xFFB2DFAF)   // 1-5 photos (brighter for visibility over gray)
    val mediumGreen = Color(0xFF7CB877)  // 6-15 photos (brighter for visibility over gray)
    val darkGreen = Color(0xFF4A7C3A)    // 16+ photos (slightly lighter than original #2D5016)

    // Log states with photos on first composition
    LaunchedEffect(states) {
        val statesWithPhotos = states.filter { it.photoCount > 0 }
        Log.d("InteractiveUsMap", "═══════════════════════════════════")
        Log.d("InteractiveUsMap", "OVERLAY SYSTEM INITIALIZATION")
        Log.d("InteractiveUsMap", "Total states: ${states.size}")
        Log.d("InteractiveUsMap", "States with photos: ${statesWithPhotos.size}")
        statesWithPhotos.forEach { state ->
            val color = when {
                state.photoCount in 1..5 -> "Light Green"
                state.photoCount in 6..15 -> "Medium Green"
                else -> "Dark Green"
            }
            Log.d("InteractiveUsMap", "  ${state.code} (${state.name}): ${state.photoCount} photos → $color")
        }
        Log.d("InteractiveUsMap", "═══════════════════════════════════")
    }

    if (isLandscape) {
        // Landscape layout - row with map and legend on side
        Row(
            modifier = modifier.fillMaxSize()
        ) {
            // Map and buttons container
            Box(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxHeight()
                    .clipToBounds() // Prevent map from panning over title
            ) {
                // Map content - SVG-based renderer with tap detection
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    SvgUsMapRenderer(
                        states = states,
                        onStateClick = onStateClick,
                        scale = scale,
                        offset = offset,
                        onScaleChange = { newScale ->
                            scale = newScale.coerceIn(MIN_SCALE, MAX_SCALE)
                        },
                        onOffsetChange = { newOffset ->
                            offset = newOffset
                        },
                        onDoubleTap = {
                            val targetScale = if (scale > 1.5f) MIN_SCALE else 2f
                            Log.d("MapZoom", "Double-tap zoom: currentScale=$scale, targetScale=$targetScale")
                            scope.launch {
                                val scaleAnim = Animatable(scale)
                                scaleAnim.animateTo(targetScale, spring())
                                scale = scaleAnim.value
                            }
                        },
                        minScale = MIN_SCALE,
                        maxScale = MAX_SCALE,
                        modifier = Modifier
                            .fillMaxWidth(0.95f)
                            .aspectRatio(1.5f)
                    )
                }

                // Reset zoom button - top right (outside map content)
                if (scale > MIN_SCALE || offset != Offset.Zero) {
                    IconButton(
                        onClick = { resetZoom() },
                        modifier = Modifier
                            .align(Alignment.TopEnd)
                            .padding(8.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.CenterFocusWeak,
                            contentDescription = "Reset Zoom",
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(24.dp)
                        )
                    }
                }
            }

            // Vertical legend and button column
            Column(
                modifier = Modifier
                    .width(120.dp)
                    .fillMaxHeight()
                    .padding(8.dp),
                verticalArrangement = Arrangement.Center,
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // "View States" button
                Button(
                    onClick = { showStateList = true },
                    modifier = Modifier.fillMaxWidth(),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.primary
                    )
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Icon(
                            imageVector = Icons.Default.Search,
                            contentDescription = null,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "View\nStates",
                            style = MaterialTheme.typography.labelSmall,
                            textAlign = TextAlign.Center
                        )
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Vertical legend
                Column(
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                    horizontalAlignment = Alignment.Start
                ) {
                    LegendItemVertical(color = Color(0xFF9E9E9E), label = "No photos")
                    LegendItemVertical(color = lightGreen, label = "1-5")
                    LegendItemVertical(color = mediumGreen, label = "6-15")
                    LegendItemVertical(color = darkGreen, label = "16+")
                }
            }
        }
    } else {
        // Portrait layout - original column layout
        Column(
            modifier = modifier.fillMaxSize(),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Map and buttons container
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
                    .clipToBounds() // Prevent map from panning over title
            ) {
                // Map content - SVG-based renderer with tap detection
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    SvgUsMapRenderer(
                        states = states,
                        onStateClick = onStateClick,
                        scale = scale,
                        offset = offset,
                        onScaleChange = { newScale ->
                            scale = newScale.coerceIn(MIN_SCALE, MAX_SCALE)
                        },
                        onOffsetChange = { newOffset ->
                            offset = newOffset
                        },
                        onDoubleTap = {
                            val targetScale = if (scale > 1.5f) MIN_SCALE else 2f
                            Log.d("MapZoom", "Double-tap zoom: currentScale=$scale, targetScale=$targetScale")
                            scope.launch {
                                val scaleAnim = Animatable(scale)
                                scaleAnim.animateTo(targetScale, spring())
                                scale = scaleAnim.value
                            }
                        },
                        minScale = MIN_SCALE,
                        maxScale = MAX_SCALE,
                        modifier = Modifier
                            .fillMaxWidth(0.95f)
                            .aspectRatio(1.5f)
                    )
                }

                // Reset zoom button - top left in portrait
                if (scale > MIN_SCALE || offset != Offset.Zero) {
                    IconButton(
                        onClick = { resetZoom() },
                        modifier = Modifier
                            .align(Alignment.TopStart)
                            .padding(8.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.CenterFocusWeak,
                            contentDescription = "Reset Zoom",
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(24.dp)
                        )
                    }
                }
            }

            // "View States" button - bottom center
            Button(
                onClick = { showStateList = true },
                modifier = Modifier
                    .fillMaxWidth(0.9f)
                    .padding(vertical = 16.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.primary
                )
            ) {
                Icon(
                    imageVector = Icons.Default.Search,
                    contentDescription = null,
                    modifier = Modifier.size(20.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "View States with Photos",
                    style = MaterialTheme.typography.titleMedium
                )
            }

            // Map legend - using same colors as overlays
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp),
                horizontalArrangement = Arrangement.SpaceEvenly,
                verticalAlignment = Alignment.CenterVertically
            ) {
                LegendItem(color = Color(0xFF9E9E9E), label = "No photos")
                LegendItem(color = lightGreen, label = "1-5")
                LegendItem(color = mediumGreen, label = "6-15")
                LegendItem(color = darkGreen, label = "16+")
            }
        }
    }

    // State selection bottom sheet
    if (showStateList) {
        ModalBottomSheet(
            onDismissRequest = {
                showStateList = false
                searchQuery = ""
            },
            sheetState = rememberModalBottomSheetState(
                skipPartiallyExpanded = true
            ),
            dragHandle = {
                // Prominent drag handle
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Surface(
                        modifier = Modifier
                            .padding(vertical = 12.dp)
                            .width(40.dp)
                            .height(4.dp),
                        shape = MaterialTheme.shapes.extraLarge,
                        color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.4f)
                    ) {}
                }
            },
            modifier = Modifier.fillMaxHeight(0.92f)
        ) {
            Column(
                modifier = Modifier.fillMaxSize()
            ) {
                // Fixed header with search - doesn't scroll
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp)
                ) {
                    // Header
                    Text(
                        text = "Your States with Photos",
                        style = MaterialTheme.typography.headlineSmall,
                        modifier = Modifier.padding(bottom = 16.dp)
                    )

                    // Search field
                    OutlinedTextField(
                        value = searchQuery,
                        onValueChange = { searchQuery = it },
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(bottom = 16.dp),
                        placeholder = { Text("Search states...") },
                        leadingIcon = {
                            Icon(
                                imageVector = Icons.Default.Search,
                                contentDescription = "Search"
                            )
                        },
                        singleLine = true
                    )
                }

                // Filtered state list - scrollable (only states with photos)
                val filteredStates = states
                    .filter { it.photoCount > 0 } // Only show states with photos
                    .filter { state ->
                        state.name.contains(searchQuery, ignoreCase = true) ||
                        state.code.contains(searchQuery, ignoreCase = true)
                    }
                    .sortedByDescending { it.photoCount } // Sort by most photos first

                LazyColumn(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    if (filteredStates.isEmpty()) {
                        // Empty state message
                        item {
                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 48.dp),
                                horizontalAlignment = Alignment.CenterHorizontally,
                                verticalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Text(
                                    text = if (searchQuery.isNotEmpty()) {
                                        "No states found matching \"$searchQuery\""
                                    } else {
                                        "No states with photos yet"
                                    },
                                    style = MaterialTheme.typography.bodyLarge,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                    textAlign = TextAlign.Center
                                )
                                if (searchQuery.isEmpty()) {
                                    Text(
                                        text = "Start adding photos to see your progress!",
                                        style = MaterialTheme.typography.bodyMedium,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f),
                                        textAlign = TextAlign.Center
                                    )
                                }
                            }
                        }
                    } else {
                        items(filteredStates) { state ->
                            Surface(
                                onClick = {
                                    showStateList = false
                                    searchQuery = ""
                                    onStateClick(state)
                                },
                                modifier = Modifier.fillMaxWidth(),
                                shape = MaterialTheme.shapes.medium,
                                color = MaterialTheme.colorScheme.surfaceVariant
                            ) {
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(16.dp),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Column {
                                        Text(
                                            text = state.name,
                                            style = MaterialTheme.typography.bodyLarge
                                        )
                                        Text(
                                            text = state.code,
                                            style = MaterialTheme.typography.bodySmall,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                    }

                                    val badgeColor = when {
                                        state.photoCount in 1..5 -> lightGreen
                                        state.photoCount in 6..15 -> mediumGreen
                                        else -> darkGreen
                                    }
                                    Surface(
                                        shape = MaterialTheme.shapes.small,
                                        color = badgeColor
                                    ) {
                                        Text(
                                            text = "${state.photoCount} ${if (state.photoCount == 1) "photo" else "photos"}",
                                            style = MaterialTheme.typography.labelMedium,
                                            color = Color.White,
                                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
                                        )
                                    }
                                }
                            }
                        }
                    }

                    // Bottom padding
                    item {
                        Spacer(modifier = Modifier.height(32.dp))
                    }
                }
            }
        }
    }
}


/**
 * Legend item showing color and label (horizontal).
 */
@Composable
private fun LegendItem(color: Color, label: String) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        Surface(
            modifier = Modifier.size(16.dp),
            shape = MaterialTheme.shapes.small,
            color = color
        ) {}
        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.7f)
        )
    }
}

/**
 * Legend item for landscape mode (vertical layout).
 */
@Composable
private fun LegendItemVertical(color: Color, label: String) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(6.dp)
    ) {
        Surface(
            modifier = Modifier.size(20.dp),
            shape = MaterialTheme.shapes.small,
            color = color
        ) {}
        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.7f)
        )
    }
}

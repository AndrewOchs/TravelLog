package com.example.wanderstate.ui.screens

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.wanderstate.R
import com.example.wanderstate.ui.theme.StatsNumberStyle
import com.example.wanderstate.ui.viewmodel.StateStats
import com.example.wanderstate.ui.viewmodel.StatsViewModel
import kotlin.math.roundToInt

/**
 * Statistics screen showing travel progress and app usage metrics.
 * Displays states visited, photos taken, journals written, and more.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun StatsScreen(
    onNavigateToState: (String) -> Unit = {},
    modifier: Modifier = Modifier
) {
    val viewModel: StatsViewModel = hiltViewModel()
    val stats by viewModel.travelStats.collectAsState()
    val configuration = LocalConfiguration.current
    val screenWidth = configuration.screenWidthDp

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        // Compact title section with centered title and compass accents
        Surface(
            modifier = Modifier.fillMaxWidth(),
            color = MaterialTheme.colorScheme.surface,
            shadowElevation = 2.dp
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                val compassColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.5f)

                // Aggressive responsive sizing for small screens
                val titleText: String
                val titleFontSize: androidx.compose.ui.unit.TextUnit
                val compassSize: androidx.compose.ui.unit.Dp
                val spacerWidth: androidx.compose.ui.unit.Dp

                when {
                    screenWidth < 360 -> {
                        // Very small screens - shorter text, smallest sizes
                        titleText = "WanderStats"
                        titleFontSize = 20.sp
                        compassSize = 22.dp
                        spacerWidth = 4.dp
                    }
                    screenWidth < 400 -> {
                        // Medium screens - slightly smaller
                        titleText = "Your WanderStats"
                        titleFontSize = 24.sp
                        compassSize = 26.dp
                        spacerWidth = 8.dp
                    }
                    else -> {
                        // Large screens - full size
                        titleText = "Your WanderStats"
                        titleFontSize = 28.sp
                        compassSize = 28.dp
                        spacerWidth = 12.dp
                    }
                }

                val titleStyle = MaterialTheme.typography.headlineMedium.copy(fontSize = titleFontSize)

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.Center
                ) {
                    // Left compass
                    Icon(
                        painter = painterResource(id = R.drawable.ic_compass_accent),
                        contentDescription = null,
                        tint = compassColor,
                        modifier = Modifier.size(compassSize)
                    )

                    Spacer(modifier = Modifier.width(spacerWidth))

                    // Title text - responsive with shortened text on small screens
                    Text(
                        text = titleText,
                        style = titleStyle,
                        color = MaterialTheme.colorScheme.primary,
                        maxLines = 1,
                        softWrap = false
                    )

                    Spacer(modifier = Modifier.width(spacerWidth))

                    // Right compass
                    Icon(
                        painter = painterResource(id = R.drawable.ic_compass_accent),
                        contentDescription = null,
                        tint = compassColor,
                        modifier = Modifier.size(compassSize)
                    )
                }
            }
        }

        // Scrollable content
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 16.dp, vertical = 8.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Hero Section - Circular Progress
            StatesProgressHero(
                statesVisited = stats.statesVisited,
                totalStates = stats.totalStates
            )

            // Key Metrics Cards
            KeyMetricsSection(stats = stats)

            // Recent Activity
            RecentActivitySection(stats = stats)

            // State Breakdown
            if (stats.stateBreakdown.isNotEmpty()) {
                StateBreakdownSection(
                    stateBreakdown = stats.stateBreakdown,
                    onNavigateToState = onNavigateToState
                )
            }

            // Fun Facts
            if (stats.totalPhotos > 0) {
                FunFactsSection(stats = stats)
            }
        }
    }
}

/**
 * Hero section with large circular progress indicator for states visited.
 */
@Composable
private fun StatesProgressHero(
    statesVisited: Int,
    totalStates: Int
) {
    val progress = if (totalStates > 0) statesVisited.toFloat() / totalStates else 0f
    val animatedProgress by animateFloatAsState(
        targetValue = progress,
        animationSpec = tween(durationMillis = 1000),
        label = "progress"
    )

    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(32.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier.size(200.dp)
            ) {
                // Circular progress indicator with visible track
                CircularProgressIndicator(
                    progress = { animatedProgress },
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.primary,
                    strokeWidth = 16.dp,
                    trackColor = MaterialTheme.colorScheme.outlineVariant,
                    strokeCap = StrokeCap.Round
                )

                // Center text
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = statesVisited.toString(),
                        style = StatsNumberStyle.copy(fontSize = 56.sp),
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        text = "of $totalStates",
                        style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        text = "${(progress * 100).roundToInt()}%",
                        style = StatsNumberStyle.copy(fontSize = 32.sp),
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            Text(
                text = "States Explored",
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface
            )

            if (statesVisited > 0) {
                Text(
                    text = "Keep exploring to reach all 50!",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    textAlign = TextAlign.Center
                )
            }
        }
    }
}

/**
 * Grid of key metric cards.
 */
@Composable
private fun KeyMetricsSection(stats: com.example.wanderstate.ui.viewmodel.TravelStats) {
    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        Text(
            text = "Key Metrics",
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.primary
        )

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            MetricCard(
                icon = Icons.Default.Photo,
                title = "Total Photos",
                value = stats.totalPhotos.toString(),
                modifier = Modifier.weight(1f)
            )

            MetricCard(
                icon = Icons.Default.CalendarToday,
                title = "Days Tracking",
                value = stats.daysSinceFirstPhoto.toString(),
                modifier = Modifier.weight(1f)
            )
        }

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            MetricCard(
                icon = Icons.Default.Book,
                title = "With Journals",
                value = "${stats.photosWithJournals} (${stats.journalPercentage.roundToInt()}%)",
                modifier = Modifier.weight(1f)
            )

            stats.mostPhotographedState?.let { state ->
                MetricCard(
                    icon = Icons.Default.Star,
                    title = "Top State",
                    value = "${state.stateCode} (${state.photoCount})",
                    modifier = Modifier.weight(1f)
                )
            }
        }
    }
}

/**
 * Individual metric card.
 */
@Composable
private fun MetricCard(
    icon: ImageVector,
    title: String,
    value: String,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier,
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Icon(
                imageVector = icon,
                contentDescription = title,
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(32.dp)
            )

            Text(
                text = value,
                style = StatsNumberStyle.copy(fontSize = 24.sp),
                color = MaterialTheme.colorScheme.onSurface,
                textAlign = TextAlign.Center
            )

            Text(
                text = title,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center
            )
        }
    }
}

/**
 * Recent activity section.
 */
@Composable
private fun RecentActivitySection(stats: com.example.wanderstate.ui.viewmodel.TravelStats) {
    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        Text(
            text = "Recent Activity",
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.primary
        )

        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.secondaryContainer
            )
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                ActivityRow(
                    icon = Icons.Default.DateRange,
                    label = "Photos this week",
                    value = stats.photosThisWeek.toString()
                )

                HorizontalDivider(color = MaterialTheme.colorScheme.onSecondaryContainer.copy(alpha = 0.2f))

                ActivityRow(
                    icon = Icons.Default.CalendarMonth,
                    label = "Photos this month",
                    value = stats.photosThisMonth.toString()
                )

                HorizontalDivider(color = MaterialTheme.colorScheme.onSecondaryContainer.copy(alpha = 0.2f))

                ActivityRow(
                    icon = Icons.Default.Map,
                    label = "States this month",
                    value = stats.statesThisMonth.toString()
                )
            }
        }
    }
}

/**
 * Individual activity row.
 */
@Composable
private fun ActivityRow(
    icon: ImageVector,
    label: String,
    value: String
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Icon(
                imageVector = icon,
                contentDescription = label,
                tint = MaterialTheme.colorScheme.onSecondaryContainer,
                modifier = Modifier.size(24.dp)
            )

            Text(
                text = label,
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSecondaryContainer
            )
        }

        Text(
            text = value,
            style = StatsNumberStyle.copy(fontSize = 20.sp),
            color = MaterialTheme.colorScheme.onSecondaryContainer
        )
    }
}

/**
 * State breakdown section with progress bars.
 */
@Composable
private fun StateBreakdownSection(
    stateBreakdown: List<StateStats>,
    onNavigateToState: (String) -> Unit
) {
    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        Text(
            text = "States Visited (${stateBreakdown.size})",
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.primary
        )

        val maxPhotos = stateBreakdown.maxOfOrNull { it.photoCount } ?: 1

        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surface
            ),
            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                stateBreakdown.take(10).forEach { state ->
                    StateBreakdownItem(
                        state = state,
                        maxPhotos = maxPhotos,
                        onClick = { onNavigateToState(state.stateCode) }
                    )
                }

                if (stateBreakdown.size > 10) {
                    Text(
                        text = "And ${stateBreakdown.size - 10} more states...",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.padding(top = 8.dp)
                    )
                }
            }
        }
    }
}

/**
 * Individual state breakdown item with progress bar.
 */
@Composable
private fun StateBreakdownItem(
    state: StateStats,
    maxPhotos: Int,
    onClick: () -> Unit
) {
    val progress = if (maxPhotos > 0) state.photoCount.toFloat() / maxPhotos else 0f
    val animatedProgress by animateFloatAsState(
        targetValue = progress,
        animationSpec = tween(durationMillis = 500),
        label = "stateProgress"
    )

    Surface(
        onClick = onClick,
        modifier = Modifier.fillMaxWidth(),
        shape = MaterialTheme.shapes.small,
        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "${state.stateCode} - ${state.stateName}",
                    style = MaterialTheme.typography.bodyLarge,
                    fontWeight = FontWeight.Medium,
                    color = MaterialTheme.colorScheme.onSurface
                )

                Text(
                    text = "${state.photoCount} photos",
                    style = StatsNumberStyle.copy(fontSize = 14.sp),
                    color = MaterialTheme.colorScheme.primary
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            LinearProgressIndicator(
                progress = { animatedProgress },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(8.dp),
                color = MaterialTheme.colorScheme.primary,
                trackColor = MaterialTheme.colorScheme.surfaceVariant,
                strokeCap = StrokeCap.Round
            )
        }
    }
}

/**
 * Fun facts section.
 */
@Composable
private fun FunFactsSection(stats: com.example.wanderstate.ui.viewmodel.TravelStats) {
    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        Text(
            text = "Fun Facts",
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.primary
        )

        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surfaceVariant
            )
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                FunFactItem(
                    icon = Icons.Default.Calculate,
                    fact = "Average photos per state",
                    value = String.format("%.1f", stats.averagePhotosPerState)
                )

                if (stats.longestJournalLength > 0) {
                    FunFactItem(
                        icon = Icons.Default.Article,
                        fact = "Longest journal entry",
                        value = "${stats.longestJournalLength} characters"
                    )
                }

                if (stats.journalPercentage > 0) {
                    FunFactItem(
                        icon = Icons.Default.TrendingUp,
                        fact = "Journal coverage",
                        value = "${stats.journalPercentage.roundToInt()}% of photos"
                    )
                }
            }
        }
    }
}

/**
 * Individual fun fact item.
 */
@Composable
private fun FunFactItem(
    icon: ImageVector,
    fact: String,
    value: String
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Icon(
            imageVector = icon,
            contentDescription = fact,
            tint = MaterialTheme.colorScheme.onSurface,
            modifier = Modifier.size(32.dp)
        )

        Column(
            modifier = Modifier.weight(1f)
        ) {
            Text(
                text = fact,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.8f) // Increased from 0.7f
            )

            Text(
                text = value,
                style = StatsNumberStyle.copy(fontSize = 18.sp),
                color = MaterialTheme.colorScheme.onSurface // Darker for better contrast
            )
        }
    }
}

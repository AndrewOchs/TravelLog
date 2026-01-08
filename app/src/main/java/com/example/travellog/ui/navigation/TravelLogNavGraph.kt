package com.example.travellog.ui.navigation

import android.net.Uri
import android.util.Log
import androidx.compose.animation.AnimatedContentTransitionScope
import androidx.compose.animation.core.tween
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.toRoute
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import com.example.travellog.ui.screens.AddPhotoDetailsScreen
import com.example.travellog.ui.screens.GalleryScreen
import com.example.travellog.ui.screens.JournalEntryScreen
import com.example.travellog.ui.screens.MapScreen
import com.example.travellog.ui.screens.PhotoDetailScreen
import com.example.travellog.ui.screens.SettingsScreen
import com.example.travellog.ui.screens.StateDetailScreen
import com.example.travellog.ui.screens.StatsScreen
import com.example.travellog.ui.viewmodel.MapViewModel
import com.example.travellog.ui.viewmodel.PhotoSaveState
import com.example.travellog.ui.viewmodel.PhotoViewModel

/**
 * Main navigation graph for TravelLog app.
 *
 * @param navController Navigation controller for managing app navigation
 * @param modifier Optional modifier for styling
 */
@Composable
fun TravelLogNavGraph(
    navController: NavHostController,
    modifier: Modifier = Modifier
) {
    NavHost(
        navController = navController,
        startDestination = Route.Map,
        modifier = modifier
    ) {
        // Main bottom navigation screens
        composable<Route.Map> {
            val mapViewModel: MapViewModel = hiltViewModel()
            val states by mapViewModel.states.collectAsState()

            MapScreen(
                states = states,
                onStateClick = { stateCode ->
                    navController.navigate(Route.StateDetail(stateCode))
                },
                onPhotoSelected = { photoUri ->
                    navController.navigate(Route.AddPhotoDetails(photoUri))
                }
            )
        }

        composable<Route.Gallery> {
            GalleryScreen(
                onNavigateToPhotoDetail = { photoId, contextType, contextValue ->
                    navController.navigate(Route.PhotoDetail(photoId, contextType, contextValue))
                },
                onNavigateToJournalEntry = { photoId ->
                    navController.navigate(Route.JournalEntry(photoId))
                }
            )
        }

        composable<Route.Stats> {
            StatsScreen(
                onNavigateToState = { stateCode ->
                    navController.navigate(Route.StateDetail(stateCode))
                }
            )
        }

        composable<Route.Settings> {
            SettingsScreen()
        }

        // State Detail Screen
        composable<Route.StateDetail> { backStackEntry ->
            val detail = backStackEntry.toRoute<Route.StateDetail>()
            StateDetailScreen(
                stateCode = detail.stateCode,
                onNavigateBack = { navController.popBackStack() },
                onNavigateToPhotoDetail = { photoId, contextType, contextValue ->
                    navController.navigate(Route.PhotoDetail(photoId, contextType, contextValue))
                },
                onNavigateToJournalEntry = { photoId ->
                    navController.navigate(Route.JournalEntry(photoId))
                }
            )
        }

        // Add Photo Details Screen
        composable<Route.AddPhotoDetails> { backStackEntry ->
            val detail = backStackEntry.toRoute<Route.AddPhotoDetails>()
            val photoViewModel: PhotoViewModel = hiltViewModel()
            val saveState by photoViewModel.saveState.collectAsState()

            // Handle save success
            LaunchedEffect(saveState) {
                if (saveState is PhotoSaveState.Success) {
                    photoViewModel.resetSaveState()
                    navController.popBackStack(Route.Map, inclusive = false)
                }
            }

            AddPhotoDetailsScreen(
                photoUri = detail.photoUri,
                onSave = { stateCode, stateName, cityName, capturedDate ->
                    photoViewModel.savePhoto(
                        photoUri = Uri.parse(detail.photoUri),
                        stateCode = stateCode,
                        stateName = stateName,
                        cityName = cityName,
                        capturedDate = capturedDate
                    )
                },
                onNavigateBack = { navController.popBackStack() }
            )
        }

        // Photo Detail Screen with swipe navigation - Smooth slide animations
        composable<Route.PhotoDetail>(
            enterTransition = {
                slideIntoContainer(
                    towards = AnimatedContentTransitionScope.SlideDirection.Left,
                    animationSpec = tween(300)
                )
            },
            exitTransition = {
                slideOutOfContainer(
                    towards = AnimatedContentTransitionScope.SlideDirection.Left,
                    animationSpec = tween(300)
                )
            },
            popEnterTransition = {
                slideIntoContainer(
                    towards = AnimatedContentTransitionScope.SlideDirection.Right,
                    animationSpec = tween(300)
                )
            },
            popExitTransition = {
                slideOutOfContainer(
                    towards = AnimatedContentTransitionScope.SlideDirection.Right,
                    animationSpec = tween(300)
                )
            }
        ) { backStackEntry ->
            val detail = backStackEntry.toRoute<Route.PhotoDetail>()
            Log.d("Navigation", "Composing PhotoDetailScreen for photoId=${detail.photoId}")
            PhotoDetailScreen(
                photoId = detail.photoId,
                contextType = detail.contextType,
                contextValue = detail.contextValue,
                onNavigateBack = {
                    Log.d("Navigation", "PhotoDetail: onNavigateBack called - popBackStack")
                    navController.popBackStack()
                },
                onNavigateToJournalEntry = { photoId ->
                    Log.d("Navigation", "PhotoDetail: Navigate to JournalEntry for photoId=$photoId")
                    navController.navigate(Route.JournalEntry(photoId)) {
                        // Single top to prevent multiple instances
                        launchSingleTop = true
                    }
                }
            )
        }

        // Journal Entry Screen - Smooth slide animations
        composable<Route.JournalEntry>(
            enterTransition = {
                slideIntoContainer(
                    towards = AnimatedContentTransitionScope.SlideDirection.Left,
                    animationSpec = tween(300)
                )
            },
            exitTransition = {
                slideOutOfContainer(
                    towards = AnimatedContentTransitionScope.SlideDirection.Left,
                    animationSpec = tween(300)
                )
            },
            popEnterTransition = {
                slideIntoContainer(
                    towards = AnimatedContentTransitionScope.SlideDirection.Right,
                    animationSpec = tween(300)
                )
            },
            popExitTransition = {
                slideOutOfContainer(
                    towards = AnimatedContentTransitionScope.SlideDirection.Right,
                    animationSpec = tween(300)
                )
            }
        ) { backStackEntry ->
            val entry = backStackEntry.toRoute<Route.JournalEntry>()

            Log.d("Navigation", "Composing JournalEntryScreen for photoId=${entry.photoId}")
            JournalEntryScreen(
                photoId = entry.photoId,
                onSaveComplete = {
                    Log.d("Navigation", "JournalEntry: onSaveComplete called - delaying navigation")

                    // Small delay to let compose state settle before navigation
                    // This prevents screen flashing from recomposition timing issues
                    // MUST use Main dispatcher - navigation requires main thread
                    GlobalScope.launch(Dispatchers.Main) {
                        delay(50) // 50ms delay - imperceptible to users

                        Log.d("Navigation", "Delay complete - popBackStack to return to PhotoDetail")
                        Log.d("Navigation", "Current backstack before pop: ${navController.currentBackStackEntry?.destination?.route}")

                        val popped = navController.popBackStack()

                        Log.d("Navigation", "PopBackStack result: $popped")
                        Log.d("Navigation", "Current backstack after pop: ${navController.currentBackStackEntry?.destination?.route}")

                        if (!popped) {
                            Log.e("Navigation", "ERROR: PopBackStack FAILED - backstack might be empty!")
                        }
                    }
                },
                onNavigateBack = {
                    Log.d("Navigation", "JournalEntry: onNavigateBack called - popBackStack")
                    navController.popBackStack()
                }
            )
        }
    }
}

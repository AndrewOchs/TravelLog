package com.example.travellog.ui.navigation

import android.net.Uri
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
import com.example.travellog.ui.screens.AddPhotoDetailsScreen
import com.example.travellog.ui.screens.GalleryScreen
import com.example.travellog.ui.screens.MapScreen
import com.example.travellog.ui.screens.SettingsScreen
import com.example.travellog.ui.screens.StateDetailScreen
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
            GalleryScreen()
        }

        composable<Route.Settings> {
            SettingsScreen()
        }

        // State Detail Screen
        composable<Route.StateDetail> { backStackEntry ->
            val detail = backStackEntry.toRoute<Route.StateDetail>()
            StateDetailScreen(
                stateCode = detail.stateCode,
                onNavigateBack = { navController.popBackStack() }
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

        // Secondary screens will be added here as needed
        /*
        composable<Route.PhotoDetail> { backStackEntry ->
            val detail = backStackEntry.toRoute<Route.PhotoDetail>()
            PhotoDetailScreen(
                photoId = detail.photoId,
                onNavigateBack = { navController.popBackStack() }
            )
        }

        composable<Route.Camera> {
            CameraScreen(
                onPhotoTaken = { photoId ->
                    navController.navigate(Route.PhotoDetail(photoId))
                },
                onNavigateBack = { navController.popBackStack() }
            )
        }

        composable<Route.CityDetail> { backStackEntry ->
            val detail = backStackEntry.toRoute<Route.CityDetail>()
            CityDetailScreen(
                stateCode = detail.stateCode,
                cityName = detail.cityName,
                onNavigateBack = { navController.popBackStack() }
            )
        }
        */
    }
}

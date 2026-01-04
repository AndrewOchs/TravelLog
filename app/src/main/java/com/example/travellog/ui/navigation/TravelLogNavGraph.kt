package com.example.travellog.ui.navigation

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.toRoute
import com.example.travellog.ui.screens.GalleryScreen
import com.example.travellog.ui.screens.MapScreen
import com.example.travellog.ui.screens.SettingsScreen
import com.example.travellog.ui.screens.StateDetailScreen

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
            MapScreen(
                onStateClick = { stateCode ->
                    navController.navigate(Route.StateDetail(stateCode))
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

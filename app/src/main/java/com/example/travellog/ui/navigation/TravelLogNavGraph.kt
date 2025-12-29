package com.example.travellog.ui.navigation

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.toRoute
import com.example.travellog.ui.screens.HomeScreen

@Composable
fun TravelLogNavGraph(
    navController: NavHostController,
    modifier: Modifier = Modifier
) {
    NavHost(
        navController = navController,
        startDestination = Route.Home,
        modifier = modifier
    ) {
        composable<Route.Home> {
            HomeScreen(
                onNavigateToTripList = { navController.navigate(Route.TripList) },
                onNavigateToCamera = { navController.navigate(Route.Camera) }
            )
        }

        // Add more destinations as screens are created
        /*
        composable<Route.TripList> {
            TripListScreen(
                onNavigateToDetail = { tripId ->
                    navController.navigate(Route.TripDetail(tripId))
                },
                onNavigateBack = { navController.popBackStack() }
            )
        }

        composable<Route.TripDetail> { backStackEntry ->
            val detail = backStackEntry.toRoute<Route.TripDetail>()
            TripDetailScreen(
                tripId = detail.tripId,
                onNavigateBack = { navController.popBackStack() }
            )
        }
        */
    }
}

package com.example.wanderstate

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AddAPhoto
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.example.wanderstate.data.preferences.ThemeMode
import com.example.wanderstate.data.preferences.ThemePreferences
import com.example.wanderstate.ui.components.WanderStateBottomBar
import com.example.wanderstate.ui.navigation.Route
import com.example.wanderstate.ui.navigation.WanderStateNavGraph
import com.example.wanderstate.ui.navigation.bottomNavItems
import com.example.wanderstate.ui.theme.WanderStateTheme
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class MainActivity : ComponentActivity() {

    @Inject
    lateinit var themePreferences: ThemePreferences

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            WanderStateAppWithTheme()
        }
    }

    @Composable
    private fun WanderStateAppWithTheme() {
        val themeMode by themePreferences.themeMode.collectAsState(initial = ThemeMode.SYSTEM)
        val isSystemDark = isSystemInDarkTheme()

        val darkTheme = when (themeMode) {
            ThemeMode.LIGHT -> false
            ThemeMode.DARK -> true
            ThemeMode.SYSTEM -> isSystemDark
        }

        WanderStateTheme(darkTheme = darkTheme) {
            WanderStateApp()
        }
    }
}

@Composable
fun WanderStateApp() {
    val navController = rememberNavController()
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route

    // Determine if current route is a main bottom nav screen
    val currentBottomNavRoute = bottomNavItems.find { item ->
        currentRoute?.contains(item.route::class.simpleName ?: "") == true
    }?.route

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        bottomBar = {
            // Only show bottom bar on main screens
            if (currentBottomNavRoute != null) {
                WanderStateBottomBar(
                    currentRoute = currentBottomNavRoute,
                    onNavigate = { route ->
                        navController.navigate(route) {
                            // Pop up to the start destination to avoid building up a back stack
                            popUpTo(navController.graph.startDestinationId) {
                                saveState = true
                            }
                            // Avoid multiple copies of the same destination
                            launchSingleTop = true
                            // Restore state when reselecting a previously selected item
                            restoreState = true
                        }
                    }
                )
            }
        },
        containerColor = MaterialTheme.colorScheme.background
    ) { innerPadding ->
        WanderStateNavGraph(
            navController = navController,
            modifier = Modifier.padding(innerPadding)
        )
    }
}

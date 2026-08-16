package com.telotaxi.planner.ui

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Dashboard
import androidx.compose.material.icons.filled.List
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavDestination.Companion.hierarchy
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.compose.runtime.collectAsState
import com.telotaxi.planner.data.Ride
import com.telotaxi.planner.data.UserPreferences
import com.telotaxi.planner.ui.components.AppFooter
import com.telotaxi.planner.ui.components.DailyGreetingDialog
import com.telotaxi.planner.ui.components.GreetingMessages
import com.telotaxi.planner.ui.screens.AddRideScreen
import com.telotaxi.planner.ui.screens.DashboardScreen
import com.telotaxi.planner.ui.screens.ReportsScreen
import com.telotaxi.planner.ui.screens.RidesListScreen
import com.telotaxi.planner.ui.screens.WelcomeScreen

private sealed class Screen(val route: String, val label: String, val icon: androidx.compose.ui.graphics.vector.ImageVector) {
    object Dashboard : Screen("dashboard", "Accueil", Icons.Default.Dashboard)
    object Rides : Screen("rides", "Courses", Icons.Default.List)
}

private val bottomItems = listOf(Screen.Dashboard, Screen.Rides)

@Composable
fun AppNavigation() {
    val context = LocalContext.current
    val navController = rememberNavController()
    val viewModel: PlannerViewModel = viewModel()

    // Nom du chauffeur : s'il n'est pas encore renseigné, on bloque sur l'écran de bienvenue
    var driverName by remember { mutableStateOf(UserPreferences.getDriverName(context)) }

    // Salutation quotidienne : affichée une seule fois par jour, après confirmation du nom
    var showGreeting by remember {
        mutableStateOf(driverName != null && UserPreferences.shouldShowGreetingToday(context))
    }
    val greetingMessage = remember(showGreeting) {
        if (showGreeting) GreetingMessages.randomGreeting(driverName ?: "") else ""
    }

    if (driverName == null) {
        WelcomeScreen(
            onNameConfirmed = { name ->
                UserPreferences.setDriverName(context, name)
                driverName = name
                if (UserPreferences.shouldShowGreetingToday(context)) {
                    showGreeting = true
                }
            }
        )
        return
    }

    if (showGreeting) {
        DailyGreetingDialog(
            message = greetingMessage,
            onDismiss = {
                UserPreferences.markGreetingShownToday(context)
                showGreeting = false
            }
        )
    }

    Scaffold(
        bottomBar = {
            val backStackEntry by navController.currentBackStackEntryAsState()
            val currentDestination = backStackEntry?.destination
            val onMainScreen = bottomItems.any { screen ->
                currentDestination?.hierarchy?.any { it.route == screen.route } == true
            }
            Column {
                if (onMainScreen) {
                    NavigationBar {
                        bottomItems.forEach { screen ->
                            val selected = currentDestination?.hierarchy?.any { it.route == screen.route } == true
                            NavigationBarItem(
                                selected = selected,
                                onClick = {
                                    navController.navigate(screen.route) {
                                        popUpTo(navController.graph.findStartDestination().id) { saveState = true }
                                        launchSingleTop = true
                                        restoreState = true
                                    }
                                },
                                icon = { Icon(screen.icon, contentDescription = screen.label) },
                                label = { Text(screen.label) }
                            )
                        }
                    }
                }
                // Pied de page toujours visible, tout en bas de l'application
                AppFooter()
            }
        }
    ) { padding ->
        NavHost(
            navController = navController,
            startDestination = Screen.Dashboard.route,
            modifier = Modifier.padding(padding)
        ) {
            composable(Screen.Dashboard.route) {
                DashboardScreen(
                    viewModel = viewModel,
                    onAddRide = { navController.navigate("add_ride") },
                    onOpenRide = { id -> navController.navigate("edit_ride/$id") },
                    onOpenReports = { navController.navigate("reports") }
                )
            }
            composable(Screen.Rides.route) {
                RidesListScreen(
                    viewModel = viewModel,
                    onOpenRide = { id -> navController.navigate("edit_ride/$id") }
                )
            }
            composable("reports") {
                ReportsScreen(
                    viewModel = viewModel,
                    onBack = { navController.popBackStack() }
                )
            }
            composable("add_ride") {
                AddRideScreen(
                    existingRide = null,
                    onSave = { ride ->
                        viewModel.addRide(ride)
                        navController.popBackStack()
                    },
                    onBack = { navController.popBackStack() }
                )
            }
            composable("edit_ride/{rideId}") { backStackEntry ->
                val rideId = backStackEntry.arguments?.getString("rideId")?.toLongOrNull()
                val rides by viewModel.allRides.collectAsState()
                val ride = rides.find { it.id == rideId }
                AddRideScreen(
                    existingRide = ride,
                    onSave = { updated ->
                        viewModel.updateRide(updated)
                        navController.popBackStack()
                    },
                    onDelete = { toDelete ->
                        viewModel.deleteRide(toDelete)
                        navController.popBackStack()
                    },
                    onBack = { navController.popBackStack() }
                )
            }
        }
    }
}

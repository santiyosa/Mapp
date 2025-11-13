package com.maintenance.app.presentation.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import com.maintenance.app.presentation.ui.screens.home.HomeScreen
import com.maintenance.app.presentation.ui.screens.search.SearchScreen
import com.maintenance.app.presentation.screens.SettingsScreen
import com.maintenance.app.presentation.ui.screens.detail.RecordDetailScreen
import com.maintenance.app.presentation.ui.screens.create.CreateRecordScreen
import com.maintenance.app.presentation.ui.screens.edit.EditRecordScreen
import com.maintenance.app.presentation.ui.screens.maintenance.create.CreateMaintenanceScreen
import com.maintenance.app.presentation.ui.screens.maintenance.edit.EditMaintenanceScreen

/**
 * Main navigation host for the app.
 */
@Composable
fun MainNavHost(
    navController: NavHostController,
    startDestination: String = Screen.Home.route
) {
    NavHost(
        navController = navController,
        startDestination = startDestination
    ) {
        // Bottom navigation screens
        composable(Screen.Home.route) {
            HomeScreen(navController = navController)
        }
        
        composable(Screen.Search.route) {
            SearchScreen(
                onNavigateToRecord = { recordId ->
                    navController.navigate(Screen.RecordDetail.createRoute(recordId))
                },
                onNavigateToMaintenance = { maintenanceId ->
                    navController.navigate(Screen.MaintenanceDetail.createRoute(maintenanceId))
                },
                onNavigateBack = {
                    navController.popBackStack()
                }
            )
        }
        
        composable(Screen.Settings.route) {
            SettingsScreen(
                onNavigateBack = {
                    navController.popBackStack()
                }
            )
        }
        
        // Record screens
        composable(
            route = Screen.RecordDetail.route,
            arguments = listOf(
                navArgument(Screen.RECORD_ID_ARG) {
                    type = NavType.LongType
                }
            )
        ) { backStackEntry ->
            val recordId = backStackEntry.arguments?.getLong(Screen.RECORD_ID_ARG) ?: 0L
            RecordDetailScreen(
                recordId = recordId,
                navController = navController
            )
        }
        
        composable(Screen.CreateRecord.route) {
            CreateRecordScreen(navController = navController)
        }
        
        composable(
            route = Screen.EditRecord.route,
            arguments = listOf(
                navArgument(Screen.RECORD_ID_ARG) {
                    type = NavType.LongType
                }
            )
        ) { backStackEntry ->
            val recordId = backStackEntry.arguments?.getLong(Screen.RECORD_ID_ARG) ?: 0L
            EditRecordScreen(
                recordId = recordId,
                navController = navController
            )
        }
        
        // Maintenance screens
        composable(
            route = Screen.CreateMaintenance.route,
            arguments = listOf(
                navArgument(Screen.RECORD_ID_ARG) {
                    type = NavType.LongType
                }
            )
        ) { backStackEntry ->
            val recordId = backStackEntry.arguments?.getLong(Screen.RECORD_ID_ARG) ?: 0L
            CreateMaintenanceScreen(
                recordId = recordId,
                navController = navController
            )
        }

        // Edit maintenance screen
        composable(
            route = Screen.EditMaintenance.route,
            arguments = listOf(
                navArgument(Screen.MAINTENANCE_ID_ARG) {
                    type = NavType.LongType
                }
            )
        ) { backStackEntry ->
            val maintenanceId = backStackEntry.arguments?.getLong(Screen.MAINTENANCE_ID_ARG) ?: 0L
            EditMaintenanceScreen(
                navController = navController,
                maintenanceId = maintenanceId
            )
        }

    }
}
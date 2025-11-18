package com.maintenance.app.presentation.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import com.maintenance.app.presentation.ui.screens.home.HomeScreen
import com.maintenance.app.presentation.ui.screens.search.SearchScreenAdvanced
import com.maintenance.app.presentation.screens.SettingsScreen
import com.maintenance.app.presentation.ui.screens.detail.RecordDetailScreen
import com.maintenance.app.presentation.ui.screens.create.CreateRecordScreen
import com.maintenance.app.presentation.ui.screens.edit.EditRecordScreenSimple
import com.maintenance.app.presentation.ui.screens.maintenance.create.CreateMaintenanceScreenSimple
import com.maintenance.app.presentation.ui.screens.maintenance.edit.EditMaintenanceScreen
import com.maintenance.app.presentation.ui.screens.backup.BackupScreen

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
            SearchScreenAdvanced(navController = navController)
        }
        
        composable(Screen.Settings.route) {
            SettingsScreen(
                navController = navController,
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
            EditRecordScreenSimple(
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
            CreateMaintenanceScreenSimple(
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

        // Maintenance detail screen
        composable(
            route = Screen.MaintenanceDetail.route,
            arguments = listOf(
                navArgument(Screen.MAINTENANCE_ID_ARG) {
                    type = NavType.LongType
                }
            )
        ) { backStackEntry ->
            val maintenanceId = backStackEntry.arguments?.getLong(Screen.MAINTENANCE_ID_ARG) ?: 0L
            // For now, navigate back or show a placeholder
            // TODO: Implement MaintenanceDetailScreen if needed
            navController.popBackStack()
        }

        // Backup screen
        composable(Screen.Backup.route) {
            BackupScreen()
        }
    }
}
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
import com.maintenance.app.presentation.ui.screens.maintenance.create.CreateMaintenanceScreen
import com.maintenance.app.presentation.ui.screens.maintenance.edit.EditMaintenanceScreen
import com.maintenance.app.presentation.ui.screens.backup.BackupScreen
import com.maintenance.app.presentation.ui.screens.trash.TrashScreen

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
        
        composable(
            route = Screen.Search.route,
            arguments = listOf(
                navArgument(Screen.RECORD_ID_ARG) {
                    type = NavType.LongType
                    defaultValue = -1L
                }
            )
        ) { backStackEntry ->
            val recordId = backStackEntry.arguments?.getLong(Screen.RECORD_ID_ARG) ?: -1L
            SearchScreenAdvanced(
                navController = navController,
                recordId = if (recordId > 0) recordId else null
            )
        }
        
        composable(Screen.Settings.route) {
            SettingsScreen(
                navController = navController,
                onNavigateBack = {
                    navController.popBackStack()
                }
            )
        }
        
        composable("trash") {
            TrashScreen(navController = navController)
        }
        
        // Record screens
        composable(
            route = Screen.RecordDetail.route,
            arguments = listOf(
                navArgument(Screen.RECORD_ID_ARG) {
                    type = NavType.LongType
                    defaultValue = -1L
                },
                navArgument("maintenanceId") {
                    type = NavType.LongType
                    defaultValue = -1L
                }
            )
        ) { backStackEntry ->
            val recordId = backStackEntry.arguments?.getLong(Screen.RECORD_ID_ARG) ?: 0L
            val maintenanceId = backStackEntry.arguments?.getLong("maintenanceId") ?: -1L
            RecordDetailScreen(
                recordId = recordId,
                navController = navController,
                initialMaintenanceId = if (maintenanceId > 0) maintenanceId else null
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

        // Backup screen
        composable(Screen.Backup.route) {
            BackupScreen(
                onNavigateBack = {
                    navController.popBackStack()
                }
            )
        }
    }
}
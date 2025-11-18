package com.maintenance.app.presentation.navigation

import android.util.Log
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.outlined.Home
import androidx.compose.material.icons.outlined.Search
import androidx.compose.material.icons.outlined.Settings
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import androidx.navigation.NavController
import androidx.navigation.compose.currentBackStackEntryAsState
import com.maintenance.app.R
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

/**
 * Data class representing a bottom navigation item.
 */
data class BottomNavItem(
    val screen: Screen,
    val titleResId: Int,
    val selectedIcon: ImageVector,
    val unselectedIcon: ImageVector
)

/**
 * Bottom navigation items configuration.
 */
val bottomNavItems = listOf(
    BottomNavItem(
        screen = Screen.Home,
        titleResId = R.string.nav_home,
        selectedIcon = Icons.Filled.Home,
        unselectedIcon = Icons.Outlined.Home
    ),
    BottomNavItem(
        screen = Screen.Search,
        titleResId = R.string.nav_search,
        selectedIcon = Icons.Filled.Search,
        unselectedIcon = Icons.Outlined.Search
    ),
    BottomNavItem(
        screen = Screen.Settings,
        titleResId = R.string.nav_settings,
        selectedIcon = Icons.Filled.Settings,
        unselectedIcon = Icons.Outlined.Settings
    )
)

@Composable
fun MainBottomNavigation(
    navController: NavController,
    recordId: Long? = null
) {
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentDestination = navBackStackEntry?.destination
    val currentRoute = currentDestination?.route ?: ""
    val coroutineScope = rememberCoroutineScope()
    
    // Use passed recordId if available, otherwise try to extract from arguments
    val contextRecordId = recordId ?: navBackStackEntry?.arguments?.getLong(Screen.RECORD_ID_ARG, -1L)
        ?.takeIf { it > 0 }

    Log.d("BottomNav", "🔴 RENDER: currentRoute=$currentRoute, contextRecordId=$contextRecordId, recordId=$recordId")

    NavigationBar {
        // Home button
        val homeItem = bottomNavItems[0]
        val homeSelected = currentRoute.startsWith("home")
        NavigationBarItem(
            icon = {
                Icon(
                    imageVector = if (homeSelected) homeItem.selectedIcon else homeItem.unselectedIcon,
                    contentDescription = stringResource(homeItem.titleResId)
                )
            },
            label = { Text(stringResource(homeItem.titleResId)) },
            selected = homeSelected,
            alwaysShowLabel = false,
            onClick = {
                Log.d("BottomNav", "🔴 Home clicked")
                navController.navigate(Screen.Home.route) {
                    popUpTo(Screen.Home.route) { inclusive = true }
                    launchSingleTop = true
                }
            }
        )
        
        // Search button - EXPLICIT
        val searchItem = bottomNavItems[1]
        val searchSelected = currentRoute.startsWith("search")
        Log.d("BottomNav", "🔴 Search item - selected=$searchSelected, contextRecordId=$contextRecordId")
        NavigationBarItem(
            icon = {
                Icon(
                    imageVector = if (searchSelected) searchItem.selectedIcon else searchItem.unselectedIcon,
                    contentDescription = stringResource(searchItem.titleResId)
                )
            },
            label = { Text(stringResource(searchItem.titleResId)) },
            selected = searchSelected,
            alwaysShowLabel = false,
            onClick = {
                Log.d("BottomNav", "🔴🔴🔴 Search CLICKED! contextRecordId=$contextRecordId")
                coroutineScope.launch {
                    try {
                        val route = if (contextRecordId != null && contextRecordId > 0) {
                            "search?recordId=$contextRecordId"
                        } else {
                            "search"
                        }
                        Log.d("BottomNav", "🔴🔴🔴 Attempting navigation to: $route")
                        
                        // Primero, navega siempre a Home para limpiar el stack
                        navController.navigate(Screen.Home.route) {
                            popUpTo(Screen.Home.route) { inclusive = true }
                        }
                        
                        // Luego, después de un pequeño delay, navega a Search
                        delay(100)
                        navController.navigate(route) {
                            popUpTo(Screen.Home.route) { 
                                inclusive = false
                                saveState = true
                            }
                            launchSingleTop = false
                            restoreState = true
                        }
                        Log.d("BottomNav", "🔴🔴🔴 Navigation SUCCESS to: $route")
                    } catch (e: Exception) {
                        Log.e("BottomNav", "🔴🔴🔴 Navigation FAILED", e)
                        e.printStackTrace()
                    }
                }
            }
        )
        
        // Settings button
        val settingsItem = bottomNavItems[2]
        val settingsSelected = currentRoute.startsWith("settings")
        NavigationBarItem(
            icon = {
                Icon(
                    imageVector = if (settingsSelected) settingsItem.selectedIcon else settingsItem.unselectedIcon,
                    contentDescription = stringResource(settingsItem.titleResId)
                )
            },
            label = { Text(stringResource(settingsItem.titleResId)) },
            selected = settingsSelected,
            alwaysShowLabel = false,
            onClick = {
                Log.d("BottomNav", "🔴 Settings clicked")
                navController.navigate(Screen.Settings.route) {
                    popUpTo(Screen.Home.route) { saveState = true }
                    launchSingleTop = true
                    restoreState = true
                }
            }
        )
    }
}
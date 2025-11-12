package com.maintenance.app.presentation.navigation

/**
 * Sealed class representing the navigation routes in the app.
 */
sealed class Screen(val route: String) {
    
    // Main screens
    object Home : Screen("home")
    object Search : Screen("search")
    object Settings : Screen("settings")
    
    // Record screens
    object RecordDetail : Screen("record_detail/{recordId}") {
        fun createRoute(recordId: Long) = "record_detail/$recordId"
    }
    
    object CreateRecord : Screen("create_record")
    
    object EditRecord : Screen("edit_record/{recordId}") {
        fun createRoute(recordId: Long) = "edit_record/$recordId"
    }
    
    // Maintenance screens
    object CreateMaintenance : Screen("create_maintenance/{recordId}") {
        fun createRoute(recordId: Long) = "create_maintenance/$recordId"
    }
    
    object EditMaintenance : Screen("edit_maintenance/{maintenanceId}") {
        fun createRoute(maintenanceId: Long) = "edit_maintenance/$maintenanceId"
    }
    
    object MaintenanceDetail : Screen("maintenance_detail/{maintenanceId}") {
        fun createRoute(maintenanceId: Long) = "maintenance_detail/$maintenanceId"
    }
    
    // Authentication screens (for future use)
    object BiometricAuth : Screen("biometric_auth")
    
    companion object {
        /**
         * List of bottom navigation screens.
         */
        val bottomNavScreens = listOf(Home, Search, Settings)
        
        /**
         * Navigation arguments constants.
         */
        const val RECORD_ID_ARG = "recordId"
        const val MAINTENANCE_ID_ARG = "maintenanceId"
    }
}
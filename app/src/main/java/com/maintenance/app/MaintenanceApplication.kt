package com.maintenance.app

import android.app.Application
import dagger.hilt.android.HiltAndroidApp

/**
 * Application class for the Maintenance App.
 * This class is annotated with @HiltAndroidApp to enable Hilt dependency injection.
 */
@HiltAndroidApp
class MaintenanceApplication : Application() {
    
    override fun onCreate() {
        super.onCreate()
        // Initialize any necessary components here
    }
}
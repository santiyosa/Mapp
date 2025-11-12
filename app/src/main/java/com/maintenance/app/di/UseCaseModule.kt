package com.maintenance.app.di

import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.android.components.ViewModelComponent

/**
 * Hilt module for Use Case dependencies.
 * Use Cases are provided at ViewModelComponent scope since they're typically used by ViewModels.
 */
@Module
@InstallIn(ViewModelComponent::class)
object UseCaseModule {
    
    // All Use Cases are provided implicitly through constructor injection
    // since they're annotated with @Inject.
    // This module exists for future explicit bindings if needed.
}
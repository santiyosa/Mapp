package com.maintenance.app.di

import android.content.Context
import com.google.gson.Gson
import com.google.gson.GsonBuilder
import com.maintenance.app.utils.GoogleDriveService
import com.maintenance.app.utils.LocalBackupService
import com.maintenance.app.utils.ShareManager
import com.maintenance.app.utils.PermissionManager
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

/**
 * Hilt module for utility dependencies.
 */
@Module
@InstallIn(SingletonComponent::class)
object UtilityModule {

    @Provides
    @Singleton
    fun provideContext(@ApplicationContext context: Context): Context {
        return context
    }

    @Provides
    @Singleton
    fun provideGson(): Gson {
        return GsonBuilder()
            .setDateFormat("yyyy-MM-dd HH:mm:ss")
            .create()
    }

    @Provides
    @Singleton
    fun provideShareManager(@ApplicationContext context: Context): ShareManager {
        return ShareManager(context)
    }

    @Provides
    @Singleton
    fun providePermissionManager(@ApplicationContext context: Context): PermissionManager {
        return PermissionManager(context)
    }

    @Provides
    @Singleton
    fun provideGoogleDriveService(@ApplicationContext context: Context): GoogleDriveService {
        return GoogleDriveService(context)
    }

    @Provides
    @Singleton
    fun provideLocalBackupService(@ApplicationContext context: Context): LocalBackupService {
        return LocalBackupService(context)
    }
}
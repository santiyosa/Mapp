package com.maintenance.app.di

import android.content.Context
import androidx.room.Room
import com.maintenance.app.data.database.MaintenanceDatabase
import com.maintenance.app.data.database.dao.AppSettingsDAO
import com.maintenance.app.data.database.dao.MaintenanceDAO
import com.maintenance.app.data.database.dao.RecordDAO
import com.maintenance.app.data.database.dao.UISettingsDAO
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

/**
 * Hilt module for database-related dependencies.
 */
@Module
@InstallIn(SingletonComponent::class)
object DatabaseModule {

    @Provides
    @Singleton
    fun provideMaintenanceDatabase(
        @ApplicationContext context: Context
    ): MaintenanceDatabase {
        return MaintenanceDatabase.getDatabase(context)
    }

    @Provides
    fun provideRecordDAO(database: MaintenanceDatabase): RecordDAO {
        return database.recordDao()
    }

    @Provides
    fun provideMaintenanceDAO(database: MaintenanceDatabase): MaintenanceDAO {
        return database.maintenanceDao()
    }

    @Provides
    fun provideUISettingsDAO(database: MaintenanceDatabase): UISettingsDAO {
        return database.uiSettingsDao()
    }

    @Provides
    fun provideAppSettingsDAO(database: MaintenanceDatabase): AppSettingsDAO {
        return database.appSettingsDao()
    }
}
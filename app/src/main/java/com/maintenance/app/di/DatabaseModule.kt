package com.maintenance.app.di

import android.content.Context
import androidx.room.Room
import com.maintenance.app.data.database.MaintenanceDatabase
import com.maintenance.app.data.database.dao.AppSettingsDAO
import com.maintenance.app.data.database.dao.MaintenanceDAO
import com.maintenance.app.data.database.dao.RecordDAO
import com.maintenance.app.data.database.dao.SearchDAO
import com.maintenance.app.data.database.dao.SearchHistoryDAO
import com.maintenance.app.data.database.dao.SettingsDao
import com.maintenance.app.data.database.dao.UISettingsDAO
import com.maintenance.app.data.database.daos.MaintenanceDraftDao
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

    @Provides
    fun provideMaintenanceDraftDao(database: MaintenanceDatabase): MaintenanceDraftDao {
        return database.maintenanceDraftDao()
    }

    @Provides
    fun provideSearchDao(database: MaintenanceDatabase): SearchDAO {
        return database.searchDao()
    }

    @Provides
    fun provideSearchHistoryDao(database: MaintenanceDatabase): SearchHistoryDAO {
        return database.searchHistoryDao()
    }

    @Provides
    fun provideSettingsDao(database: MaintenanceDatabase): SettingsDao {
        return database.settingsDao()
    }
}
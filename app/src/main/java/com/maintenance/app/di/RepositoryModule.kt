package com.maintenance.app.di

import com.maintenance.app.data.repositories.BackupRepositoryImpl
import com.maintenance.app.data.repositories.MaintenanceDraftRepositoryImpl
import com.maintenance.app.data.repositories.SearchRepositoryImpl
import com.maintenance.app.data.repositories.SettingsRepositoryImpl
import com.maintenance.app.data.repository.MaintenanceRepositoryImpl
import com.maintenance.app.data.repository.RecordRepositoryImpl
import com.maintenance.app.domain.repositories.BackupRepository
import com.maintenance.app.domain.repository.MaintenanceDraftRepository
import com.maintenance.app.domain.repository.MaintenanceRepository
import com.maintenance.app.domain.repository.RecordRepository
import com.maintenance.app.domain.repository.SearchRepository
import com.maintenance.app.domain.repository.SettingsRepository
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

/**
 * Hilt module for repository dependencies.
 * This module binds repository interfaces to their implementations.
 */
@Module
@InstallIn(SingletonComponent::class)
abstract class RepositoryModule {

    @Binds
    @Singleton
    abstract fun bindRecordRepository(
        recordRepositoryImpl: RecordRepositoryImpl
    ): RecordRepository

    @Binds
    @Singleton
    abstract fun bindMaintenanceRepository(
        maintenanceRepositoryImpl: MaintenanceRepositoryImpl
    ): MaintenanceRepository

    @Binds
    @Singleton
    abstract fun bindMaintenanceDraftRepository(
        maintenanceDraftRepositoryImpl: MaintenanceDraftRepositoryImpl
    ): MaintenanceDraftRepository

    @Binds
    @Singleton
    abstract fun bindSearchRepository(
        searchRepositoryImpl: SearchRepositoryImpl
    ): SearchRepository

    @Binds
    @Singleton
    abstract fun bindSettingsRepository(
        settingsRepositoryImpl: SettingsRepositoryImpl
    ): SettingsRepository

    @Binds
    @Singleton
    abstract fun bindBackupRepository(
        backupRepositoryImpl: BackupRepositoryImpl
    ): BackupRepository
}
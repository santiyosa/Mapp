package com.maintenance.app.di

import com.maintenance.app.data.repository.MaintenanceRepositoryImpl
import com.maintenance.app.data.repository.RecordRepositoryImpl
import com.maintenance.app.domain.repository.MaintenanceRepository
import com.maintenance.app.domain.repository.RecordRepository
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
}
package com.maintenance.app.di

import com.maintenance.app.domain.repositories.BackupRepository
import com.maintenance.app.domain.usecases.backup.CheckGoogleDriveAuthUseCase
import com.maintenance.app.domain.usecases.backup.CreateBackupUseCase
import com.maintenance.app.domain.usecases.backup.DeleteBackupUseCase
import com.maintenance.app.domain.usecases.backup.GetAvailableStorageUseCase
import com.maintenance.app.domain.usecases.backup.GetBackupListUseCase
import com.maintenance.app.domain.usecases.backup.GetBackupScheduleUseCase
import com.maintenance.app.domain.usecases.backup.GetLocalDatabaseSizeUseCase
import com.maintenance.app.domain.usecases.backup.RestoreBackupUseCase
import com.maintenance.app.domain.usecases.backup.UpdateBackupScheduleUseCase
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

/**
 * Hilt module for Use Case dependencies.
 * Use Cases are provided at SingletonComponent scope to be shared across the app.
 */
@Module
@InstallIn(SingletonComponent::class)
object UseCaseModule {
    
    @Provides
    @Singleton
    fun provideCreateBackupUseCase(
        backupRepository: BackupRepository
    ): CreateBackupUseCase {
        return CreateBackupUseCase(backupRepository)
    }

    @Provides
    @Singleton
    fun provideRestoreBackupUseCase(
        backupRepository: BackupRepository
    ): RestoreBackupUseCase {
        return RestoreBackupUseCase(backupRepository)
    }

    @Provides
    @Singleton
    fun provideGetBackupListUseCase(
        backupRepository: BackupRepository
    ): GetBackupListUseCase {
        return GetBackupListUseCase(backupRepository)
    }

    @Provides
    @Singleton
    fun provideDeleteBackupUseCase(
        backupRepository: BackupRepository
    ): DeleteBackupUseCase {
        return DeleteBackupUseCase(backupRepository)
    }

    @Provides
    @Singleton
    fun provideUpdateBackupScheduleUseCase(
        backupRepository: BackupRepository
    ): UpdateBackupScheduleUseCase {
        return UpdateBackupScheduleUseCase(backupRepository)
    }

    @Provides
    @Singleton
    fun provideGetBackupScheduleUseCase(
        backupRepository: BackupRepository
    ): GetBackupScheduleUseCase {
        return GetBackupScheduleUseCase(backupRepository)
    }

    @Provides
    @Singleton
    fun provideCheckGoogleDriveAuthUseCase(
        backupRepository: BackupRepository
    ): CheckGoogleDriveAuthUseCase {
        return CheckGoogleDriveAuthUseCase(backupRepository)
    }

    @Provides
    @Singleton
    fun provideGetAvailableStorageUseCase(
        backupRepository: BackupRepository
    ): GetAvailableStorageUseCase {
        return GetAvailableStorageUseCase(backupRepository)
    }

    @Provides
    @Singleton
    fun provideGetLocalDatabaseSizeUseCase(
        backupRepository: BackupRepository
    ): GetLocalDatabaseSizeUseCase {
        return GetLocalDatabaseSizeUseCase(backupRepository)
    }
}
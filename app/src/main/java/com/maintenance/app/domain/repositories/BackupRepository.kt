package com.maintenance.app.domain.repositories

import com.maintenance.app.domain.model.BackupConfig
import com.maintenance.app.domain.model.BackupMetadata
import com.maintenance.app.domain.model.BackupSchedule
import com.maintenance.app.utils.Result

/**
 * Repository interface for backup operations.
 */
interface BackupRepository {

    /**
     * Create a new backup of the database.
     */
    suspend fun createBackup(
        backupName: String,
        encryptionEnabled: Boolean = true
    ): Result<BackupConfig>

    /**
     * Restore a backup from Google Drive.
     */
    suspend fun restoreBackup(backupId: String): Result<Unit>

    /**
     * Get list of all available backups.
     */
    suspend fun getBackupList(): Result<List<BackupMetadata>>

    /**
     * Delete a backup from Google Drive.
     */
    suspend fun deleteBackup(backupId: String): Result<Unit>

    /**
     * Get current backup schedule configuration.
     */
    suspend fun getBackupSchedule(): Result<BackupSchedule>

    /**
     * Update backup schedule configuration.
     */
    suspend fun updateBackupSchedule(schedule: BackupSchedule): Result<Unit>

    /**
     * Check if Google Drive is authenticated.
     */
    suspend fun isGoogleDriveAuthenticated(): Result<Boolean>

    /**
     * Get available storage space on Google Drive (in bytes).
     */
    suspend fun getAvailableStorage(): Result<Long>

    /**
     * Get the size of the local database (in bytes).
     */
    suspend fun getLocalDatabaseSize(): Result<Long>
}

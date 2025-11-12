package com.maintenance.app.data.database.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.maintenance.app.data.database.entities.AppSettingsEntity
import kotlinx.coroutines.flow.Flow
import java.time.LocalDateTime

/**
 * Data Access Object for App Settings operations.
 */
@Dao
interface AppSettingsDAO {

    @Query("SELECT * FROM app_settings WHERE id = 1")
    fun getAppSettings(): Flow<AppSettingsEntity?>

    @Query("SELECT * FROM app_settings WHERE id = 1")
    suspend fun getAppSettingsOnce(): AppSettingsEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAppSettings(settings: AppSettingsEntity)

    @Update
    suspend fun updateAppSettings(settings: AppSettingsEntity)

    @Query("UPDATE app_settings SET biometric_enabled = :enabled WHERE id = 1")
    suspend fun updateBiometricEnabled(enabled: Boolean)

    @Query("UPDATE app_settings SET biometric_timeout_minutes = :timeoutMinutes WHERE id = 1")
    suspend fun updateBiometricTimeout(timeoutMinutes: Int)

    @Query("UPDATE app_settings SET auto_lock_enabled = :enabled WHERE id = 1")
    suspend fun updateAutoLockEnabled(enabled: Boolean)

    @Query("UPDATE app_settings SET data_encryption_enabled = :enabled WHERE id = 1")
    suspend fun updateDataEncryptionEnabled(enabled: Boolean)

    @Query("UPDATE app_settings SET google_drive_sync_enabled = :enabled WHERE id = 1")
    suspend fun updateGoogleDriveSyncEnabled(enabled: Boolean)

    @Query("UPDATE app_settings SET google_drive_account_email = :email WHERE id = 1")
    suspend fun updateGoogleDriveAccountEmail(email: String?)

    @Query("UPDATE app_settings SET last_backup_date = :date WHERE id = 1")
    suspend fun updateLastBackupDate(date: LocalDateTime?)

    @Query("UPDATE app_settings SET last_sync_date = :date WHERE id = 1")
    suspend fun updateLastSyncDate(date: LocalDateTime?)

    @Query("UPDATE app_settings SET backup_file_count = :count WHERE id = 1")
    suspend fun updateBackupFileCount(count: Int)

    @Query("UPDATE app_settings SET total_records_count = :count WHERE id = 1")
    suspend fun updateTotalRecordsCount(count: Long)

    @Query("UPDATE app_settings SET total_maintenances_count = :count WHERE id = 1")
    suspend fun updateTotalMaintenancesCount(count: Long)

    @Query("UPDATE app_settings SET database_size_mb = :sizeMb WHERE id = 1")
    suspend fun updateDatabaseSize(sizeMb: Float)

    @Query("UPDATE app_settings SET images_size_mb = :sizeMb WHERE id = 1")
    suspend fun updateImagesSize(sizeMb: Float)

    @Query("UPDATE app_settings SET app_version = :version WHERE id = 1")
    suspend fun updateAppVersion(version: String)

    @Query("UPDATE app_settings SET database_version = :version WHERE id = 1")
    suspend fun updateDatabaseVersion(version: Int)

    @Query("UPDATE app_settings SET analytics_enabled = :enabled WHERE id = 1")
    suspend fun updateAnalyticsEnabled(enabled: Boolean)

    @Query("UPDATE app_settings SET crash_reporting_enabled = :enabled WHERE id = 1")
    suspend fun updateCrashReportingEnabled(enabled: Boolean)

    @Query("UPDATE app_settings SET share_template_record = :template WHERE id = 1")
    suspend fun updateShareTemplateRecord(template: String?)

    @Query("UPDATE app_settings SET share_template_maintenance = :template WHERE id = 1")
    suspend fun updateShareTemplateMaintenance(template: String?)

    @Query("UPDATE app_settings SET notification_enabled = :enabled WHERE id = 1")
    suspend fun updateNotificationEnabled(enabled: Boolean)

    @Query("UPDATE app_settings SET notification_reminder_days = :days WHERE id = 1")
    suspend fun updateNotificationReminderDays(days: Int)

    @Query("UPDATE app_settings SET export_include_images = :includeImages WHERE id = 1")
    suspend fun updateExportIncludeImages(includeImages: Boolean)

    @Query("UPDATE app_settings SET maintenance_categories = :categories WHERE id = 1")
    suspend fun updateMaintenanceCategories(categories: String?)

    @Query("DELETE FROM app_settings")
    suspend fun deleteAllAppSettings()
}
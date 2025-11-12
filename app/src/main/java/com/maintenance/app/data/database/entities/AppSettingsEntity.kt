package com.maintenance.app.data.database.entities

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import java.time.LocalDateTime

/**
 * Room entity for storing application-level settings and configuration.
 */
@Entity(tableName = "app_settings")
data class AppSettingsEntity(
    @PrimaryKey
    @ColumnInfo(name = "id")
    val id: Long = 1, // Single row settings

    @ColumnInfo(name = "biometric_enabled")
    val biometricEnabled: Boolean = false,

    @ColumnInfo(name = "biometric_timeout_minutes")
    val biometricTimeoutMinutes: Int = 15,

    @ColumnInfo(name = "auto_lock_enabled")
    val autoLockEnabled: Boolean = false,

    @ColumnInfo(name = "data_encryption_enabled")
    val dataEncryptionEnabled: Boolean = false,

    @ColumnInfo(name = "google_drive_sync_enabled")
    val googleDriveSyncEnabled: Boolean = false,

    @ColumnInfo(name = "google_drive_account_email")
    val googleDriveAccountEmail: String? = null,

    @ColumnInfo(name = "last_backup_date")
    val lastBackupDate: LocalDateTime? = null,

    @ColumnInfo(name = "last_sync_date")
    val lastSyncDate: LocalDateTime? = null,

    @ColumnInfo(name = "backup_file_count")
    val backupFileCount: Int = 0,

    @ColumnInfo(name = "total_records_count")
    val totalRecordsCount: Long = 0,

    @ColumnInfo(name = "total_maintenances_count")
    val totalMaintenancesCount: Long = 0,

    @ColumnInfo(name = "database_size_mb")
    val databaseSizeMb: Float = 0.0f,

    @ColumnInfo(name = "images_size_mb")
    val imagesSizeMb: Float = 0.0f,

    @ColumnInfo(name = "app_version")
    val appVersion: String,

    @ColumnInfo(name = "database_version")
    val databaseVersion: Int,

    @ColumnInfo(name = "first_install_date")
    val firstInstallDate: LocalDateTime,

    @ColumnInfo(name = "analytics_enabled")
    val analyticsEnabled: Boolean = false,

    @ColumnInfo(name = "crash_reporting_enabled")
    val crashReportingEnabled: Boolean = true,

    @ColumnInfo(name = "share_template_record")
    val shareTemplateRecord: String? = null, // Template for sharing records

    @ColumnInfo(name = "share_template_maintenance")
    val shareTemplateMaintenance: String? = null, // Template for sharing maintenances

    @ColumnInfo(name = "notification_enabled")
    val notificationEnabled: Boolean = true,

    @ColumnInfo(name = "notification_reminder_days")
    val notificationReminderDays: Int = 7, // Days before maintenance due

    @ColumnInfo(name = "export_include_images")
    val exportIncludeImages: Boolean = true,

    @ColumnInfo(name = "maintenance_categories")
    val maintenanceCategories: String? = null, // JSON array of custom categories

    @ColumnInfo(name = "created_date")
    val createdDate: LocalDateTime,

    @ColumnInfo(name = "updated_date")
    val updatedDate: LocalDateTime
)
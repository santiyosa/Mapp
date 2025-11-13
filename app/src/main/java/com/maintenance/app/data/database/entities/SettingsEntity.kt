package com.maintenance.app.data.database.entities

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import com.maintenance.app.domain.model.AppLanguage
import com.maintenance.app.domain.model.AppSettings
import com.maintenance.app.domain.model.ThemeMode

/**
 * Room entity for app settings.
 */
@Entity(tableName = "user_app_settings")
data class SettingsEntity(
    @PrimaryKey
    val id: Long = 1,
    
    @ColumnInfo(name = "theme_mode")
    val themeMode: String = ThemeMode.SYSTEM.name,
    
    @ColumnInfo(name = "language")
    val language: String = AppLanguage.SPANISH.name,
    
    @ColumnInfo(name = "enable_notifications")
    val enableNotifications: Boolean = true,
    
    @ColumnInfo(name = "enable_biometric")
    val enableBiometric: Boolean = false,
    
    @ColumnInfo(name = "enable_auto_backup")
    val enableAutoBackup: Boolean = true,
    
    @ColumnInfo(name = "backup_frequency_days")
    val backupFrequency: Int = 7,
    
    @ColumnInfo(name = "enable_data_collection")
    val enableDataCollection: Boolean = false,
    
    @ColumnInfo(name = "last_updated")
    val lastUpdated: Long = System.currentTimeMillis()
) {
    fun toDomain(): AppSettings {
        return AppSettings(
            id = id,
            themeMode = ThemeMode.valueOf(themeMode),
            language = AppLanguage.valueOf(language),
            enableNotifications = enableNotifications,
            enableBiometric = enableBiometric,
            enableAutoBackup = enableAutoBackup,
            backupFrequency = backupFrequency,
            enableDataCollection = enableDataCollection,
            lastUpdated = lastUpdated
        )
    }
}

fun AppSettings.toEntity(): SettingsEntity {
    return SettingsEntity(
        id = id,
        themeMode = themeMode.name,
        language = language.name,
        enableNotifications = enableNotifications,
        enableBiometric = enableBiometric,
        enableAutoBackup = enableAutoBackup,
        backupFrequency = backupFrequency,
        enableDataCollection = enableDataCollection,
        lastUpdated = lastUpdated
    )
}

package com.maintenance.app.domain.model

/**
 * Theme mode enumeration for user preference.
 */
enum class ThemeMode {
    LIGHT,
    DARK,
    SYSTEM
}

/**
 * Language enumeration for app localization.
 */
enum class AppLanguage {
    SPANISH,
    ENGLISH,
    PORTUGUESE
}

/**
 * Data class representing application settings.
 */
data class AppSettings(
    val id: Long = 1,
    val themeMode: ThemeMode = ThemeMode.SYSTEM,
    val language: AppLanguage = AppLanguage.SPANISH,
    val enableNotifications: Boolean = true,
    val enableBiometric: Boolean = false,
    val enableAutoBackup: Boolean = true,
    val backupFrequency: Int = 7, // days
    val enableDataCollection: Boolean = false,
    val lastUpdated: Long = System.currentTimeMillis()
)

/**
 * User profile settings.
 */
data class UserSettings(
    val userId: String = "default",
    val userName: String = "",
    val userEmail: String = "",
    val profilePicture: String? = null,
    val createdDate: Long = System.currentTimeMillis(),
    val lastModified: Long = System.currentTimeMillis()
)

/**
 * Privacy settings.
 */
data class PrivacySettings(
    val analyticsEnabled: Boolean = false,
    val crashReportsEnabled: Boolean = false,
    val locationServicesEnabled: Boolean = false,
    val cameraPermission: Boolean = false,
    val storagePermission: Boolean = false
)

/**
 * Notification preferences.
 */
data class NotificationSettings(
    val maintenanceReminders: Boolean = true,
    val maintenanceReminderDaysBefore: Int = 7,
    val categoryNotifications: Boolean = true,
    val soundEnabled: Boolean = true,
    val vibrationEnabled: Boolean = true,
    val quietHoursStart: String = "22:00",
    val quietHoursEnd: String = "08:00",
    val quietHoursEnabled: Boolean = false
)

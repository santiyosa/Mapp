package com.maintenance.app.domain.repository

import com.maintenance.app.domain.model.AppSettings
import com.maintenance.app.domain.model.NotificationSettings
import com.maintenance.app.domain.model.PrivacySettings
import com.maintenance.app.domain.model.UserSettings
import com.maintenance.app.utils.Result
import kotlinx.coroutines.flow.Flow

/**
 * Repository interface for managing application settings.
 */
interface SettingsRepository {
    
    /**
     * Get current app settings as a Flow for reactive updates.
     */
    fun getSettingsFlow(): Flow<AppSettings>
    
    /**
     * Get current app settings (suspend function).
     */
    suspend fun getSettings(): Result<AppSettings>
    
    /**
     * Update application settings.
     */
    suspend fun updateSettings(settings: AppSettings): Result<Unit>
    
    /**
     * Get notification settings.
     */
    fun getNotificationSettingsFlow(): Flow<NotificationSettings>
    
    /**
     * Update notification settings.
     */
    suspend fun updateNotificationSettings(settings: NotificationSettings): Result<Unit>
    
    /**
     * Get privacy settings.
     */
    fun getPrivacySettingsFlow(): Flow<PrivacySettings>
    
    /**
     * Update privacy settings.
     */
    suspend fun updatePrivacySettings(settings: PrivacySettings): Result<Unit>
    
    /**
     * Get user settings.
     */
    suspend fun getUserSettings(): Result<UserSettings>
    
    /**
     * Update user settings.
     */
    suspend fun updateUserSettings(settings: UserSettings): Result<Unit>
    
    /**
     * Reset settings to default values.
     */
    suspend fun resetToDefaults(): Result<Unit>
    
    /**
     * Clear all user data.
     */
    suspend fun clearAllData(): Result<Unit>
}

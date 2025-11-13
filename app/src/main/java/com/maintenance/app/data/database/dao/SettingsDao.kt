package com.maintenance.app.data.database.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update
import com.maintenance.app.data.database.entities.SettingsEntity
import kotlinx.coroutines.flow.Flow

/**
 * Data Access Object for Settings.
 */
@Dao
interface SettingsDao {
    
    /**
     * Get all settings as a Flow.
     */
    @Query("SELECT * FROM user_app_settings WHERE id = 1")
    fun getSettingsFlow(): Flow<SettingsEntity?>
    
    /**
     * Get settings by ID.
     */
    @Query("SELECT * FROM user_app_settings WHERE id = :id")
    suspend fun getSettingsById(id: Long): SettingsEntity?
    
    /**
     * Get all settings (first record only).
     */
    @Query("SELECT * FROM user_app_settings LIMIT 1")
    suspend fun getSettings(): SettingsEntity?
    
    /**
     * Insert new settings.
     */
    @Insert
    suspend fun insertSettings(settings: SettingsEntity): Long
    
    /**
     * Update existing settings.
     */
    @Update
    suspend fun updateSettings(settings: SettingsEntity)
    
    /**
     * Delete settings.
     */
    @Delete
    suspend fun deleteSettings(settings: SettingsEntity)
    
    /**
     * Delete all settings.
     */
    @Query("DELETE FROM user_app_settings")
    suspend fun deleteAll()
}

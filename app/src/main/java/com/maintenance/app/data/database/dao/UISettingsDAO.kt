package com.maintenance.app.data.database.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.maintenance.app.data.database.entities.UISettingsEntity
import kotlinx.coroutines.flow.Flow

/**
 * Data Access Object for UI Settings operations.
 */
@Dao
interface UISettingsDAO {

    @Query("SELECT * FROM ui_settings WHERE id = 1")
    fun getUISettings(): Flow<UISettingsEntity?>

    @Query("SELECT * FROM ui_settings WHERE id = 1")
    suspend fun getUISettingsOnce(): UISettingsEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertUISettings(settings: UISettingsEntity)

    @Update
    suspend fun updateUISettings(settings: UISettingsEntity)

    @Query("UPDATE ui_settings SET theme_mode = :themeMode WHERE id = 1")
    suspend fun updateThemeMode(themeMode: String)

    @Query("UPDATE ui_settings SET font_size_scale = :fontSizeScale WHERE id = 1")
    suspend fun updateFontSizeScale(fontSizeScale: Float)

    @Query("UPDATE ui_settings SET language_code = :languageCode WHERE id = 1")
    suspend fun updateLanguageCode(languageCode: String)

    @Query("UPDATE ui_settings SET currency_code = :currencyCode WHERE id = 1")
    suspend fun updateCurrencyCode(currencyCode: String)

    @Query("UPDATE ui_settings SET date_format = :dateFormat WHERE id = 1")
    suspend fun updateDateFormat(dateFormat: String)

    @Query("UPDATE ui_settings SET time_format = :timeFormat WHERE id = 1")
    suspend fun updateTimeFormat(timeFormat: String)

    @Query("UPDATE ui_settings SET density_mode = :densityMode WHERE id = 1")
    suspend fun updateDensityMode(densityMode: String)

    @Query("UPDATE ui_settings SET show_grid_view = :showGridView WHERE id = 1")
    suspend fun updateShowGridView(showGridView: Boolean)

    @Query("UPDATE ui_settings SET items_per_page = :itemsPerPage WHERE id = 1")
    suspend fun updateItemsPerPage(itemsPerPage: Int)

    @Query("UPDATE ui_settings SET auto_backup_enabled = :enabled WHERE id = 1")
    suspend fun updateAutoBackupEnabled(enabled: Boolean)

    @Query("UPDATE ui_settings SET backup_frequency_days = :days WHERE id = 1")
    suspend fun updateBackupFrequencyDays(days: Int)

    @Query("UPDATE ui_settings SET backup_wifi_only = :wifiOnly WHERE id = 1")
    suspend fun updateBackupWifiOnly(wifiOnly: Boolean)

    @Query("UPDATE ui_settings SET compress_images = :compressImages WHERE id = 1")
    suspend fun updateCompressImages(compressImages: Boolean)

    @Query("UPDATE ui_settings SET image_quality = :quality WHERE id = 1")
    suspend fun updateImageQuality(quality: Int)

    @Query("UPDATE ui_settings SET max_image_size_mb = :sizeMb WHERE id = 1")
    suspend fun updateMaxImageSizeMb(sizeMb: Float)

    @Query("UPDATE ui_settings SET show_tooltips = :showTooltips WHERE id = 1")
    suspend fun updateShowTooltips(showTooltips: Boolean)

    @Query("UPDATE ui_settings SET enable_haptic_feedback = :enabled WHERE id = 1")
    suspend fun updateHapticFeedback(enabled: Boolean)

    @Query("UPDATE ui_settings SET enable_sound_effects = :enabled WHERE id = 1")
    suspend fun updateSoundEffects(enabled: Boolean)

    @Query("DELETE FROM ui_settings")
    suspend fun deleteAllUISettings()
}
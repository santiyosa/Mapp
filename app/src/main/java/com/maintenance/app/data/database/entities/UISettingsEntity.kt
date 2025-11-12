package com.maintenance.app.data.database.entities

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import java.time.LocalDateTime

/**
 * Room entity for storing user interface settings and preferences.
 */
@Entity(tableName = "ui_settings")
data class UISettingsEntity(
    @PrimaryKey
    @ColumnInfo(name = "id")
    val id: Long = 1, // Single row settings

    @ColumnInfo(name = "theme_mode")
    val themeMode: String = "SYSTEM", // LIGHT, DARK, SYSTEM

    @ColumnInfo(name = "font_size_scale")
    val fontSizeScale: Float = 1.0f, // 0.8f to 1.5f

    @ColumnInfo(name = "language_code")
    val languageCode: String = "en", // ISO 639-1 language codes

    @ColumnInfo(name = "currency_code")
    val currencyCode: String = "USD", // ISO 4217 currency codes

    @ColumnInfo(name = "date_format")
    val dateFormat: String = "MM/dd/yyyy", // Date display format

    @ColumnInfo(name = "time_format")
    val timeFormat: String = "12h", // 12h or 24h

    @ColumnInfo(name = "density_mode")
    val densityMode: String = "COMFORTABLE", // COMPACT, COMFORTABLE, SPACIOUS

    @ColumnInfo(name = "show_grid_view")
    val showGridView: Boolean = false, // List vs Grid view preference

    @ColumnInfo(name = "items_per_page")
    val itemsPerPage: Int = 20, // Pagination size

    @ColumnInfo(name = "auto_backup_enabled")
    val autoBackupEnabled: Boolean = false,

    @ColumnInfo(name = "backup_frequency_days")
    val backupFrequencyDays: Int = 7, // Weekly by default

    @ColumnInfo(name = "backup_wifi_only")
    val backupWifiOnly: Boolean = true,

    @ColumnInfo(name = "compress_images")
    val compressImages: Boolean = true,

    @ColumnInfo(name = "image_quality")
    val imageQuality: Int = 80, // 1-100 JPEG quality

    @ColumnInfo(name = "max_image_size_mb")
    val maxImageSizeMb: Float = 5.0f,

    @ColumnInfo(name = "show_tooltips")
    val showTooltips: Boolean = true,

    @ColumnInfo(name = "enable_haptic_feedback")
    val enableHapticFeedback: Boolean = true,

    @ColumnInfo(name = "enable_sound_effects")
    val enableSoundEffects: Boolean = false,

    @ColumnInfo(name = "created_date")
    val createdDate: LocalDateTime,

    @ColumnInfo(name = "updated_date")
    val updatedDate: LocalDateTime
)
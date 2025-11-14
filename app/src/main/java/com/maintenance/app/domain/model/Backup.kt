package com.maintenance.app.domain.model

import java.time.LocalDateTime

/**
 * Represents a backup configuration and metadata.
 */
data class BackupConfig(
    val id: String = "",
    val name: String = "",
    val description: String = "",
    val createdDate: LocalDateTime = LocalDateTime.now(),
    val lastModifiedDate: LocalDateTime = LocalDateTime.now(),
    val size: Long = 0L,
    val fileId: String = "",
    val isEncrypted: Boolean = true,
    val isAutomatic: Boolean = false,
    val version: Int = 1
)

/**
 * Represents backup schedule settings.
 */
enum class BackupFrequency {
    MANUAL,
    DAILY,
    WEEKLY,
    MONTHLY
}

/**
 * Represents backup schedule configuration.
 */
data class BackupSchedule(
    val id: String = "",
    val enabled: Boolean = false,
    val frequency: BackupFrequency = BackupFrequency.MANUAL,
    val wifiOnly: Boolean = true,
    val chargingOnly: Boolean = false,
    val lastBackupDate: LocalDateTime? = null,
    val nextBackupDate: LocalDateTime? = null,
    val maxBackupsToKeep: Int = 10
)

/**
 * Represents backup metadata stored locally.
 */
data class BackupMetadata(
    val id: String,
    val name: String,
    val createdDate: LocalDateTime,
    val size: Long,
    val driveFileId: String,
    val isEncrypted: Boolean,
    val recordCount: Int = 0,
    val maintenanceCount: Int = 0,
    val version: Int = 1
)

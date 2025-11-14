package com.maintenance.app.data.repositories

import android.content.Context
import com.maintenance.app.data.database.MaintenanceDatabase
import com.maintenance.app.domain.model.BackupConfig
import com.maintenance.app.domain.model.BackupMetadata
import com.maintenance.app.domain.model.BackupSchedule
import com.maintenance.app.domain.model.BackupFrequency
import com.maintenance.app.domain.repositories.BackupRepository
import com.maintenance.app.utils.GoogleDriveService
import com.maintenance.app.utils.Result
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
import java.time.LocalDateTime
import java.util.zip.ZipEntry
import java.util.zip.ZipOutputStream

/**
 * Implementation of BackupRepository.
 */
class BackupRepositoryImpl(
    private val context: Context,
    private val database: MaintenanceDatabase,
    private val googleDriveService: GoogleDriveService
) : BackupRepository {

    companion object {
        private const val BACKUP_DIR = "backups"
        private const val SCHEDULE_PREFS = "backup_schedule"
    }

    private val prefs = context.getSharedPreferences(SCHEDULE_PREFS, Context.MODE_PRIVATE)
    private val backupDir = File(context.getExternalFilesDir(null), BACKUP_DIR).apply {
        mkdirs()
    }

    override suspend fun createBackup(
        backupName: String,
        encryptionEnabled: Boolean
    ): Result<BackupConfig> {
        return try {
            withContext(Dispatchers.IO) {
                // Get database file
                val dbFile = context.getDatabasePath(database.openHelper.databaseName)

                // Create zip file with database
                val timestamp = System.currentTimeMillis()
                val zipData = createZipFromDatabaseFile(dbFile)

                // Upload to Google Drive
                val folderId = googleDriveService.getOrCreateBackupFolder()
                val fileId = googleDriveService.uploadBackup(
                    fileName = "${backupName}_${timestamp}.zip",
                    fileContent = zipData,
                    parentFolderId = folderId
                )

                if (fileId.isNullOrEmpty()) {
                    throw Exception("Failed to upload backup to Google Drive")
                }

                val backupConfig = BackupConfig(
                    id = fileId,
                    name = backupName,
                    createdDate = LocalDateTime.now(),
                    size = zipData.size.toLong(),
                    fileId = fileId,
                    isEncrypted = encryptionEnabled
                )

                Result.success(backupConfig)
            }
        } catch (e: Exception) {
            Result.Error(e, "Failed to create backup: ${e.message}")
        }
    }

    override suspend fun restoreBackup(backupId: String): Result<Unit> {
        return try {
            withContext(Dispatchers.IO) {
                // Download from Google Drive
                val backupData = googleDriveService.downloadBackup(backupId)
                    ?: throw Exception("Failed to download backup")

                // Extract and restore database
                restoreDatabaseFromZip(backupData)

                Result.success(Unit)
            }
        } catch (e: Exception) {
            Result.Error(e, "Failed to restore backup: ${e.message}")
        }
    }

    override suspend fun getBackupList(): Result<List<BackupMetadata>> {
        return try {
            withContext(Dispatchers.IO) {
                val backups = googleDriveService.listBackups().map { (fileId, fileName, createdDate) ->
                    BackupMetadata(
                        id = fileId,
                        name = fileName,
                        createdDate = LocalDateTime.now(),
                        size = 0L,
                        driveFileId = fileId,
                        isEncrypted = false
                    )
                }
                Result.success(backups)
            }
        } catch (e: Exception) {
            Result.Error(e, "Failed to get backup list")
        }
    }

    override suspend fun deleteBackup(backupId: String): Result<Unit> {
        return try {
            withContext(Dispatchers.IO) {
                val success = googleDriveService.deleteBackup(backupId)
                if (success) {
                    Result.success(Unit)
                } else {
                    Result.Error(Exception("Delete failed"), "Failed to delete backup")
                }
            }
        } catch (e: Exception) {
            Result.Error(e, "Failed to delete backup")
        }
    }

    override suspend fun getBackupSchedule(): Result<BackupSchedule> {
        return try {
            val enabled = prefs.getBoolean("schedule_enabled", false)
            val frequency = prefs.getString("schedule_frequency", BackupFrequency.MANUAL.name)
                ?.let { BackupFrequency.valueOf(it) } ?: BackupFrequency.MANUAL
            val wifiOnly = prefs.getBoolean("wifi_only", true)
            val chargingOnly = prefs.getBoolean("charging_only", false)
            val maxBackups = prefs.getInt("max_backups", 10)

            val schedule = BackupSchedule(
                enabled = enabled,
                frequency = frequency,
                wifiOnly = wifiOnly,
                chargingOnly = chargingOnly,
                maxBackupsToKeep = maxBackups
            )
            Result.success(schedule)
        } catch (e: Exception) {
            Result.Error(e, "Failed to get backup schedule")
        }
    }

    override suspend fun updateBackupSchedule(schedule: BackupSchedule): Result<Unit> {
        return try {
            withContext(Dispatchers.IO) {
                prefs.edit().apply {
                    putBoolean("schedule_enabled", schedule.enabled)
                    putString("schedule_frequency", schedule.frequency.name)
                    putBoolean("wifi_only", schedule.wifiOnly)
                    putBoolean("charging_only", schedule.chargingOnly)
                    putInt("max_backups", schedule.maxBackupsToKeep)
                    apply()
                }
                Result.success(Unit)
            }
        } catch (e: Exception) {
            Result.Error(e, "Failed to update backup schedule")
        }
    }

    override suspend fun isGoogleDriveAuthenticated(): Result<Boolean> {
        return try {
            val isAuth = googleDriveService.isAuthenticated()
            Result.success(isAuth)
        } catch (e: Exception) {
            Result.Error(e, "Failed to check authentication")
        }
    }

    override suspend fun getAvailableStorage(): Result<Long> {
        return try {
            withContext(Dispatchers.IO) {
                val storage = googleDriveService.getAvailableStorage()
                Result.success(storage)
            }
        } catch (e: Exception) {
            Result.Error(e, "Failed to get available storage")
        }
    }

    override suspend fun getLocalDatabaseSize(): Result<Long> {
        return try {
            withContext(Dispatchers.IO) {
                val dbFile = context.getDatabasePath(database.openHelper.databaseName)
                Result.success(dbFile.length())
            }
        } catch (e: Exception) {
            Result.Error(e, "Failed to get database size")
        }
    }

    /**
     * Create a zip file from the database file.
     */
    private fun createZipFromDatabaseFile(dbFile: File): ByteArray {
        val outputStream = java.io.ByteArrayOutputStream()
        ZipOutputStream(outputStream).use { zos ->
            val entry = ZipEntry(dbFile.name)
            zos.putNextEntry(entry)
            dbFile.inputStream().use { input ->
                input.copyTo(zos)
            }
            zos.closeEntry()
        }
        return outputStream.toByteArray()
    }

    /**
     * Restore database from zip file.
     */
    private fun restoreDatabaseFromZip(zipData: ByteArray) {
        val inputStream = java.io.ByteArrayInputStream(zipData)
        java.util.zip.ZipInputStream(inputStream).use { zis ->
            var entry = zis.nextEntry
            while (entry != null) {
                if (entry.name.endsWith(".db")) {
                    val dbFile = context.getDatabasePath(entry.name)
                    dbFile.parentFile?.mkdirs()
                    dbFile.outputStream().use { output ->
                        zis.copyTo(output)
                    }
                }
                entry = zis.nextEntry
            }
        }
    }
}

package com.maintenance.app.data.repositories

import android.content.Context
import android.os.StatFs
import com.maintenance.app.data.database.MaintenanceDatabase
import com.maintenance.app.domain.model.BackupConfig
import com.maintenance.app.domain.model.BackupMetadata
import com.maintenance.app.domain.model.BackupSchedule
import com.maintenance.app.domain.model.BackupFrequency
import com.maintenance.app.domain.repositories.BackupRepository
import com.maintenance.app.utils.GoogleDriveService
import com.maintenance.app.utils.LocalBackupService
import com.maintenance.app.utils.EncryptionUtil
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
    private val googleDriveService: GoogleDriveService,
    private val localBackupService: LocalBackupService
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
                var zipData = createZipFromDatabaseFile(dbFile)

                // Generate checksum before encryption
                val checksum = localBackupService.generateChecksum(zipData)

                // Encrypt if requested
                if (encryptionEnabled) {
                    zipData = EncryptionUtil.encrypt(zipData)
                }

                // Save backup file
                val backupFileName = "${backupName}_${timestamp}.backup"
                val filePath = localBackupService.saveBackupFile(backupFileName, zipData)

                val backupConfig = BackupConfig(
                    id = backupFileName.replace(".backup", ""),
                    name = backupName,
                    createdDate = LocalDateTime.now(),
                    size = zipData.size.toLong(),
                    filePath = filePath,
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
                // Try to find backup file with either .backup or .zip extension
                val backupFile = File(backupDir, "$backupId.backup").let { file ->
                    if (file.exists()) file else File(backupDir, "$backupId.zip")
                }
                
                if (!backupFile.exists()) {
                    throw Exception("Backup file not found")
                }

                var backupData = localBackupService.loadBackupFile(backupFile.absolutePath)
                    ?: throw Exception("Failed to load backup file")

                // Decrypt if file is encrypted
                if (backupFile.extension == "backup") {
                    backupData = EncryptionUtil.decrypt(backupData)
                }

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
                val backups = localBackupService.listBackupFiles().map { file ->
                    // Determine if encrypted by file extension
                    val isEncrypted = file.extension == "backup"
                    
                    BackupMetadata(
                        id = file.nameWithoutExtension,
                        name = file.nameWithoutExtension,
                        createdDate = LocalDateTime.now(),
                        size = file.length(),
                        filePath = file.absolutePath,
                        isEncrypted = isEncrypted,
                        checksum = ""
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
                val backupFile = File(backupDir, "$backupId.backup").let { file ->
                    if (file.exists()) file else File(backupDir, "$backupId.zip")
                }
                
                val deleted = localBackupService.deleteBackupFile(backupFile.absolutePath)
                if (deleted) {
                    Result.success(Unit)
                } else {
                    Result.Error(Exception("Delete failed"), "Failed to delete backup file")
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
            // Local backups don't require authentication
            Result.success(true)
        } catch (e: Exception) {
            Result.Error(e, "Failed to check availability")
        }
    }

    override suspend fun getAvailableStorage(): Result<Long> {
        return try {
            withContext(Dispatchers.IO) {
                val stat = StatFs(backupDir.absolutePath)
                val availableBytes = stat.availableBytes
                Result.success(availableBytes)
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

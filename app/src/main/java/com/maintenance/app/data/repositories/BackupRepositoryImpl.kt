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
import java.time.format.DateTimeFormatter
import java.util.zip.ZipEntry
import java.util.zip.ZipOutputStream
import javax.inject.Inject

/**
 * Implementation of BackupRepository.
 */
class BackupRepositoryImpl @Inject constructor(
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

                // Create zip file from database
                val dateFormatter = DateTimeFormatter.ofPattern("dd-MM-yyyy")
                val currentDate = LocalDateTime.now().format(dateFormatter)
                var zipData = createZipFromDatabaseFile(dbFile)

                android.util.Log.d("BackupRepository", "Created backup zip for $backupName dated $currentDate")

                // Generate checksum before encryption for integrity verification
                // Used for potential future validation mechanisms
                @Suppress("UNUSED_VARIABLE")
                val checksum = localBackupService.generateChecksum(zipData)

                // Encrypt if requested
                if (encryptionEnabled) {
                    zipData = EncryptionUtil.encrypt(zipData)
                }

                // Save backup file
                val backupFileName = "${backupName}_${currentDate}.backup"
                val filePath = localBackupService.saveBackupFile(backupFileName, zipData)

                android.util.Log.d("BackupRepository", "Backup saved to: $filePath (size: ${zipData.size} bytes)")

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
            android.util.Log.e("BackupRepository", "Failed to create backup: ${e.message}", e)
            Result.Error(e, "Failed to create backup: ${e.message}")
        }
    }

    override suspend fun restoreBackup(backupId: String): Result<Unit> {
        return try {
            withContext(Dispatchers.IO) {
                android.util.Log.d("BackupRepository", "Restore: Looking for backupId=$backupId")
                
                // Try to find backup file with either .backup or .zip extension
                val backupFile = File(backupDir, "$backupId.backup").let { file ->
                    if (file.exists()) {
                        android.util.Log.d("BackupRepository", "Restore: Found backup file: ${file.absolutePath}")
                        file
                    } else {
                        File(backupDir, "$backupId.zip")
                    }
                }
                
                if (!backupFile.exists()) {
                    android.util.Log.e("BackupRepository", "Restore: Backup file NOT found at: ${backupFile.absolutePath}")
                    throw Exception("Backup file not found at: ${backupFile.absolutePath}")
                }

                android.util.Log.d("BackupRepository", "Restore: Loading ${backupFile.length()} bytes from backup")
                
                var backupData = localBackupService.loadBackupFile(backupFile.absolutePath)
                    ?: throw Exception("Failed to load backup file")

                android.util.Log.d("BackupRepository", "Restore: Loaded ${backupData.size} bytes, decrypting...")
                
                // Decrypt if file is encrypted
                if (backupFile.extension == "backup") {
                    backupData = EncryptionUtil.decrypt(backupData)
                    android.util.Log.d("BackupRepository", "Restore: Decrypted to ${backupData.size} bytes")
                }

                // Merge backup data with current database (don't replace)
                mergeDatabaseFromZip(backupData)
                
                android.util.Log.d("BackupRepository", "Backup restored successfully from: ${backupFile.absolutePath}")

                Result.success(Unit)
            }
        } catch (e: Exception) {
            android.util.Log.e("BackupRepository", "Failed to restore backup: ${e.message}", e)
            Result.Error(e, "Failed to restore backup: ${e.message}")
        }
    }

    override suspend fun getBackupList(): Result<List<BackupMetadata>> {
        return try {
            withContext(Dispatchers.IO) {
                val backups = localBackupService.listBackupFiles().map { file ->
                    // Determine if encrypted by file extension
                    val isEncrypted = file.extension == "backup"
                    
                    // Extract date from filename (format: name_dd-MM-yyyy.backup)
                    val createdDate = try {
                        val parts = file.nameWithoutExtension.split("_")
                        if (parts.size >= 2) {
                            val dateStr = parts.last()
                            LocalDateTime.parse("${dateStr}T00:00:00", 
                                java.time.format.DateTimeFormatter.ISO_LOCAL_DATE_TIME)
                        } else {
                            LocalDateTime.ofInstant(
                                java.time.Instant.ofEpochMilli(file.lastModified()),
                                java.time.ZoneId.systemDefault()
                            )
                        }
                    } catch (e: Exception) {
                        LocalDateTime.ofInstant(
                            java.time.Instant.ofEpochMilli(file.lastModified()),
                            java.time.ZoneId.systemDefault()
                        )
                    }
                    
                    BackupMetadata(
                        id = file.nameWithoutExtension,
                        name = file.nameWithoutExtension,
                        createdDate = createdDate,
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

    /**
     * Merge database from zip file with current database (without replacing).
     * Extracts backup data to a temporary database, then merges with the current one.
     */
    private fun mergeDatabaseFromZip(zipData: ByteArray) {
        val inputStream = java.io.ByteArrayInputStream(zipData)
        java.util.zip.ZipInputStream(inputStream).use { zis ->
            var entry = zis.nextEntry
            while (entry != null) {
                if (entry.name.endsWith(".db")) {
                    // Create a temporary file for the backup database
                    val tempDbFile = File(context.cacheDir, "temp_backup_${System.currentTimeMillis()}.db")
                    
                    // Extract backup to temporary location
                    tempDbFile.outputStream().use { output ->
                        zis.copyTo(output)
                    }
                    
                    // Merge data from temporary database to current database
                    mergeDataFromTempDatabase(tempDbFile)
                    
                    // Clean up temporary file
                    tempDbFile.delete()
                }
                entry = zis.nextEntry
            }
        }
    }

    /**
     * Merge data from a temporary backup database into the current database.
     * Restores active records with new auto-generated IDs to avoid conflicts.
     */
    private fun mergeDataFromTempDatabase(tempDbFile: File) {
        try {
            val currentDb = database.openHelper.writableDatabase
            
            // Disable foreign keys temporarily to allow merging
            currentDb.execSQL("PRAGMA foreign_keys=OFF;")
            
            try {
                // Attach the temporary database
                currentDb.execSQL("ATTACH DATABASE '${tempDbFile.absolutePath}' AS backup;")
                
                android.util.Log.d("BackupRepository", "Attached backup database")
                
                // Debug: Check what records exist in the backup
                val backupQuery = currentDb.query("""
                    SELECT COUNT(*) as total, SUM(CASE WHEN is_active = 1 THEN 1 ELSE 0 END) as active_count
                    FROM backup.records
                """.trimIndent(), arrayOf())
                
                var totalBackupRecords = 0
                var activeBackupRecords = 0
                if (backupQuery.moveToFirst()) {
                    totalBackupRecords = backupQuery.getInt(backupQuery.getColumnIndex("total"))
                    activeBackupRecords = backupQuery.getInt(backupQuery.getColumnIndex("active_count"))
                }
                backupQuery.close()
                
                android.util.Log.d("BackupRepository", "Backup DB has $totalBackupRecords total records, $activeBackupRecords active")
                
                // Step 1: Copy ONLY ACTIVE RecordEntity records from backup with NEW IDs
                // Don't specify ID - let SQLite auto-generate them
                currentDb.execSQL("""
                    INSERT INTO records (
                        name, description, image_path, created_date, 
                        last_maintenance_date, updated_date, is_active, 
                        category, location
                    )
                    SELECT 
                        name, description, image_path, created_date,
                        last_maintenance_date, updated_date, is_active,
                        category, location
                    FROM backup.records
                    WHERE is_active = 1
                    ORDER BY id ASC;
                """.trimIndent())
                
                android.util.Log.d("BackupRepository", "Inserted active backup records with new auto-generated IDs")
                
                // Debug: Check how many records were just inserted
                val newRecordsQuery = currentDb.query("""
                    SELECT COUNT(*) as count
                    FROM records
                    WHERE id > (
                        SELECT COALESCE(MAX(id), 0) - $activeBackupRecords
                        FROM records
                        WHERE id <= (SELECT MAX(id) FROM records)
                    )
                """.trimIndent(), arrayOf())
                
                var insertedCount = 0
                if (newRecordsQuery.moveToFirst()) {
                    insertedCount = newRecordsQuery.getInt(newRecordsQuery.getColumnIndex("count"))
                }
                newRecordsQuery.close()
                
                android.util.Log.d("BackupRepository", "Actually inserted $insertedCount records into current DB")
                
                // Step 2: Copy MaintenanceEntity records using ROW_NUMBER to map IDs
                // The backup records were inserted in order, so we can use ROW_NUMBER to match them
                currentDb.execSQL("""
                    WITH backup_records AS (
                        SELECT 
                            id,
                            ROW_NUMBER() OVER (ORDER BY id) as row_pos
                        FROM backup.records
                        WHERE is_active = 1
                    ),
                    current_new_records AS (
                        SELECT 
                            id,
                            ROW_NUMBER() OVER (ORDER BY id DESC) as row_pos
                        FROM records
                        WHERE id > (
                            SELECT COALESCE(MAX(id), 0) - COUNT(*) 
                            FROM backup.records 
                            WHERE is_active = 1
                        )
                    )
                    INSERT OR IGNORE INTO maintenances (
                        record_id, maintenance_date, description, type, 
                        cost, currency, performed_by, location, created_date, updated_date
                    )
                    SELECT 
                        cnr.id as new_record_id,
                        bm.maintenance_date, bm.description, bm.type,
                        bm.cost, bm.currency, bm.performed_by, bm.location,
                        bm.created_date, bm.updated_date
                    FROM backup.maintenances bm
                    JOIN backup_records br ON bm.record_id = br.id
                    JOIN current_new_records cnr ON br.row_pos = cnr.row_pos;
                """.trimIndent())
                
                android.util.Log.d("BackupRepository", "Inserted maintenance records with new record_ids")
                
                // Step 3: Copy other data tables
                currentDb.execSQL("""
                    INSERT OR IGNORE INTO maintenance_drafts 
                    SELECT * FROM backup.maintenance_drafts;
                """.trimIndent())
                
                currentDb.execSQL("""
                    INSERT OR IGNORE INTO search_history 
                    SELECT * FROM backup.search_history;
                """.trimIndent())
                
                currentDb.execSQL("""
                    INSERT OR REPLACE INTO ui_settings 
                    SELECT * FROM backup.ui_settings;
                """.trimIndent())
                
                currentDb.execSQL("""
                    INSERT OR REPLACE INTO app_settings 
                    SELECT * FROM backup.app_settings;
                """.trimIndent())
                
                currentDb.execSQL("""
                    INSERT OR REPLACE INTO user_app_settings 
                    SELECT * FROM backup.user_app_settings;
                """.trimIndent())
                
                // Detach the temporary database
                currentDb.execSQL("DETACH DATABASE backup;")
                
                android.util.Log.d("BackupRepository", 
                    "Merge completed - active records restored with new IDs")
                
            } finally {
                // Re-enable foreign keys
                currentDb.execSQL("PRAGMA foreign_keys=ON;")
            }
        } catch (e: Exception) {
            android.util.Log.e("BackupRepository", "Merge failed: ${e.message}", e)
            e.printStackTrace()
            throw Exception("Failed to merge backup data: ${e.message}")
        }
    }
}

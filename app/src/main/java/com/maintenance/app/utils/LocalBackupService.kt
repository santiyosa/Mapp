package com.maintenance.app.utils

import android.content.Context
import java.io.File
import java.security.MessageDigest

/**
 * Service for managing local file backups.
 * Replaces Google Drive service with local storage operations.
 */
class LocalBackupService(private val context: Context) {

    companion object {
        private const val BACKUP_DIR = "backups"
        private const val ALGORITHM = "SHA-256"
    }

    /**
     * Get the backup directory, creating it if it doesn't exist.
     */
    fun getBackupDirectory(): File {
        val externalFilesDir = context.getExternalFilesDir(null)
            ?: throw Exception("External files directory not available")
        return File(externalFilesDir, BACKUP_DIR).apply {
            if (!exists()) {
                if (!mkdirs()) {
                    throw Exception("Failed to create backup directory: $absolutePath")
                }
            }
        }
    }

    /**
     * Save a backup file to local storage.
     * Returns the absolute path of the saved file.
     */
    fun saveBackupFile(fileName: String, data: ByteArray): String {
        val backupDir = getBackupDirectory()
        // Ensure directory exists
        if (!backupDir.exists()) {
            backupDir.mkdirs()
        }
        val backupFile = File(backupDir, fileName)
        backupFile.writeBytes(data)
        return backupFile.absolutePath
    }

    /**
     * Load a backup file from local storage.
     * Returns null if file doesn't exist.
     */
    fun loadBackupFile(filePath: String): ByteArray? {
        val file = File(filePath)
        return if (file.exists()) {
            file.readBytes()
        } else {
            null
        }
    }

    /**
     * List all backup files in the backup directory.
     * Returns list of File objects sorted by creation date (newest first).
     */
    fun listBackupFiles(): List<File> {
        val backupDir = getBackupDirectory()
        return backupDir.listFiles { file ->
            file.isFile && (file.extension == "zip" || file.extension == "backup")
        }?.sortedByDescending { it.lastModified() } ?: emptyList()
    }

    /**
     * Delete a backup file by path.
     * Returns true if deletion was successful.
     */
    fun deleteBackupFile(filePath: String): Boolean {
        return try {
            File(filePath).delete()
        } catch (e: Exception) {
            false
        }
    }

    /**
     * Generate SHA-256 checksum for backup integrity verification.
     */
    fun generateChecksum(data: ByteArray): String {
        val digest = MessageDigest.getInstance(ALGORITHM)
        val hash = digest.digest(data)
        return hash.joinToString("") { "%02x".format(it) }
    }

    /**
     * Verify backup integrity using checksum.
     */
    fun verifyChecksum(data: ByteArray, expectedChecksum: String): Boolean {
        val actualChecksum = generateChecksum(data)
        return actualChecksum.equals(expectedChecksum, ignoreCase = true)
    }

    /**
     * Get total size of all backups in bytes.
     */
    fun getTotalBackupSize(): Long {
        return listBackupFiles().sumOf { it.length() }
    }

    /**
     * Clean up old backups keeping only the specified number of most recent ones.
     */
    fun cleanupOldBackups(maxBackupsToKeep: Int): Int {
        val backups = listBackupFiles()
        if (backups.size <= maxBackupsToKeep) {
            return 0
        }

        var deletedCount = 0
        for (i in maxBackupsToKeep until backups.size) {
            if (backups[i].delete()) {
                deletedCount++
            }
        }
        return deletedCount
    }
}

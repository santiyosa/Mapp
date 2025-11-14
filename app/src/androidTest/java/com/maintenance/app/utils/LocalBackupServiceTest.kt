package com.maintenance.app.utils

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import org.junit.After
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import java.io.File
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNotNull
import kotlin.test.assertTrue

/**
 * Unit tests for LocalBackupService.
 * These tests run on Android device/emulator.
 */
@RunWith(AndroidJUnit4::class)
class LocalBackupServiceTest {

    private lateinit var context: Context
    private lateinit var backupService: LocalBackupService
    private lateinit var backupDir: File

    @Before
    fun setUp() {
        context = ApplicationProvider.getApplicationContext()
        backupService = LocalBackupService(context)
        backupDir = backupService.getBackupDirectory()
        
        // Clean up any existing backups from previous test runs
        backupDir.listFiles()?.forEach { it.delete() }
    }

    @After
    fun tearDown() {
        // Clean up test files
        backupDir.listFiles()?.forEach { it.delete() }
    }

    @Test
    fun testGetBackupDirectory_CreatesIfNotExists() {
        val dir = backupService.getBackupDirectory()
        assertTrue(dir.exists())
        assertTrue(dir.isDirectory)
    }

    @Test
    fun testSaveBackupFile_CreatesFile() {
        val fileName = "test_backup.backup"
        val data = "Test backup data".toByteArray()

        val filePath = backupService.saveBackupFile(fileName, data)

        assertTrue(File(filePath).exists())
        assertEquals(fileName, File(filePath).name)
    }

    @Test
    fun testLoadBackupFile_RetrievesCorrectData() {
        val fileName = "test_backup.backup"
        val originalData = "Test backup data".toByteArray()

        val filePath = backupService.saveBackupFile(fileName, originalData)
        val loadedData = backupService.loadBackupFile(filePath)

        assertNotNull(loadedData)
        assertEquals(originalData.contentToString(), loadedData?.contentToString())
    }

    @Test
    fun testLoadBackupFile_ReturnsNullIfNotExists() {
        val nonExistentPath = "/non/existent/path/backup.backup"

        val result = backupService.loadBackupFile(nonExistentPath)

        assertEquals(null, result)
    }

    @Test
    fun testListBackupFiles_ReturnsSortedByDate() {
        // Create multiple backup files with delays
        val files = mutableListOf<String>()
        repeat(3) { i ->
            val fileName = "backup_$i.backup"
            backupService.saveBackupFile(fileName, "Data $i".toByteArray())
            files.add(fileName)
            Thread.sleep(10)  // Small delay to ensure different timestamps
        }

        val backups = backupService.listBackupFiles()

        assertEquals(3, backups.size)
        // Most recent first
        assertEquals("backup_2.backup", backups[0].name)
    }

    @Test
    fun testListBackupFiles_OnlyReturnsZipFiles() {
        backupService.saveBackupFile("test1.backup", "data1".toByteArray())
        backupService.saveBackupFile("test2.txt", "data2".toByteArray())
        backupService.saveBackupFile("test3.backup", "data3".toByteArray())

        val backups = backupService.listBackupFiles()

        // Should only return .backup and .zip files
        assertEquals(2, backups.size)
    }

    @Test
    fun testDeleteBackupFile_DeletesFile() {
        val fileName = "test_backup.backup"
        val filePath = backupService.saveBackupFile(fileName, "Data".toByteArray())

        assertTrue(File(filePath).exists())

        val deleted = backupService.deleteBackupFile(filePath)

        assertTrue(deleted)
        assertFalse(File(filePath).exists())
    }

    @Test
    fun testDeleteBackupFile_ReturnsFalseIfNotExists() {
        val nonExistentPath = "/non/existent/backup.backup"

        val deleted = backupService.deleteBackupFile(nonExistentPath)

        assertFalse(deleted)
    }

    @Test
    fun testGenerateChecksum_ConsistentForSameData() {
        val data = "Test data for checksum".toByteArray()

        val checksum1 = backupService.generateChecksum(data)
        val checksum2 = backupService.generateChecksum(data)

        assertEquals(checksum1, checksum2)
    }

    @Test
    fun testGenerateChecksum_DifferentForDifferentData() {
        val data1 = "Test data 1".toByteArray()
        val data2 = "Test data 2".toByteArray()

        val checksum1 = backupService.generateChecksum(data1)
        val checksum2 = backupService.generateChecksum(data2)

        assertFalse(checksum1.equals(checksum2, ignoreCase = false))
    }

    @Test
    fun testVerifyChecksum_ValidChecksum() {
        val data = "Test data".toByteArray()
        val checksum = backupService.generateChecksum(data)

        val isValid = backupService.verifyChecksum(data, checksum)

        assertTrue(isValid)
    }

    @Test
    fun testVerifyChecksum_InvalidChecksum() {
        val data = "Test data".toByteArray()
        val wrongChecksum = "invalid_checksum_123abc"

        val isValid = backupService.verifyChecksum(data, wrongChecksum)

        assertFalse(isValid)
    }

    @Test
    fun testGetTotalBackupSize_CalculatesCorrectly() {
        backupService.saveBackupFile("backup1.backup", ByteArray(1000))
        backupService.saveBackupFile("backup2.backup", ByteArray(2000))

        val totalSize = backupService.getTotalBackupSize()

        assertEquals(3000L, totalSize)
    }

    @Test
    fun testCleanupOldBackups_KeepsMaxNumber() {
        repeat(5) { i ->
            backupService.saveBackupFile("backup_$i.backup", "Data $i".toByteArray())
            Thread.sleep(10)
        }

        val deletedCount = backupService.cleanupOldBackups(maxBackupsToKeep = 3)

        assertEquals(2, deletedCount)
        assertEquals(3, backupService.listBackupFiles().size)
    }

    @Test
    fun testCleanupOldBackups_NoDeleteIfUnderLimit() {
        repeat(2) { i ->
            backupService.saveBackupFile("backup_$i.backup", "Data $i".toByteArray())
        }

        val deletedCount = backupService.cleanupOldBackups(maxBackupsToKeep = 5)

        assertEquals(0, deletedCount)
        assertEquals(2, backupService.listBackupFiles().size)
    }
}

package com.maintenance.app.presentation.viewmodels.backup

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Environment
import androidx.core.content.FileProvider
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.maintenance.app.R
import com.maintenance.app.domain.model.BackupConfig
import com.maintenance.app.domain.model.BackupFrequency
import com.maintenance.app.domain.model.BackupMetadata
import com.maintenance.app.domain.model.BackupSchedule
import com.maintenance.app.domain.usecases.backup.CreateBackupUseCase
import com.maintenance.app.domain.usecases.backup.DeleteBackupUseCase
import com.maintenance.app.domain.usecases.backup.GetAvailableStorageUseCase
import com.maintenance.app.domain.usecases.backup.GetBackupListUseCase
import com.maintenance.app.domain.usecases.backup.GetBackupScheduleUseCase
import com.maintenance.app.domain.usecases.backup.GetLocalDatabaseSizeUseCase
import com.maintenance.app.domain.usecases.backup.RestoreBackupUseCase
import com.maintenance.app.domain.usecases.backup.UpdateBackupScheduleUseCase
import com.maintenance.app.utils.Result
import com.maintenance.app.utils.ResourceProvider
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.io.File
import javax.inject.Inject

/**
 * UI State for backup operations
 */
data class BackupUiState(
    val backups: List<BackupMetadata> = emptyList(),
    val backupSchedule: BackupSchedule? = null,
    val isLoading: Boolean = false,
    val isCreatingBackup: Boolean = false,
    val errorMessage: String? = null,
    val successMessage: String? = null,
    val availableStorage: Long = 0L,
    val databaseSize: Long = 0L,
    val isGoogleDriveConnected: Boolean = false
)

@HiltViewModel
class BackupViewModel @Inject constructor(
    private val context: Context,
    private val resourceProvider: ResourceProvider,
    private val createBackupUseCase: CreateBackupUseCase,
    private val restoreBackupUseCase: RestoreBackupUseCase,
    private val deleteBackupUseCase: DeleteBackupUseCase,
    private val getBackupListUseCase: GetBackupListUseCase,
    private val getBackupScheduleUseCase: GetBackupScheduleUseCase,
    private val updateBackupScheduleUseCase: UpdateBackupScheduleUseCase,
    private val getAvailableStorageUseCase: GetAvailableStorageUseCase,
    private val getLocalDatabaseSizeUseCase: GetLocalDatabaseSizeUseCase
) : ViewModel() {

    private val _uiState = MutableStateFlow(BackupUiState())
    val uiState: StateFlow<BackupUiState> = _uiState.asStateFlow()

    init {
        loadInitialData()
    }

    /**
     * Load initial backup data
     */
    private fun loadInitialData() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true)
            try {
                loadBackupList()
                loadBackupSchedule()
                loadStorageInfo()
                _uiState.value = _uiState.value.copy(isGoogleDriveConnected = true)
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    errorMessage = e.message ?: resourceProvider.getString(R.string.error_loading_backups),
                    isLoading = false
                )
            }
        }
    }

    /**
     * Load list of available backups
     */
    private suspend fun loadBackupList() {
        try {
            val result = getBackupListUseCase(GetBackupListUseCase.Params())
            when (result) {
                is Result.Success -> {
                    _uiState.value = _uiState.value.copy(
                        backups = result.data,
                        isLoading = false
                    )
                }
                is Result.Error -> {
                    _uiState.value = _uiState.value.copy(
                        errorMessage = result.message ?: resourceProvider.getString(R.string.failed_load_backups),
                        isLoading = false
                    )
                }
                is Result.Loading -> {}
            }
        } catch (e: Exception) {
            _uiState.value = _uiState.value.copy(
                errorMessage = e.message ?: "Failed to load backups",
                isLoading = false
            )
        }
    }

    /**
     * Load backup schedule configuration
     */
    private suspend fun loadBackupSchedule() {
        try {
            val result = getBackupScheduleUseCase(GetBackupScheduleUseCase.Params())
            when (result) {
                is Result.Success -> {
                    _uiState.value = _uiState.value.copy(backupSchedule = result.data)
                }
                is Result.Error -> {
                    // Schedule might not exist yet, just log
                    result.exception?.printStackTrace()
                }
                is Result.Loading -> {}
            }
        } catch (e: Exception) {
            // Schedule might not exist yet, just log
            e.printStackTrace()
        }
    }

    /**
     * Load storage information
     */
    private suspend fun loadStorageInfo() {
        try {
            val availableStorageResult = getAvailableStorageUseCase(GetAvailableStorageUseCase.Params())
            val databaseSizeResult = getLocalDatabaseSizeUseCase(GetLocalDatabaseSizeUseCase.Params())
            
            val availableStorage = (availableStorageResult as? Result.Success)?.data ?: 0L
            val databaseSize = (databaseSizeResult as? Result.Success)?.data ?: 0L
            
            _uiState.value = _uiState.value.copy(
                availableStorage = availableStorage,
                databaseSize = databaseSize
            )
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    /**
     * Create a new backup
     */
    fun createBackup(backupName: String, enableEncryption: Boolean) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isCreatingBackup = true, errorMessage = null)
            try {
                val result = createBackupUseCase(
                    CreateBackupUseCase.Params(
                        backupName = backupName,
                        encryptionEnabled = enableEncryption
                    )
                )
                
                when (result) {
                    is Result.Success -> {
                        _uiState.value = _uiState.value.copy(
                            successMessage = "Backup '$backupName' created successfully",
                            isCreatingBackup = false
                        )
                        // Reload backups list
                        loadBackupList()
                        // Clear success message after 3 seconds
                        kotlinx.coroutines.delay(3000)
                        _uiState.value = _uiState.value.copy(successMessage = null)
                    }
                    is Result.Error -> {
                        _uiState.value = _uiState.value.copy(
                            errorMessage = result.message ?: resourceProvider.getString(R.string.failed_create_backup),
                            isCreatingBackup = false
                        )
                    }
                is Result.Loading -> {}
            }
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    errorMessage = e.message ?: resourceProvider.getString(R.string.failed_create_backup),
                    isCreatingBackup = false
                )
            }
        }
    }

    /**
     * Restore a backup
     */
    fun restoreBackup(backupId: String) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, errorMessage = null)
            try {
                val result = restoreBackupUseCase(RestoreBackupUseCase.Params(backupId = backupId))
                
                when (result) {
                    is Result.Success -> {
                        _uiState.value = _uiState.value.copy(
                            successMessage = "Backup restored successfully",
                            isLoading = false
                        )
                        // Clear success message after 3 seconds
                        kotlinx.coroutines.delay(3000)
                        _uiState.value = _uiState.value.copy(successMessage = null)
                    }
                    is Result.Error -> {
                        _uiState.value = _uiState.value.copy(
                            errorMessage = result.message ?: "Failed to restore backup",
                            isLoading = false
                        )
                    }
                is Result.Loading -> {}
            }
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    errorMessage = e.message ?: "Failed to restore backup",
                    isLoading = false
                )
            }
        }
    }

    /**
     * Delete a backup
     */
    fun deleteBackup(backupId: String) {
        viewModelScope.launch {
            try {
                val result = deleteBackupUseCase(DeleteBackupUseCase.Params(backupId = backupId))
                
                when (result) {
                    is Result.Success -> {
                        _uiState.value = _uiState.value.copy(
                            successMessage = "Backup deleted successfully"
                        )
                        // Reload backups list
                        loadBackupList()
                        // Clear success message after 3 seconds
                        kotlinx.coroutines.delay(3000)
                        _uiState.value = _uiState.value.copy(successMessage = null)
                    }
                    is Result.Error -> {
                        _uiState.value = _uiState.value.copy(
                            errorMessage = result.message ?: "Failed to delete backup"
                        )
                    }
                is Result.Loading -> {}
            }
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    errorMessage = e.message ?: "Failed to delete backup"
                )
            }
        }
    }

    /**
     * Update backup schedule
     */
    fun updateBackupSchedule(
        enabled: Boolean,
        frequency: BackupFrequency,
        wifiOnly: Boolean,
        chargingOnly: Boolean
    ) {
        viewModelScope.launch {
            try {
                val schedule = BackupSchedule(
                    enabled = enabled,
                    frequency = frequency,
                    wifiOnly = wifiOnly,
                    chargingOnly = chargingOnly
                )
                
                val result = updateBackupScheduleUseCase(
                    UpdateBackupScheduleUseCase.Params(schedule = schedule)
                )
                
                when (result) {
                    is Result.Success -> {
                        _uiState.value = _uiState.value.copy(
                            backupSchedule = schedule,
                            successMessage = resourceProvider.getString(R.string.backup_schedule_updated)
                        )
                        // Clear success message after 3 seconds
                        kotlinx.coroutines.delay(3000)
                        _uiState.value = _uiState.value.copy(successMessage = null)
                    }
                    is Result.Error -> {
                        _uiState.value = _uiState.value.copy(
                            errorMessage = result.message ?: resourceProvider.getString(R.string.failed_update_schedule)
                        )
                    }
                is Result.Loading -> {}
            }
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    errorMessage = e.message ?: resourceProvider.getString(R.string.failed_update_schedule)
                )
            }
        }
    }

    /**
     * Clear error message
     */
    fun clearErrorMessage() {
        _uiState.value = _uiState.value.copy(errorMessage = null)
    }

    /**
     * Refresh backup list
     */
    fun refreshBackupList() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true)
            loadBackupList()
        }
    }

    /**
     * Restore backup from a URI (file selected from device).
     * Reads the backup data and merges it with the current database.
     */
    fun restoreBackupFromUri(uri: Uri) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, errorMessage = null)
            try {
                // Read the file from URI
                android.util.Log.d("BackupViewModel", "Restore: Reading from URI: $uri")
                val inputStream = context.contentResolver.openInputStream(uri)
                    ?: throw Exception("Failed to open file")
                
                val backupData = inputStream.readBytes()
                inputStream.close()
                
                android.util.Log.d("BackupViewModel", "Restore: Read ${backupData.size} bytes from URI")
                
                // Copy to backups directory temporarily for restoration
                val backupDir = File(context.getExternalFilesDir(null), "backups")
                if (!backupDir.exists()) {
                    backupDir.mkdirs()
                }
                
                val tempRestoreFile = File(backupDir, "restore_temp_${System.currentTimeMillis()}.backup")
                tempRestoreFile.writeBytes(backupData)
                
                android.util.Log.d("BackupViewModel", "Restore: Copied ${backupData.size} bytes to ${tempRestoreFile.absolutePath}")
                
                // Now use the restore backup use case with the file in the backups directory
                val tempBackupId = tempRestoreFile.nameWithoutExtension
                android.util.Log.d("BackupViewModel", "Restore: Starting restore with backupId=$tempBackupId")
                
                val result = restoreBackupUseCase(
                    RestoreBackupUseCase.Params(backupId = tempBackupId)
                )
                
                android.util.Log.d("BackupViewModel", "Restore: Use case returned ${result::class.simpleName}")
                
                // Clean up temp file
                tempRestoreFile.delete()
                
                when (result) {
                    is Result.Success -> {
                        _uiState.value = _uiState.value.copy(
                            successMessage = "Backup restored successfully from file",
                            isLoading = false
                        )
                        // Reload backups list
                        loadBackupList()
                        // Clear success message after 3 seconds
                        kotlinx.coroutines.delay(3000)
                        _uiState.value = _uiState.value.copy(successMessage = null)
                    }
                    is Result.Error -> {
                        _uiState.value = _uiState.value.copy(
                            errorMessage = result.message ?: "Failed to restore backup from file",
                            isLoading = false
                        )
                    }
                    is Result.Loading -> {}
                }
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    errorMessage = e.message ?: "Failed to restore backup from file",
                    isLoading = false
                )
            }
        }
    }

    /**
     * Share a backup file.
     * Opens the system share dialog to share the backup file.
     */
    fun shareBackup(backupId: String) {
        viewModelScope.launch {
            try {
                val backup = _uiState.value.backups.find { it.id == backupId }
                if (backup != null) {
                    val backupFile = File(backup.filePath)
                    if (backupFile.exists()) {
                        // Create a URI using FileProvider
                        val uri = FileProvider.getUriForFile(
                            context,
                            "${context.packageName}.fileprovider",
                            backupFile
                        )
                        
                        // Create share intent
                        val shareIntent = Intent(Intent.ACTION_SEND).apply {
                            type = "application/octet-stream"
                            putExtra(Intent.EXTRA_STREAM, uri)
                            putExtra(Intent.EXTRA_SUBJECT, "Backup: ${backup.name}")
                            putExtra(Intent.EXTRA_TEXT, "Backup file from Maintenance App")
                            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                        }
                        
                        // Start chooser with FLAG_ACTIVITY_NEW_TASK
                        val chooser = Intent.createChooser(shareIntent, "Share Backup")
                        chooser.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                        context.startActivity(chooser)
                        
                        _uiState.value = _uiState.value.copy(
                            successMessage = "Sharing backup: ${backup.name}"
                        )
                        kotlinx.coroutines.delay(2000)
                        _uiState.value = _uiState.value.copy(successMessage = null)
                    } else {
                        _uiState.value = _uiState.value.copy(
                            errorMessage = "Backup file not found"
                        )
                    }
                } else {
                    _uiState.value = _uiState.value.copy(
                        errorMessage = "Backup not found"
                    )
                }
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    errorMessage = e.message ?: "Failed to share backup"
                )
            }
        }
    }

    /**
     * Download a backup file to the device's Downloads folder.
     */
    fun downloadBackup(backupId: String) {
        viewModelScope.launch {
            try {
                val backup = _uiState.value.backups.find { it.id == backupId }
                if (backup != null) {
                    val sourceFile = File(backup.filePath)
                    android.util.Log.d("BackupViewModel", "Download: sourceFile exists=${sourceFile.exists()}, path=${sourceFile.absolutePath}, size=${sourceFile.length()} bytes")
                    
                    if (sourceFile.exists()) {
                        // Get Downloads directory
                        val downloadsDir = Environment.getExternalStoragePublicDirectory(
                            Environment.DIRECTORY_DOWNLOADS
                        )
                        
                        if (!downloadsDir.exists()) {
                            downloadsDir.mkdirs()
                        }
                        
                        // Create destination file
                        val destFile = File(
                            downloadsDir,
                            "${backup.name}_${System.currentTimeMillis()}.backup"
                        )
                        
                        // Copy file
                        sourceFile.copyTo(destFile, overwrite = true)
                        
                        android.util.Log.d("BackupViewModel", "Download: COPIED to ${destFile.absolutePath}, size=${destFile.length()} bytes")
                        
                        _uiState.value = _uiState.value.copy(
                            successMessage = "Backup downloaded to: ${destFile.name}"
                        )
                        kotlinx.coroutines.delay(3000)
                        _uiState.value = _uiState.value.copy(successMessage = null)
                    } else {
                        _uiState.value = _uiState.value.copy(
                            errorMessage = "Backup file not found"
                        )
                    }
                } else {
                    _uiState.value = _uiState.value.copy(
                        errorMessage = "Backup not found"
                    )
                }
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    errorMessage = e.message ?: "Failed to download backup"
                )
            }
        }
    }
}


package com.maintenance.app.presentation.viewmodels.backup

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
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
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
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
                    errorMessage = e.message ?: "Error loading backup data",
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
                        errorMessage = result.message ?: "Failed to load backups",
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
                            errorMessage = result.message ?: "Failed to create backup",
                            isCreatingBackup = false
                        )
                    }
                is Result.Loading -> {}
            }
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    errorMessage = e.message ?: "Failed to create backup",
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
                            successMessage = "Backup schedule updated"
                        )
                        // Clear success message after 3 seconds
                        kotlinx.coroutines.delay(3000)
                        _uiState.value = _uiState.value.copy(successMessage = null)
                    }
                    is Result.Error -> {
                        _uiState.value = _uiState.value.copy(
                            errorMessage = result.message ?: "Failed to update backup schedule"
                        )
                    }
                is Result.Loading -> {}
            }
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    errorMessage = e.message ?: "Failed to update backup schedule"
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
}


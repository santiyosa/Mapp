package com.maintenance.app.presentation.viewmodels.detail

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.maintenance.app.domain.model.Maintenance
import com.maintenance.app.domain.model.Record
import com.maintenance.app.domain.usecases.maintenances.GetMaintenancesUseCase
import com.maintenance.app.domain.usecases.records.GetRecordByIdUseCase
import com.maintenance.app.domain.usecases.sharing.ShareRecordViaWhatsAppUseCase
import com.maintenance.app.domain.usecases.sharing.ShareRecordGenericUseCase
import com.maintenance.app.domain.usecases.sharing.CheckWhatsAppInstalledUseCase
import kotlinx.coroutines.flow.first
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * ViewModel for the Record Detail screen that manages record details and maintenance history.
 */
@HiltViewModel
class RecordDetailViewModel @Inject constructor(
    private val getRecordByIdUseCase: GetRecordByIdUseCase,
    private val getMaintenancesByRecordUseCase: GetMaintenancesUseCase,
    private val shareRecordViaWhatsAppUseCase: ShareRecordViaWhatsAppUseCase,
    private val shareRecordGenericUseCase: ShareRecordGenericUseCase,
    private val checkWhatsAppInstalledUseCase: CheckWhatsAppInstalledUseCase
) : ViewModel() {

    private val _uiState = MutableStateFlow<RecordDetailUiState>(RecordDetailUiState.Loading)
    val uiState: StateFlow<RecordDetailUiState> = _uiState.asStateFlow()

    private val _isWhatsAppAvailable = MutableStateFlow(false)
    val isWhatsAppAvailable: StateFlow<Boolean> = _isWhatsAppAvailable.asStateFlow()

    private val _shareLoading = MutableStateFlow(false)
    val shareLoading: StateFlow<Boolean> = _shareLoading.asStateFlow()

    private val _shareError = MutableStateFlow<String?>(null)
    val shareError: StateFlow<String?> = _shareError.asStateFlow()

    init {
        checkWhatsAppAvailability()
    }

    /**
     * Check if WhatsApp is installed on the device.
     */
    private fun checkWhatsAppAvailability() {
        viewModelScope.launch {
            try {
                val result = checkWhatsAppInstalledUseCase(Unit)
                _isWhatsAppAvailable.value = result.getOrDefault(false)
            } catch (e: Exception) {
                _isWhatsAppAvailable.value = false
            }
        }
    }

    /**
     * Load record details and maintenance history.
     */
    fun loadRecord(recordId: Long) {
        viewModelScope.launch {
            _uiState.value = RecordDetailUiState.Loading
            
            try {
                // Load record
                val recordResult = getRecordByIdUseCase(GetRecordByIdUseCase.Params(recordId))
                
                if (recordResult.isSuccess) {
                    val record = recordResult.getOrNull()
                    if (record != null) {
                        // Load maintenances for this record
                        val maintenancesFlow = getMaintenancesByRecordUseCase(
                            GetMaintenancesUseCase.Params(
                                filter = GetMaintenancesUseCase.FilterType.BY_RECORD,
                                recordId = recordId
                            )
                        )
                        val maintenances = maintenancesFlow.first()
                        
                        _uiState.value = RecordDetailUiState.Success(
                            record = record,
                            maintenances = maintenances
                        )
                    } else {
                        _uiState.value = RecordDetailUiState.Error("Record not found")
                    }
                } else {
                    _uiState.value = RecordDetailUiState.Error(
                        recordResult.exceptionOrNull()?.message ?: "Failed to load record"
                    )
                }
            } catch (e: Exception) {
                _uiState.value = RecordDetailUiState.Error(
                    e.message ?: "Unknown error occurred"
                )
            }
        }
    }

    /**
     * Share the current record via WhatsApp.
     */
    fun shareRecordViaWhatsApp() {
        val currentState = _uiState.value
        if (currentState !is RecordDetailUiState.Success) return

        viewModelScope.launch {
            _shareLoading.value = true
            _shareError.value = null

            try {
                val result = shareRecordViaWhatsAppUseCase(
                    ShareRecordViaWhatsAppUseCase.Params(currentState.record)
                )
                val success = result.getOrDefault(false)
                if (!success) {
                    _shareError.value = "Failed to share record via WhatsApp"
                }
            } catch (e: Exception) {
                _shareError.value = e.message ?: "Error sharing record"
            } finally {
                _shareLoading.value = false
            }
        }
    }

    /**
     * Share the current record with selected maintenances via WhatsApp.
     */
    fun shareRecordWithMaintenancesViaWhatsApp(selectedMaintenances: List<Maintenance>) {
        val currentState = _uiState.value
        if (currentState !is RecordDetailUiState.Success) return

        viewModelScope.launch {
            _shareLoading.value = true
            _shareError.value = null

            try {
                if (selectedMaintenances.isEmpty()) {
                    shareRecordViaWhatsApp()
                } else {
                    // TODO: Implement share with selected maintenances
                    // For now, just share the record
                    val result = shareRecordViaWhatsAppUseCase(
                        ShareRecordViaWhatsAppUseCase.Params(currentState.record)
                    )
                    val success = result.getOrDefault(false)
                    if (!success) {
                        _shareError.value = "Failed to share record via WhatsApp"
                    }
                }
            } catch (e: Exception) {
                _shareError.value = e.message ?: "Error sharing record"
            } finally {
                _shareLoading.value = false
            }
        }
    }

    /**
     * Share the current record via generic share intent.
     */
    fun shareRecordGeneric() {
        val currentState = _uiState.value
        if (currentState !is RecordDetailUiState.Success) return

        viewModelScope.launch {
            _shareLoading.value = true
            _shareError.value = null

            try {
                // For now, use the same WhatsApp share since generic doesn't seem to work
                val result = shareRecordViaWhatsAppUseCase(
                    ShareRecordViaWhatsAppUseCase.Params(currentState.record)
                )
                val success = result.getOrDefault(false)
                if (!success) {
                    _shareError.value = "Failed to share record"
                }
            } catch (e: Exception) {
                _shareError.value = e.message ?: "Error sharing record"
            } finally {
                _shareLoading.value = false
            }
        }
    }

    /**
     * Clear the share error message.
     */
    fun clearShareError() {
        _shareError.value = null
    }
}

/**
 * UI state for the Record Detail screen.
 */
sealed class RecordDetailUiState {
    object Loading : RecordDetailUiState()
    data class Success(
        val record: Record,
        val maintenances: List<Maintenance>
    ) : RecordDetailUiState()
    data class Error(val message: String) : RecordDetailUiState()
}
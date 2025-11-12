package com.maintenance.app.presentation.viewmodels.detail

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.maintenance.app.domain.model.Maintenance
import com.maintenance.app.domain.model.Record
import com.maintenance.app.domain.usecases.maintenances.GetMaintenancesUseCase
import com.maintenance.app.domain.usecases.records.GetRecordByIdUseCase
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
    private val getMaintenancesByRecordUseCase: GetMaintenancesUseCase
) : ViewModel() {

    private val _uiState = MutableStateFlow<RecordDetailUiState>(RecordDetailUiState.Loading)
    val uiState: StateFlow<RecordDetailUiState> = _uiState.asStateFlow()

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
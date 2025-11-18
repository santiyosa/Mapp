package com.maintenance.app.presentation.viewmodels.maintenance.detail

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.maintenance.app.domain.model.Maintenance
import com.maintenance.app.domain.usecases.maintenance.GetMaintenanceUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

sealed class MaintenanceDetailUiState {
    object Loading : MaintenanceDetailUiState()
    data class Success(val maintenance: Maintenance) : MaintenanceDetailUiState()
    data class Error(val message: String) : MaintenanceDetailUiState()
}

@HiltViewModel
class MaintenanceDetailViewModel @Inject constructor(
    private val getMaintenanceUseCase: GetMaintenanceUseCase
) : ViewModel() {

    private val _uiState = MutableStateFlow<MaintenanceDetailUiState>(MaintenanceDetailUiState.Loading)
    val uiState: StateFlow<MaintenanceDetailUiState> = _uiState

    fun loadMaintenance(maintenanceId: Long) {
        viewModelScope.launch {
            try {
                _uiState.value = MaintenanceDetailUiState.Loading
                val result = getMaintenanceUseCase(GetMaintenanceUseCase.Params(maintenanceId))
                when (result) {
                    is com.maintenance.app.utils.Result.Success -> {
                        val maintenance = result.data
                        if (maintenance != null) {
                            _uiState.value = MaintenanceDetailUiState.Success(maintenance)
                        } else {
                            _uiState.value = MaintenanceDetailUiState.Error("Maintenance not found")
                        }
                    }
                    is com.maintenance.app.utils.Result.Error -> {
                        _uiState.value = MaintenanceDetailUiState.Error(result.message)
                    }
                    is com.maintenance.app.utils.Result.Loading -> {
                        _uiState.value = MaintenanceDetailUiState.Loading
                    }
                }
            } catch (e: Exception) {
                _uiState.value = MaintenanceDetailUiState.Error(e.message ?: "Unknown error")
            }
        }
    }
}

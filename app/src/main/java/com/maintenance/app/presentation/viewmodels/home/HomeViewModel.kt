package com.maintenance.app.presentation.viewmodels.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.maintenance.app.domain.model.Record
import com.maintenance.app.domain.usecases.records.GetRecordsUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * ViewModel for the Home screen that manages the list of maintenance records.
 */
@HiltViewModel
class HomeViewModel @Inject constructor(
    private val getAllRecordsUseCase: GetRecordsUseCase
) : ViewModel() {

    private val _uiState = MutableStateFlow<HomeUiState>(HomeUiState.Loading)
    val uiState: StateFlow<HomeUiState> = _uiState.asStateFlow()

    init {
        loadRecords()
    }

    /**
     * Load all maintenance records.
     */
    fun loadRecords() {
        viewModelScope.launch {
            try {
                _uiState.value = HomeUiState.Loading
                
                val recordsFlow = getAllRecordsUseCase(
                    GetRecordsUseCase.Params(
                        filter = GetRecordsUseCase.FilterType.ALL_ACTIVE
                    )
                )
                
                recordsFlow.collect { records ->
                    if (records.isEmpty()) {
                        _uiState.value = HomeUiState.Empty
                    } else {
                        _uiState.value = HomeUiState.Success(records)
                    }
                }
            } catch (exception: Exception) {
                _uiState.value = HomeUiState.Error(
                    exception.message ?: "Unknown error occurred"
                )
            }
        }
    }
}

/**
 * UI state for the Home screen.
 */
sealed class HomeUiState {
    object Loading : HomeUiState()
    object Empty : HomeUiState()
    data class Success(val records: List<Record>) : HomeUiState()
    data class Error(val message: String) : HomeUiState()
}
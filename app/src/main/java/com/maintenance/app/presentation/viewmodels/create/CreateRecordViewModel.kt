package com.maintenance.app.presentation.viewmodels.create

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.maintenance.app.domain.model.Record
import com.maintenance.app.domain.usecases.records.CreateRecordUseCase
import com.maintenance.app.domain.usecases.validation.ValidateRecordDataUseCase
import com.maintenance.app.utils.ValidationResult
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.time.LocalDateTime
import javax.inject.Inject

/**
 * ViewModel for creating a new maintenance record.
 */
@HiltViewModel
class CreateRecordViewModel @Inject constructor(
    private val createRecordUseCase: CreateRecordUseCase,
    private val validateRecordUseCase: ValidateRecordDataUseCase
) : ViewModel() {

    private val _uiState = MutableStateFlow<CreateRecordUiState>(CreateRecordUiState.Idle)
    val uiState: StateFlow<CreateRecordUiState> = _uiState.asStateFlow()

    // Form fields
    var title by mutableStateOf("")
        private set
    var category by mutableStateOf("")
        private set
    var description by mutableStateOf("")
        private set

    // Validation errors
    var titleError by mutableStateOf<String?>(null)
        private set
    var categoryError by mutableStateOf<String?>(null)
        private set

    /**
     * Update title field.
     */
    fun updateTitle(newTitle: String) {
        title = newTitle
        titleError = null // Clear error when user types
    }

    /**
     * Update category field.
     */
    fun updateCategory(newCategory: String) {
        category = newCategory
        categoryError = null // Clear error when user types
    }

    /**
     * Update description field.
     */
    fun updateDescription(newDescription: String) {
        description = newDescription
    }

    /**
     * Save the record after validation.
     */
    fun saveRecord() {
        viewModelScope.launch {
            // Clear previous errors
            titleError = null
            categoryError = null
            
            // Create record object for validation
            val now = LocalDateTime.now()
            val record = Record(
                id = 0, // Will be assigned by database
                name = title.trim(),
                description = description.trim(),
                category = category.trim(),
                createdDate = now,
                updatedDate = now,
                lastMaintenanceDate = null
            )
            
            // Validate the record
            val validationResult = validateRecordUseCase(
                ValidateRecordDataUseCase.Params(
                    name = title.trim(),
                    description = description.trim(),
                    category = category.trim()
                )
            )
            
            if (validationResult.isError) {
                val errorMessage = validationResult.exceptionOrNull()?.message ?: "Validation failed"
                _uiState.value = CreateRecordUiState.Error(errorMessage)
                return@launch
            }
            
            val validation = validationResult.getOrNull()
            if (validation != null && !validation.isValid) {
                val errorMessage = validation.firstError ?: "Validation failed"
                
                // Set specific field errors based on validation message
                when {
                    errorMessage.contains("name", ignoreCase = true) -> {
                        titleError = errorMessage
                    }
                    errorMessage.contains("category", ignoreCase = true) -> {
                        categoryError = errorMessage
                    }
                    else -> {
                        _uiState.value = CreateRecordUiState.Error(errorMessage)
                    }
                }
                return@launch
            }
            
            // If validation passes, save the record
            _uiState.value = CreateRecordUiState.Loading
            
            val createResult = createRecordUseCase(
                CreateRecordUseCase.Params(
                    name = title.trim(),
                    description = description.trim(),
                    category = category.trim()
                )
            )
            
            if (createResult.isSuccess) {
                _uiState.value = CreateRecordUiState.Success
            } else {
                _uiState.value = CreateRecordUiState.Error(
                    createResult.exceptionOrNull()?.message ?: "Failed to save record"
                )
            }
        }
    }
}

/**
 * UI state for creating a record.
 */
sealed class CreateRecordUiState {
    object Idle : CreateRecordUiState()
    object Loading : CreateRecordUiState()
    object Success : CreateRecordUiState()
    data class Error(val message: String) : CreateRecordUiState()
}
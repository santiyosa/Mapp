package com.maintenance.app.presentation.viewmodels.edit

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.maintenance.app.domain.model.Record
import com.maintenance.app.domain.usecases.records.DeleteRecordUseCase
import com.maintenance.app.domain.usecases.records.GetRecordByIdUseCase
import com.maintenance.app.domain.usecases.records.UpdateRecordUseCase
import com.maintenance.app.domain.usecases.validation.ValidateRecordDataUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * ViewModel for editing an existing maintenance record.
 */
@HiltViewModel
class EditRecordViewModel @Inject constructor(
    private val getRecordByIdUseCase: GetRecordByIdUseCase,
    private val updateRecordUseCase: UpdateRecordUseCase,
    private val deleteRecordUseCase: DeleteRecordUseCase,
    private val validateRecordUseCase: ValidateRecordDataUseCase
) : ViewModel() {

    private val _uiState = MutableStateFlow<EditRecordUiState>(EditRecordUiState.Loading)
    val uiState: StateFlow<EditRecordUiState> = _uiState.asStateFlow()

    private var originalRecord: Record? = null

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
     * Load the record for editing.
     */
    fun loadRecord(recordId: Long) {
        viewModelScope.launch {
            _uiState.value = EditRecordUiState.Loading
            
            try {
                val recordResult = getRecordByIdUseCase(GetRecordByIdUseCase.Params(recordId))
                
                if (recordResult.isSuccess) {
                    val record = recordResult.getOrNull()
                    if (record != null) {
                        originalRecord = record
                        title = record.name
                        category = record.category ?: ""
                        description = record.description ?: ""
                        _uiState.value = EditRecordUiState.Loaded
                    } else {
                        _uiState.value = EditRecordUiState.Error("Record not found")
                    }
                } else {
                    _uiState.value = EditRecordUiState.Error(
                        recordResult.exceptionOrNull()?.message ?: "Failed to load record"
                    )
                }
            } catch (exception: Exception) {
                _uiState.value = EditRecordUiState.Error(
                    exception.message ?: "Failed to load record"
                )
            }
        }
    }

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
     * Save the updated record after validation.
     */
    fun saveRecord() {
        val record = originalRecord ?: return
        
        viewModelScope.launch {
            // Clear previous errors
            titleError = null
            categoryError = null
            
            // Create updated record
            val updatedRecord = record.copy(
                name = title.trim(),
                description = description.trim(),
                category = category.trim()
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
                _uiState.value = EditRecordUiState.Error(errorMessage)
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
                        _uiState.value = EditRecordUiState.Error(errorMessage)
                    }
                }
                return@launch
            }
            
            // If validation passes, update the record
            _uiState.value = EditRecordUiState.Saving
            
            val updateResult = updateRecordUseCase(UpdateRecordUseCase.Params(updatedRecord))
            
            if (updateResult.isSuccess) {
                _uiState.value = EditRecordUiState.Success
            } else {
                _uiState.value = EditRecordUiState.Error(
                    updateResult.exceptionOrNull()?.message ?: "Failed to update record"
                )
            }
        }
    }

    /**
     * Delete the record.
     */
    fun deleteRecord() {
        val record = originalRecord ?: return
        
        viewModelScope.launch {
            _uiState.value = EditRecordUiState.Saving
            
            val deleteResult = deleteRecordUseCase(DeleteRecordUseCase.Params(record.id))
            
            if (deleteResult.isSuccess) {
                _uiState.value = EditRecordUiState.Deleted
            } else {
                _uiState.value = EditRecordUiState.Error(
                    deleteResult.exceptionOrNull()?.message ?: "Failed to delete record"
                )
            }
        }
    }
}

/**
 * UI state for editing a record.
 */
sealed class EditRecordUiState {
    object Loading : EditRecordUiState()
    object Loaded : EditRecordUiState()
    object Saving : EditRecordUiState()
    object Success : EditRecordUiState()
    object Deleted : EditRecordUiState()
    data class Error(val message: String) : EditRecordUiState()
}
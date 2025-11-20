package com.maintenance.app.presentation.viewmodels.maintenance.create

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import android.net.Uri
import com.maintenance.app.R
import com.maintenance.app.domain.model.Maintenance
import com.maintenance.app.domain.usecases.maintenances.CreateMaintenanceUseCase
import com.maintenance.app.domain.usecases.validation.ValidateMaintenanceDataUseCase
import com.maintenance.app.domain.usecases.images.ImageCaptureUseCase
import com.maintenance.app.domain.usecases.images.DeleteImageUseCase
import com.maintenance.app.domain.usecases.images.CreateTempImageUseCase
import com.maintenance.app.domain.usecases.drafts.SaveMaintenanceDraftUseCase
import com.maintenance.app.domain.usecases.drafts.LoadMaintenanceDraftUseCase
import com.maintenance.app.domain.usecases.drafts.DeleteMaintenanceDraftUseCase
import com.maintenance.app.domain.model.MaintenanceDraft
import com.maintenance.app.utils.ResourceProvider
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.time.LocalTime
import java.math.BigDecimal
import java.time.LocalDateTime
import javax.inject.Inject

/**
 * ViewModel for creating a new maintenance record.
 */
@HiltViewModel
class CreateMaintenanceViewModel @Inject constructor(
    private val resourceProvider: ResourceProvider,
    private val createMaintenanceUseCase: CreateMaintenanceUseCase,
    private val validateMaintenanceUseCase: ValidateMaintenanceDataUseCase,
    private val imageCaptureUseCase: ImageCaptureUseCase,
    private val deleteImageUseCase: DeleteImageUseCase,
    private val createTempImageUseCase: CreateTempImageUseCase,
    private val saveDraftUseCase: SaveMaintenanceDraftUseCase,
    private val loadDraftUseCase: LoadMaintenanceDraftUseCase,
    private val deleteDraftUseCase: DeleteMaintenanceDraftUseCase
) : ViewModel() {

    private val _uiState = MutableStateFlow<CreateMaintenanceUiState>(CreateMaintenanceUiState.Idle)
    val uiState: StateFlow<CreateMaintenanceUiState> = _uiState.asStateFlow()
    
    // Auto-save related properties
    private var autoSaveJob: Job? = null
    private var currentRecordId: Long = 0
    private var isDraftLoaded = false
    
    companion object {
        private const val AUTO_SAVE_DELAY_MS = 2000L // 2 seconds delay after typing stops
    }

    // Form fields
    var description by mutableStateOf("")
        private set
    var type by mutableStateOf("")
        private set
    var cost by mutableStateOf("")
        private set
    var currency by mutableStateOf("USD")
        private set
    var performedBy by mutableStateOf("")
        private set
    var location by mutableStateOf("")
        private set
    var durationMinutes by mutableStateOf("")
        private set
    var partsReplaced by mutableStateOf("")
        private set
    var notes by mutableStateOf("")
        private set
    var priority by mutableStateOf(Maintenance.Priority.MEDIUM)
        private set
    var isRecurring by mutableStateOf(false)
        private set
    var recurrenceIntervalDays by mutableStateOf("")
        private set
    var maintenanceDate by mutableStateOf<LocalDate?>(null)
        private set
    var maintenanceTime by mutableStateOf<LocalTime?>(null)
        private set
    var nextMaintenanceDue by mutableStateOf<LocalDate?>(null)
        private set

    // Image fields
    var selectedImages by mutableStateOf<List<String>>(emptyList())
        private set
    var isCapturingImage by mutableStateOf(false)
        private set
    var tempImageUri by mutableStateOf<Uri?>(null)
        private set

    /**
     * Update description field.
     */
    fun updateDescription(newDescription: String) {
        description = newDescription
        scheduleAutoSave()
        // TODO: Add real-time validation
    }

    /**
     * Update type field.
     */
    fun updateType(newType: String) {
        type = newType
        scheduleAutoSave()
        // TODO: Add real-time validation
    }

    /**
     * Update cost field.
     */
    fun updateCost(newCost: String) {
        cost = newCost
        scheduleAutoSave()
        // TODO: Add real-time validation
    }

    /**
     * Update currency field.
     */
    fun updateCurrency(newCurrency: String) {
        currency = newCurrency
        scheduleAutoSave()
        // TODO: Add validation
    }

    /**
     * Update performed by field.
     */
    fun updatePerformedBy(newPerformedBy: String) {
        performedBy = newPerformedBy
        scheduleAutoSave()
        // TODO: Add validation
    }

    /**
     * Update location field.
     */
    fun updateLocation(newLocation: String) {
        location = newLocation
        scheduleAutoSave()
        // TODO: Add validation
    }

    /**
     * Update duration field.
     */
    fun updateDuration(newDuration: String) {
        durationMinutes = newDuration
        scheduleAutoSave()
        // TODO: Add validation
    }

    /**
     * Update parts replaced field.
     */
    fun updatePartsReplaced(newParts: String) {
        partsReplaced = newParts
        scheduleAutoSave()
        // TODO: Add validation
    }

    /**
     * Update notes field.
     */
    fun updateNotes(newNotes: String) {
        notes = newNotes
        scheduleAutoSave()
        // TODO: Add validation
    }

    /**
     * Update priority field.
     */
    fun updatePriority(newPriority: Maintenance.Priority) {
        priority = newPriority
    }

    /**
     * Update priority from string.
     */
    fun updateStatus(newStatus: String) {
        // Map string to status if needed
        // For now, this is a placeholder for consistency
    }

    /**
     * Update next maintenance due date.
     */
    fun updateNextMaintenanceDue(date: LocalDate) {
        nextMaintenanceDue = date
        scheduleAutoSave()
    }

    /**
     * Update maintenance date.
     */
    fun updateMaintenanceDate(date: LocalDate) {
        maintenanceDate = date
        scheduleAutoSave()
    }

    /**
     * Update maintenance time.
     */
    fun updateMaintenanceTime(time: LocalTime) {
        maintenanceTime = time
        scheduleAutoSave()
    }

    /**
     * Load draft maintenance for editing.
     */
    fun loadDraft(recordId: Long) {
        currentRecordId = recordId
        viewModelScope.launch {
            try {
                val result = loadDraftUseCase.invoke(LoadMaintenanceDraftUseCase.Params(recordId = recordId))
                if (result.isSuccess) {
                    val draft = result.getOrNull()
                    if (draft != null) {
                        isDraftLoaded = true
                        // Load draft fields
                    }
                }
            } catch (e: Exception) {
                // Handle error
            }
        }
    }

    /**
     * Update recurring setting.
     */
    fun updateRecurring(recurring: Boolean) {
        isRecurring = recurring
        if (!recurring) {
            recurrenceIntervalDays = ""
        }
    }

    /**
     * Update recurrence interval.
     */
    fun updateRecurrenceInterval(interval: String) {
        recurrenceIntervalDays = interval
    }

    /**
     * Prepares URI for camera capture.
     */
    fun prepareCameraCapture(): Uri {
        val uri = createTempImageUseCase()
        tempImageUri = uri
        isCapturingImage = true
        return uri
    }

    /**
     * Handles result from camera capture.
     */
    fun handleCameraResult(success: Boolean) {
        viewModelScope.launch {
            isCapturingImage = false
            if (success && tempImageUri != null) {
                try {
                    val result = imageCaptureUseCase.invoke(
                        ImageCaptureUseCase.Params(
                            source = ImageCaptureUseCase.ImageSource.CAMERA,
                            imageUri = tempImageUri!!
                        )
                    )
                    
                    if (result.isSuccess) {
                        val imagePath = result.getOrNull()
                        if (imagePath != null) {
                            selectedImages = selectedImages + imagePath
                        }
                    }
                } catch (e: Exception) {
                    // Handle error - could show snackbar
                }
            }
            tempImageUri = null
        }
    }

    /**
     * Handles result from gallery selection.
     */
    fun handleGalleryResult(uri: Uri?) {
        if (uri != null) {
            viewModelScope.launch {
                try {
                    val result = imageCaptureUseCase.invoke(
                        ImageCaptureUseCase.Params(
                            source = ImageCaptureUseCase.ImageSource.GALLERY,
                            imageUri = uri
                        )
                    )
                    
                    if (result.isSuccess) {
                        val imagePath = result.getOrNull()
                        if (imagePath != null) {
                            selectedImages = selectedImages + imagePath
                        }
                    }
                } catch (e: Exception) {
                    // Handle error - could show snackbar
                }
            }
        }
    }

    /**
     * Removes an image from the list.
     */
    fun removeImage(imagePath: String) {
        viewModelScope.launch {
            try {
                deleteImageUseCase.invoke(
                    DeleteImageUseCase.Params(imagePath)
                )
                selectedImages = selectedImages - imagePath
            } catch (e: Exception) {
                // Handle error
            }
        }
    }

    /**
     * Create new maintenance record.
     */
    fun createMaintenance(recordId: Long) {
        viewModelScope.launch {
            _uiState.value = CreateMaintenanceUiState.Loading

            try {
                // Basic validation
                if (description.trim().isBlank()) {
                    _uiState.value = CreateMaintenanceUiState.Error(
                        resourceProvider.getString(R.string.error_description_required)
                    )
                    return@launch
                }

                if (type.trim().isBlank()) {
                    _uiState.value = CreateMaintenanceUiState.Error(
                        resourceProvider.getString(R.string.error_type_required)
                    )
                    return@launch
                }

                // Create maintenance using UseCase
                val createResult = createMaintenanceUseCase.invoke(
                    CreateMaintenanceUseCase.Params(
                        recordId = recordId,
                        maintenanceDate = LocalDateTime.now(),
                        description = description.trim(),
                        type = type.trim(),
                        cost = cost.trim().toBigDecimalOrNull(),
                        currency = currency,
                        performedBy = performedBy.trim().ifBlank { null },
                        location = location.trim().ifBlank { null },
                        durationMinutes = durationMinutes.trim().toIntOrNull(),
                        partsReplaced = partsReplaced.trim().ifBlank { null },
                        nextMaintenanceDue = null, // Can be calculated based on recurrence
                        priority = priority,
                        imagesPaths = selectedImages,
                        notes = notes.trim().ifBlank { null },
                        isRecurring = isRecurring,
                        recurrenceIntervalDays = if (isRecurring) recurrenceIntervalDays.trim().toIntOrNull() else null
                    )
                )

                if (createResult.isSuccess) {
                    _uiState.value = CreateMaintenanceUiState.Success
                } else {
                    _uiState.value = CreateMaintenanceUiState.Error(
                        createResult.exceptionOrNull()?.message ?: resourceProvider.getString(R.string.error_generic)
                    )
                }
            } catch (e: Exception) {
                _uiState.value = CreateMaintenanceUiState.Error(
                    e.message ?: resourceProvider.getString(R.string.error_generic)
                )
            }
        }
    }
    
    /**
     * Initialize the ViewModel with a record ID and load any existing draft.
     */
    fun initializeWithRecord(recordId: Long) {
        currentRecordId = recordId
        loadExistingDraft()
    }
    
    /**
     * Load existing draft for the current record.
     */
    private fun loadExistingDraft() {
        if (isDraftLoaded || currentRecordId == 0L) return
        
        viewModelScope.launch {
            try {
                loadDraftUseCase.invoke(LoadMaintenanceDraftUseCase.Params(currentRecordId))
                    .onSuccess { draft ->
                        draft?.let { loadDraft(it) }
                        isDraftLoaded = true
                    }
                    .onError { _, _ ->
                        // Silent fail - drafts are optional
                    }
            } catch (e: Exception) {
                // Silent fail - drafts are optional
            }
        }
    }
    
    /**
     * Load draft data into form fields.
     */
    private fun loadDraft(draft: MaintenanceDraft) {
        description = draft.description
        type = draft.type
        cost = draft.cost
        currency = draft.currency
        performedBy = draft.performedBy
        location = draft.location
        durationMinutes = draft.durationMinutes
        partsReplaced = draft.partsReplaced
        notes = draft.notes
        priority = draft.priority
        isRecurring = draft.isRecurring
        recurrenceIntervalDays = draft.recurrenceIntervalDays
        selectedImages = draft.selectedImages
    }
    
    /**
     * Schedule auto-save after a delay.
     */
    private fun scheduleAutoSave() {
        autoSaveJob?.cancel()
        autoSaveJob = viewModelScope.launch {
            delay(AUTO_SAVE_DELAY_MS)
            saveDraftIfNeeded()
        }
    }
    
    /**
     * Save current form state as draft.
     */
    private suspend fun saveDraftIfNeeded() {
        if (currentRecordId == 0L) return
        
        val draft = createDraftFromCurrentState()
        if (draft.hasContent) {
            try {
                saveDraftUseCase.invoke(SaveMaintenanceDraftUseCase.Params(draft))
            } catch (e: Exception) {
                // Silent fail - drafts are optional
            }
        }
    }
    
    /**
     * Create draft from current form state.
     */
    private fun createDraftFromCurrentState(): MaintenanceDraft {
        return MaintenanceDraft(
            recordId = currentRecordId,
            description = description,
            type = type,
            cost = cost,
            currency = currency,
            performedBy = performedBy,
            location = location,
            durationMinutes = durationMinutes,
            partsReplaced = partsReplaced,
            notes = notes,
            priority = priority,
            isRecurring = isRecurring,
            recurrenceIntervalDays = recurrenceIntervalDays,
            selectedImages = selectedImages
        )
    }
    
    /**
     * Clear the current draft.
     */
    fun clearDraft() {
        if (currentRecordId == 0L) return
        
        viewModelScope.launch {
            try {
                deleteDraftUseCase.invoke(DeleteMaintenanceDraftUseCase.Params(recordId = currentRecordId))
            } catch (e: Exception) {
                // Silent fail
            }
        }
    }
    
    /**
     * Save draft manually.
     */
    fun saveDraft() {
        viewModelScope.launch {
            saveDraftIfNeeded()
        }
    }

    /**
     * Reset UI state back to Idle.
     */
    fun resetUiState() {
        _uiState.value = CreateMaintenanceUiState.Idle
    }
    
    override fun onCleared() {
        super.onCleared()
        autoSaveJob?.cancel()
    }
}

/**
 * UI state for creating a maintenance.
 */
sealed class CreateMaintenanceUiState {
    object Idle : CreateMaintenanceUiState()
    object Loading : CreateMaintenanceUiState()
    object Success : CreateMaintenanceUiState()
    data class Error(val message: String) : CreateMaintenanceUiState()
}
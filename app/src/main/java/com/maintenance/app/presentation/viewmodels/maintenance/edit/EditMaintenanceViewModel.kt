package com.maintenance.app.presentation.viewmodels.maintenance.edit

import android.net.Uri
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.maintenance.app.domain.model.Maintenance
import com.maintenance.app.domain.usecases.maintenances.GetMaintenanceByIdUseCase
import com.maintenance.app.domain.usecases.maintenances.UpdateMaintenanceUseCase
import com.maintenance.app.domain.usecases.images.ImageCaptureUseCase
import com.maintenance.app.domain.usecases.images.DeleteImageUseCase
import com.maintenance.app.domain.usecases.images.CreateTempImageUseCase
import com.maintenance.app.domain.usecases.records.GetRecordByIdUseCase
import com.maintenance.app.domain.usecases.sharing.ShareMaintenanceViaWhatsAppUseCase
import com.maintenance.app.domain.usecases.sharing.ShareMaintenanceGenericUseCase
import com.maintenance.app.domain.usecases.sharing.CheckWhatsAppInstalledUseCase

import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

sealed class EditMaintenanceUiState {
    object Idle : EditMaintenanceUiState()
    object Loading : EditMaintenanceUiState()
    object LoadingMaintenance : EditMaintenanceUiState()
    object Success : EditMaintenanceUiState()
    data class Error(val message: String) : EditMaintenanceUiState()
}

@HiltViewModel
class EditMaintenanceViewModel @Inject constructor(
    private val getMaintenanceByIdUseCase: GetMaintenanceByIdUseCase,
    private val updateMaintenanceUseCase: UpdateMaintenanceUseCase,
    private val imageCaptureUseCase: ImageCaptureUseCase,
    private val deleteImageUseCase: DeleteImageUseCase,
    private val createTempImageUseCase: CreateTempImageUseCase,
    private val getRecordByIdUseCase: GetRecordByIdUseCase,
    private val shareMaintenanceViaWhatsAppUseCase: ShareMaintenanceViaWhatsAppUseCase,
    private val shareMaintenanceGenericUseCase: ShareMaintenanceGenericUseCase,
    private val checkWhatsAppInstalledUseCase: CheckWhatsAppInstalledUseCase,
    savedStateHandle: SavedStateHandle
) : ViewModel() {

    private val maintenanceId: Long = savedStateHandle.get<Long>("maintenanceId") ?: 0L

    private val _uiState = MutableStateFlow<EditMaintenanceUiState>(EditMaintenanceUiState.Idle)
    val uiState: StateFlow<EditMaintenanceUiState> = _uiState.asStateFlow()

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
    var maintenanceDate by mutableStateOf<java.time.LocalDate?>(null)
        private set
    var maintenanceType by mutableStateOf("")
        private set
    var nextMaintenanceDue by mutableStateOf<java.time.LocalDate?>(null)
        private set

    // Image fields
    var selectedImages by mutableStateOf<List<String>>(emptyList())
        private set
    var isCapturingImage by mutableStateOf(false)
        private set
    var tempImageUri by mutableStateOf<Uri?>(null)
        private set

    // Validation errors
    var descriptionError by mutableStateOf<String?>(null)
        private set
    var typeError by mutableStateOf<String?>(null)
        private set
    var costError by mutableStateOf<String?>(null)
        private set

    private var originalMaintenance: Maintenance? = null

    // Sharing states
    private val _isWhatsAppAvailable = MutableStateFlow(false)
    val isWhatsAppAvailable: StateFlow<Boolean> = _isWhatsAppAvailable.asStateFlow()

    private val _shareLoading = MutableStateFlow(false)
    val shareLoading: StateFlow<Boolean> = _shareLoading.asStateFlow()

    private val _shareError = MutableStateFlow<String?>(null)
    val shareError: StateFlow<String?> = _shareError.asStateFlow()

    init {
        loadMaintenance()
        checkWhatsAppAvailability()
    }

    private fun loadMaintenance() {
        viewModelScope.launch {
            _uiState.value = EditMaintenanceUiState.LoadingMaintenance
            try {
                val maintenance = getMaintenanceByIdUseCase.invoke(
                    GetMaintenanceByIdUseCase.Params(maintenanceId)
                ).getOrNull()
                if (maintenance != null) {
                    originalMaintenance = maintenance
                    populateFormWithMaintenance(maintenance)
                    _uiState.value = EditMaintenanceUiState.Idle
                } else {
                    _uiState.value = EditMaintenanceUiState.Error("Mantenimiento no encontrado")
                }
            } catch (e: Exception) {
                _uiState.value = EditMaintenanceUiState.Error(
                    e.message ?: "Error al cargar el mantenimiento"
                )
            }
        }
    }

    private fun populateFormWithMaintenance(maintenance: Maintenance) {
        description = maintenance.description
        type = maintenance.type
        cost = maintenance.cost?.toString() ?: ""
        currency = maintenance.currency.takeIf { it.isNotBlank() } ?: "USD"
        performedBy = maintenance.performedBy ?: ""
        location = maintenance.location ?: ""
        durationMinutes = maintenance.durationMinutes?.toString() ?: ""
        partsReplaced = maintenance.partsReplaced ?: ""
        notes = maintenance.notes ?: ""
        priority = maintenance.priority
        isRecurring = maintenance.isRecurring
        recurrenceIntervalDays = maintenance.recurrenceIntervalDays?.toString() ?: ""
        selectedImages = maintenance.imagesPaths
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
    // Update functions
    fun updateDescription(value: String) {
        description = value
        descriptionError = null
    }

    fun updateType(value: String) {
        type = value
        typeError = null
    }

    fun updateCost(value: String) {
        cost = value
        costError = null
    }

    fun updateCurrency(value: String) {
        currency = value
    }

    fun updatePerformedBy(value: String) {
        performedBy = value
    }

    fun updateLocation(value: String) {
        location = value
    }

    fun updateDuration(value: String) {
        durationMinutes = value
    }

    fun updatePartsReplaced(value: String) {
        partsReplaced = value
    }

    fun updateNotes(value: String) {
        notes = value
    }

    fun updatePriority(value: Maintenance.Priority) {
        priority = value
    }

    fun updateRecurring(value: Boolean) {
        isRecurring = value
        if (!value) {
            recurrenceIntervalDays = ""
        }
    }

    fun updateRecurrenceInterval(value: String) {
        recurrenceIntervalDays = value
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

    fun updateMaintenance() {
        viewModelScope.launch {
            _uiState.value = EditMaintenanceUiState.Loading

            // Basic validation
            if (description.trim().isBlank()) {
                descriptionError = "La descripción es requerida"
                _uiState.value = EditMaintenanceUiState.Idle
                return@launch
            }

            if (type.trim().isBlank()) {
                typeError = "El tipo de mantenimiento es requerido"
                _uiState.value = EditMaintenanceUiState.Idle
                return@launch
            }

            try {
                val original = originalMaintenance ?: return@launch

                val updatedMaintenance = original.copy(
                    description = description,
                    type = type,
                    cost = cost.toBigDecimalOrNull(),
                    currency = currency.takeIf { it.isNotBlank() } ?: original.currency,
                    performedBy = performedBy.takeIf { it.isNotBlank() },
                    location = location.takeIf { it.isNotBlank() },
                    durationMinutes = durationMinutes.toIntOrNull(),
                    partsReplaced = partsReplaced.takeIf { it.isNotBlank() },
                    notes = notes.takeIf { it.isNotBlank() },
                    priority = priority,
                    imagesPaths = selectedImages,
                    isRecurring = isRecurring,
                    recurrenceIntervalDays = if (isRecurring) {
                        recurrenceIntervalDays.toIntOrNull()
                    } else null,
                    updatedDate = java.time.LocalDateTime.now()
                )

                val result = updateMaintenanceUseCase.invoke(
                    UpdateMaintenanceUseCase.Params(updatedMaintenance)
                )
                if (result.isError) {
                    throw (result as com.maintenance.app.utils.Result.Error).exception 
                        ?: Exception("Failed to update maintenance")
                }
                _uiState.value = EditMaintenanceUiState.Success
            } catch (e: Exception) {
                _uiState.value = EditMaintenanceUiState.Error(
                    e.message ?: "Error al actualizar el mantenimiento"
                )
            }
        }
    }

    fun updateDurationMinutes(value: String) {
        durationMinutes = value
    }

    fun updateMaintenanceDate(date: java.time.LocalDate) {
        maintenanceDate = date
    }

    fun updateMaintenanceType(value: String) {
        maintenanceType = value
    }

    fun updateNextMaintenanceDue(date: java.time.LocalDate) {
        nextMaintenanceDue = date
    }

    fun updateStatus(value: String) {
        // Placeholder for status update
    }

    fun saveMaintenance() {
        updateMaintenance()
    }

    fun deleteMaintenance() {
        viewModelScope.launch {
            _uiState.value = EditMaintenanceUiState.Loading
            try {
                // TODO: Implement delete maintenance logic
                _uiState.value = EditMaintenanceUiState.Success
            } catch (e: Exception) {
                _uiState.value = EditMaintenanceUiState.Error(
                    e.message ?: "Error al eliminar el mantenimiento"
                )
            }
        }
    }

    /**
     * Share the current maintenance via WhatsApp.
     */
    fun shareMaintenanceViaWhatsApp(recordName: String) {
        val maintenance = originalMaintenance ?: return

        viewModelScope.launch {
            _shareLoading.value = true
            _shareError.value = null

            try {
                val result = shareMaintenanceViaWhatsAppUseCase(
                    ShareMaintenanceViaWhatsAppUseCase.Params(maintenance, recordName)
                )
                val success = result.getOrDefault(false)
                if (!success) {
                    _shareError.value = "Failed to share maintenance via WhatsApp"
                }
            } catch (e: Exception) {
                _shareError.value = e.message ?: "Error sharing maintenance"
            } finally {
                _shareLoading.value = false
            }
        }
    }

    /**
     * Share the current maintenance via generic share intent.
     */
    fun shareMaintenanceGeneric(recordName: String) {
        val maintenance = originalMaintenance ?: return

        viewModelScope.launch {
            _shareLoading.value = true
            _shareError.value = null

            try {
                val result = shareMaintenanceGenericUseCase(
                    ShareMaintenanceGenericUseCase.Params(maintenance, recordName)
                )
                val success = result.getOrDefault(false)
                if (!success) {
                    _shareError.value = "Failed to share maintenance"
                }
            } catch (e: Exception) {
                _shareError.value = e.message ?: "Error sharing maintenance"
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
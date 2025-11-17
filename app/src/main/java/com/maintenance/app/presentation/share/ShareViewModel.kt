package com.maintenance.app.presentation.share

import android.content.Context
import android.graphics.Bitmap
import android.net.Uri
import androidx.core.content.FileProvider
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.maintenance.app.domain.model.Maintenance
import com.maintenance.app.domain.model.Record
import com.maintenance.app.domain.repository.MaintenanceRepository
import com.maintenance.app.domain.repository.RecordRepository
import com.maintenance.app.presentation.utils.ImageGenerationUtils
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * State for share functionality
 */
data class ShareUiState(
    val record: Record? = null,
    val maintenance: Maintenance? = null,
    val generatedBitmap: Bitmap? = null,
    val imageUri: Uri? = null,
    val isLoading: Boolean = false,
    val errorMessage: String? = null,
    val successMessage: String? = null,
    val shareText: String? = null
)

@HiltViewModel
class ShareViewModel @Inject constructor(
    private val recordRepository: RecordRepository,
    private val maintenanceRepository: MaintenanceRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(ShareUiState())
    val uiState: StateFlow<ShareUiState> = _uiState.asStateFlow()

    /**
     * Load record and generate image
     */
    fun loadAndGenerateImage(recordId: Long, maintenanceId: Long? = null) {
        viewModelScope.launch {
            try {
                _uiState.value = _uiState.value.copy(isLoading = true, errorMessage = null)

                // Load record
                val recordResult = recordRepository.getRecordById(recordId)
                val record = when (recordResult) {
                    is com.maintenance.app.utils.Result.Success -> recordResult.data
                    is com.maintenance.app.utils.Result.Error -> null
                    is com.maintenance.app.utils.Result.Loading -> null
                }

                if (record == null) {
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        errorMessage = "Record not found"
                    )
                    return@launch
                }

                // Load maintenance if provided
                var maintenance: Maintenance? = null
                if (maintenanceId != null) {
                    val maintenanceResult = maintenanceRepository.getMaintenanceById(maintenanceId)
                    maintenance = when (maintenanceResult) {
                        is com.maintenance.app.utils.Result.Success -> maintenanceResult.data
                        is com.maintenance.app.utils.Result.Error -> null
                        is com.maintenance.app.utils.Result.Loading -> null
                    }
                }

                _uiState.value = _uiState.value.copy(
                    record = record,
                    maintenance = maintenance,
                    isLoading = false
                )
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    errorMessage = e.message ?: "Error loading record"
                )
            }
        }
    }

    /**
     * Generate image from record
     */
    fun generateImage(context: Context, record: Record, maintenance: Maintenance? = null) {
        viewModelScope.launch {
            try {
                _uiState.value = _uiState.value.copy(isLoading = true)

                val bitmap = ImageGenerationUtils.generateRecordImage(context, record, maintenance)
                val file = ImageGenerationUtils.saveBitmapToFile(bitmap, context)
                val uri = FileProvider.getUriForFile(
                    context,
                    "${context.packageName}.fileprovider",
                    file
                )

                _uiState.value = _uiState.value.copy(
                    generatedBitmap = bitmap,
                    imageUri = uri,
                    isLoading = false
                )

                // Generate share text
                generateShareText(record, maintenance)
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    errorMessage = e.message ?: "Error generating image"
                )
            }
        }
    }

    /**
     * Generate device summary image
     */
    fun generateDeviceSummaryImage(
        context: Context,
        deviceName: String,
        totalMaintenance: Int,
        totalCost: String,
        recentMaintenances: List<Pair<String, String>>
    ) {
        viewModelScope.launch {
            try {
                _uiState.value = _uiState.value.copy(isLoading = true)

                val bitmap = ImageGenerationUtils.generateDeviceSummaryImage(
                    context,
                    deviceName,
                    totalMaintenance,
                    totalCost,
                    recentMaintenances
                )
                val file = ImageGenerationUtils.saveBitmapToFile(bitmap, context)
                val uri = FileProvider.getUriForFile(
                    context,
                    "${context.packageName}.fileprovider",
                    file
                )

                _uiState.value = _uiState.value.copy(
                    generatedBitmap = bitmap,
                    imageUri = uri,
                    isLoading = false
                )
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    errorMessage = e.message ?: "Error generating summary image"
                )
            }
        }
    }

    /**
     * Generate share text
     */
    private fun generateShareText(record: Record, maintenance: Maintenance?) {
        val text = if (maintenance != null) {
            """
            🔧 Maintenance Record - ${record.name}
            
            Type: ${maintenance.type}
            Category: ${record.category}
            Cost: $${maintenance.cost}
            
            Description:
            ${maintenance.description}
            
            Generated by MaintenanceApp
            """.trimIndent()
        } else {
            """
            📱 Device: ${record.name}
            Category: ${record.category}
            
            ${record.description}
            
            Generated by MaintenanceApp
            """.trimIndent()
        }

        _uiState.value = _uiState.value.copy(shareText = text)
    }

    /**
     * Update share text
     */
    fun updateShareText(text: String) {
        _uiState.value = _uiState.value.copy(shareText = text)
    }

    /**
     * Clear success message
     */
    fun clearSuccessMessage() {
        _uiState.value = _uiState.value.copy(successMessage = null)
    }

    /**
     * Clear error message
     */
    fun clearErrorMessage() {
        _uiState.value = _uiState.value.copy(errorMessage = null)
    }

    /**
     * Show success message
     */
    private fun showSuccess(message: String) {
        _uiState.value = _uiState.value.copy(successMessage = message)
    }

    /**
     * Show error message
     */
    private fun showError(message: String) {
        _uiState.value = _uiState.value.copy(errorMessage = message)
    }
}

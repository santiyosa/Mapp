package com.maintenance.app.domain.model

import java.time.LocalDateTime

/**
 * Data class representing a draft of maintenance form data.
 */
data class MaintenanceDraft(
    val id: Long = 0,
    val recordId: Long,
    val description: String = "",
    val type: String = "",
    val cost: String = "",
    val currency: String = "USD",
    val performedBy: String = "",
    val location: String = "",
    val durationMinutes: String = "",
    val partsReplaced: String = "",
    val notes: String = "",
    val priority: Maintenance.Priority = Maintenance.Priority.MEDIUM,
    val isRecurring: Boolean = false,
    val recurrenceIntervalDays: String = "",
    val selectedImages: List<String> = emptyList(),
    val createdAt: LocalDateTime = LocalDateTime.now(),
    val updatedAt: LocalDateTime = LocalDateTime.now()
) {
    /**
     * Check if the draft has any meaningful content.
     */
    val hasContent: Boolean
        get() = description.isNotBlank() || 
               type.isNotBlank() || 
               cost.isNotBlank() ||
               performedBy.isNotBlank() ||
               location.isNotBlank() ||
               durationMinutes.isNotBlank() ||
               partsReplaced.isNotBlank() ||
               notes.isNotBlank() ||
               selectedImages.isNotEmpty()

    /**
     * Get completion percentage based on filled fields.
     */
    val completionPercentage: Int
        get() {
            val totalFields = 9 // Total form fields we consider for completion
            var filledFields = 0
            
            if (description.isNotBlank()) filledFields++
            if (type.isNotBlank()) filledFields++
            if (cost.isNotBlank()) filledFields++
            if (performedBy.isNotBlank()) filledFields++
            if (location.isNotBlank()) filledFields++
            if (durationMinutes.isNotBlank()) filledFields++
            if (partsReplaced.isNotBlank()) filledFields++
            if (notes.isNotBlank()) filledFields++
            if (selectedImages.isNotEmpty()) filledFields++
            
            return (filledFields * 100) / totalFields
        }
}
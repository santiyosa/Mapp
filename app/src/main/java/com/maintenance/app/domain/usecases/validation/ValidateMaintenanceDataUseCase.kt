package com.maintenance.app.domain.usecases.validation

import com.maintenance.app.domain.model.Maintenance
import com.maintenance.app.domain.usecases.base.UseCase
import com.maintenance.app.utils.ValidationResult
import java.math.BigDecimal
import java.time.LocalDateTime
import javax.inject.Inject

/**
 * Use case for validating maintenance data before creation or update.
 */
class ValidateMaintenanceDataUseCase @Inject constructor() : UseCase<ValidateMaintenanceDataUseCase.Params, ValidationResult>() {

    override suspend fun execute(parameters: Params): ValidationResult {
        val errors = mutableListOf<String>()

        // Validate record ID
        if (parameters.recordId <= 0) {
            errors.add("Valid record ID is required")
        }

        // Validate description
        if (parameters.description.isBlank()) {
            errors.add("Description is required")
        } else if (parameters.description.length > 500) {
            errors.add("Description must be 500 characters or less")
        }

        // Validate type
        if (parameters.type.isBlank()) {
            errors.add("Maintenance type is required")
        } else if (parameters.type.length > 50) {
            errors.add("Maintenance type must be 50 characters or less")
        }

        // Validate maintenance date
        if (parameters.maintenanceDate.isAfter(LocalDateTime.now().plusDays(1))) {
            errors.add("Maintenance date cannot be more than 1 day in the future")
        }

        // Validate cost
        if (parameters.cost != null) {
            if (parameters.cost < BigDecimal.ZERO) {
                errors.add("Cost cannot be negative")
            } else if (parameters.cost > BigDecimal("999999.99")) {
                errors.add("Cost cannot exceed 999,999.99")
            }
        }

        // Validate currency
        if (parameters.currency.isBlank()) {
            errors.add("Currency is required")
        } else if (parameters.currency.length != 3) {
            errors.add("Currency must be a 3-character ISO code (e.g., USD, EUR)")
        }

        // Validate performed by
        if (parameters.performedBy != null && parameters.performedBy.length > 100) {
            errors.add("Performed by field must be 100 characters or less")
        }

        // Validate location
        if (parameters.location != null && parameters.location.length > 100) {
            errors.add("Location must be 100 characters or less")
        }

        // Validate duration
        if (parameters.durationMinutes != null) {
            if (parameters.durationMinutes < 0) {
                errors.add("Duration cannot be negative")
            } else if (parameters.durationMinutes > 10080) { // 7 days in minutes
                errors.add("Duration cannot exceed 7 days (10,080 minutes)")
            }
        }

        // Validate parts replaced
        if (parameters.partsReplaced != null && parameters.partsReplaced.length > 500) {
            errors.add("Parts replaced field must be 500 characters or less")
        }

        // Validate next maintenance due date
        if (parameters.nextMaintenanceDue != null) {
            if (parameters.nextMaintenanceDue.isBefore(parameters.maintenanceDate)) {
                errors.add("Next maintenance due date cannot be before the current maintenance date")
            }
        }

        // Validate notes
        if (parameters.notes != null && parameters.notes.length > 1000) {
            errors.add("Notes must be 1000 characters or less")
        }

        // Validate recurrence settings
        if (parameters.isRecurring) {
            if (parameters.recurrenceIntervalDays == null || parameters.recurrenceIntervalDays <= 0) {
                errors.add("Recurrence interval must be specified and positive for recurring maintenances")
            } else if (parameters.recurrenceIntervalDays > 3650) { // 10 years
                errors.add("Recurrence interval cannot exceed 10 years (3650 days)")
            }
        }

        return ValidationResult(
            isValid = errors.isEmpty(),
            errors = errors
        )
    }

    data class Params(
        val recordId: Long,
        val maintenanceDate: LocalDateTime,
        val description: String,
        val type: String,
        val cost: BigDecimal? = null,
        val currency: String = "USD",
        val performedBy: String? = null,
        val location: String? = null,
        val durationMinutes: Int? = null,
        val partsReplaced: String? = null,
        val nextMaintenanceDue: LocalDateTime? = null,
        val priority: Maintenance.Priority = Maintenance.Priority.MEDIUM,
        val status: Maintenance.Status = Maintenance.Status.COMPLETED,
        val notes: String? = null,
        val isRecurring: Boolean = false,
        val recurrenceIntervalDays: Int? = null
    )
}
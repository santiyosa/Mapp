package com.maintenance.app.domain.usecases.validation

import com.maintenance.app.domain.usecases.base.UseCase
import com.maintenance.app.utils.ValidationResult
import java.math.BigDecimal
import java.time.LocalDateTime
import javax.inject.Inject

/**
 * Use case for validating record data before creation or update.
 */
class ValidateRecordDataUseCase @Inject constructor() : UseCase<ValidateRecordDataUseCase.Params, ValidationResult>() {

    override suspend fun execute(parameters: Params): ValidationResult {
        val errors = mutableListOf<String>()

        // Validate name
        if (parameters.name.isBlank()) {
            errors.add("Name is required")
        } else if (parameters.name.length > 100) {
            errors.add("Name must be 100 characters or less")
        }

        // Validate description
        if (parameters.description != null && parameters.description.length > 500) {
            errors.add("Description must be 500 characters or less")
        }

        // Validate category
        if (parameters.category != null && parameters.category.length > 50) {
            errors.add("Category must be 50 characters or less")
        }

        // Validate location
        if (parameters.location != null && parameters.location.length > 100) {
            errors.add("Location must be 100 characters or less")
        }

        // Validate brand/model
        if (parameters.brandModel != null && parameters.brandModel.length > 100) {
            errors.add("Brand/Model must be 100 characters or less")
        }

        // Validate serial number
        if (parameters.serialNumber != null && parameters.serialNumber.length > 50) {
            errors.add("Serial number must be 50 characters or less")
        }

        // Validate dates
        if (parameters.purchaseDate != null && parameters.purchaseDate.isAfter(LocalDateTime.now())) {
            errors.add("Purchase date cannot be in the future")
        }

        if (parameters.warrantyExpiryDate != null) {
            if (parameters.purchaseDate != null && parameters.warrantyExpiryDate.isBefore(parameters.purchaseDate)) {
                errors.add("Warranty expiry date cannot be before purchase date")
            }
        }

        // Validate notes
        if (parameters.notes != null && parameters.notes.length > 1000) {
            errors.add("Notes must be 1000 characters or less")
        }

        return ValidationResult(
            isValid = errors.isEmpty(),
            errors = errors
        )
    }

    data class Params(
        val name: String,
        val description: String? = null,
        val category: String? = null,
        val location: String? = null,
        val brandModel: String? = null,
        val serialNumber: String? = null,
        val purchaseDate: LocalDateTime? = null,
        val warrantyExpiryDate: LocalDateTime? = null,
        val notes: String? = null
    )
}
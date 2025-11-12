package com.maintenance.app.utils

/**
 * Data class representing the result of a validation operation.
 */
data class ValidationResult(
    val isValid: Boolean,
    val errors: List<String> = emptyList(),
    val warnings: List<String> = emptyList()
) {
    /**
     * Returns true if there are any errors.
     */
    val hasErrors: Boolean
        get() = errors.isNotEmpty()

    /**
     * Returns true if there are any warnings.
     */
    val hasWarnings: Boolean
        get() = warnings.isNotEmpty()

    /**
     * Returns the first error message, or null if there are no errors.
     */
    val firstError: String?
        get() = errors.firstOrNull()

    /**
     * Returns the first warning message, or null if there are no warnings.
     */
    val firstWarning: String?
        get() = warnings.firstOrNull()

    /**
     * Returns all error messages joined by a separator.
     */
    fun getErrorsAsString(separator: String = "\n"): String {
        return errors.joinToString(separator)
    }

    /**
     * Returns all warning messages joined by a separator.
     */
    fun getWarningsAsString(separator: String = "\n"): String {
        return warnings.joinToString(separator)
    }

    /**
     * Returns all error and warning messages joined by a separator.
     */
    fun getAllMessagesAsString(separator: String = "\n"): String {
        return (errors + warnings).joinToString(separator)
    }

    companion object {
        /**
         * Creates a successful validation result.
         */
        fun success(warnings: List<String> = emptyList()): ValidationResult {
            return ValidationResult(
                isValid = true,
                errors = emptyList(),
                warnings = warnings
            )
        }

        /**
         * Creates a failed validation result with errors.
         */
        fun failure(
            errors: List<String>,
            warnings: List<String> = emptyList()
        ): ValidationResult {
            return ValidationResult(
                isValid = false,
                errors = errors,
                warnings = warnings
            )
        }

        /**
         * Creates a failed validation result with a single error.
         */
        fun failure(
            error: String,
            warnings: List<String> = emptyList()
        ): ValidationResult {
            return ValidationResult(
                isValid = false,
                errors = listOf(error),
                warnings = warnings
            )
        }
    }
}
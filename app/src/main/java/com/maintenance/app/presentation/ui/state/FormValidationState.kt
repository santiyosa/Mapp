package com.maintenance.app.presentation.ui.state

/**
 * Represents the validation state for form fields.
 */
data class FormValidationState(
    val descriptionError: String? = null,
    val typeError: String? = null,
    val costError: String? = null,
    val currencyError: String? = null,
    val performedByError: String? = null,
    val locationError: String? = null,
    val notesError: String? = null,
    val partsReplacedError: String? = null,
    val durationError: String? = null
) {
    /**
     * Returns true if any field has an error.
     */
    val hasErrors: Boolean
        get() = descriptionError != null || typeError != null || costError != null ||
                currencyError != null || performedByError != null || locationError != null ||
                notesError != null || partsReplacedError != null || durationError != null

    /**
     * Returns true if form is valid (no errors).
     */
    val isValid: Boolean
        get() = !hasErrors

    /**
     * Returns the first error found, useful for general error display.
     */
    val firstError: String?
        get() = listOfNotNull(
            descriptionError,
            typeError,
            costError,
            currencyError,
            performedByError,
            locationError,
            notesError,
            partsReplacedError,
            durationError
        ).firstOrNull()
}

/**
 * Represents form loading states.
 */
data class FormLoadingState(
    val isSubmitting: Boolean = false,
    val isValidating: Boolean = false,
    val isSavingDraft: Boolean = false,
    val isLoadingDraft: Boolean = false
) {
    /**
     * Returns true if any loading operation is in progress.
     */
    val isLoading: Boolean
        get() = isSubmitting || isValidating || isSavingDraft || isLoadingDraft
}
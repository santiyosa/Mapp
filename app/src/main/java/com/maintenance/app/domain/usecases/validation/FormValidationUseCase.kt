package com.maintenance.app.domain.usecases.validation

import com.maintenance.app.utils.ValidationResult
import java.math.BigDecimal
import javax.inject.Inject

/**
 * Use case for real-time form field validation.
 */
class FormValidationUseCase @Inject constructor() {

    /**
     * Validates a description field.
     */
    fun validateDescription(description: String): FieldValidationResult {
        return when {
            description.isBlank() -> FieldValidationResult(
                isValid = false,
                errorMessage = "La descripción es requerida"
            )
            description.length > 500 -> FieldValidationResult(
                isValid = false,
                errorMessage = "La descripción debe tener máximo 500 caracteres"
            )
            description.length < 3 -> FieldValidationResult(
                isValid = false,
                errorMessage = "La descripción debe tener al menos 3 caracteres"
            )
            else -> FieldValidationResult(isValid = true)
        }
    }

    /**
     * Validates maintenance type field with smart suggestions and business rules.
     */
    fun validateType(type: String): FieldValidationResult {
        if (type.isBlank()) {
            return FieldValidationResult(
                isValid = false,
                errorMessage = "El tipo de mantenimiento es requerido"
            )
        }

        val cleanType = type.trim()
        
        // Common maintenance types for validation and suggestions
        val commonTypes = mapOf(
            "preventivo" to listOf("preventivo", "mantenimiento preventivo", "prevención"),
            "correctivo" to listOf("correctivo", "reparación", "arreglo", "corrección"),
            "predictivo" to listOf("predictivo", "diagnóstico", "análisis"),
            "limpieza" to listOf("limpieza", "aseo", "higiene"),
            "calibración" to listOf("calibración", "ajuste", "configuración"),
            "inspección" to listOf("inspección", "revisión", "chequeo", "verificación"),
            "lubricación" to listOf("lubricación", "engrase", "aceite"),
            "cambio de filtro" to listOf("filtro", "cambio de filtro"),
            "cambio de aceite" to listOf("aceite", "cambio de aceite"),
            "soldadura" to listOf("soldadura", "soldar"),
            "pintura" to listOf("pintura", "pintar", "repintado")
        )

        return when {
            cleanType.length < 3 -> FieldValidationResult(
                isValid = false,
                errorMessage = "El tipo debe tener al menos 3 caracteres"
            )
            cleanType.length > 50 -> FieldValidationResult(
                isValid = false,
                errorMessage = "El tipo no puede exceder 50 caracteres"
            )
            cleanType.matches(Regex("""^\d+$""")) -> FieldValidationResult(
                isValid = false,
                errorMessage = "El tipo no puede ser solo números"
            )
            // Check for potential matches with common types
            commonTypes.any { (_, variants) -> 
                variants.any { variant -> 
                    cleanType.lowercase().contains(variant.lowercase()) 
                }
            } -> {
                // Found a match - this is good
                FieldValidationResult(isValid = true)
            }
            // Check for typos or similar words
            else -> {
                val suggestion = findClosestMatch(cleanType.lowercase(), commonTypes)
                if (suggestion != null) {
                    FieldValidationResult(
                        isValid = true,
                        warningMessage = "¿Quiso decir '$suggestion'?"
                    )
                } else {
                    FieldValidationResult(isValid = true)
                }
            }
        }
    }

    /**
     * Find closest match for maintenance type suggestions.
     */
    private fun findClosestMatch(input: String, commonTypes: Map<String, List<String>>): String? {
        val inputLower = input.lowercase()
        
        // Look for partial matches first
        commonTypes.forEach { (standard, variants) ->
            variants.forEach { variant ->
                if (variant.contains(inputLower) || inputLower.contains(variant)) {
                    return standard
                }
            }
        }
        
        // Simple fuzzy matching for common typos
        val typoMap = mapOf(
            "preventibo" to "preventivo",
            "correctibo" to "correctivo",
            "reparacion" to "reparación",
            "inspeccion" to "inspección",
            "lubricacion" to "lubricación",
            "calibracion" to "calibración"
        )
        
        return typoMap[inputLower]
    }

    /**
     * Validates a cost field input as string with advanced formatting support.
     */
    fun validateCostInput(costString: String): FieldValidationResult {
        if (costString.isBlank()) {
            return FieldValidationResult(isValid = true) // Cost is optional
        }

        // Clean the input - remove currency symbols, spaces, and common separators
        val cleanedCost = costString
            .replace("$", "")
            .replace("€", "")
            .replace("£", "")
            .replace("¥", "")
            .replace(",", "")
            .replace(" ", "")
            .trim()

        return try {
            val cost = cleanedCost.toDouble()
            when {
                cost < 0 -> FieldValidationResult(
                    isValid = false,
                    errorMessage = "El costo no puede ser negativo"
                )
                cost == 0.0 -> FieldValidationResult(
                    isValid = true,
                    warningMessage = "Costo de $0 - ¿está seguro?"
                )
                cost > 999999.99 -> FieldValidationResult(
                    isValid = false,
                    errorMessage = "El costo no puede exceder $999,999.99"
                )
                cost < 0.01 -> FieldValidationResult(
                    isValid = false,
                    errorMessage = "El costo mínimo es $0.01"
                )
                cost > 50000 -> FieldValidationResult(
                    isValid = true,
                    warningMessage = "Costo alto (>${String.format("%.2f", cost)}) - verifique si es correcto"
                )
                else -> FieldValidationResult(isValid = true)
            }
        } catch (e: NumberFormatException) {
            // Check for common formatting mistakes
            val errorMessage = when {
                costString.contains("[a-zA-Z]".toRegex()) -> 
                    "El costo no puede contener letras. Solo números y punto decimal"
                costString.count { it == '.' } > 1 -> 
                    "Use solo un punto decimal en el costo"
                costString.contains("..") -> 
                    "Formato inválido - puntos decimales duplicados"
                else -> 
                    "Formato de costo inválido. Ejemplo: 150.50"
            }
            
            FieldValidationResult(
                isValid = false,
                errorMessage = errorMessage
            )
        }
    }

    /**
     * Validates currency code.
     */
    fun validateCurrency(currency: String): FieldValidationResult {
        val validCurrencies = setOf("USD", "EUR", "GBP", "JPY", "CAD", "AUD", "CHF", "CNY", "MXN", "BRL", "COP", "PEN", "CLP", "ARS")
        
        return when {
            currency.isBlank() -> FieldValidationResult(
                isValid = false,
                errorMessage = "La moneda es requerida"
            )
            currency.length != 3 -> FieldValidationResult(
                isValid = false,
                errorMessage = "La moneda debe tener 3 caracteres (ej: USD, EUR)"
            )
            !validCurrencies.contains(currency.uppercase()) -> FieldValidationResult(
                isValid = false,
                errorMessage = "Código de moneda no válido"
            )
            else -> FieldValidationResult(isValid = true)
        }
    }

    /**
     * Validates optional text fields with length limit.
     */
    fun validateOptionalText(text: String, fieldName: String, maxLength: Int): FieldValidationResult {
        return when {
            text.length > maxLength -> FieldValidationResult(
                isValid = false,
                errorMessage = "$fieldName debe tener máximo $maxLength caracteres"
            )
            else -> FieldValidationResult(isValid = true)
        }
    }

    /**
     * Validates performed by field.
     */
    fun validatePerformedBy(performedBy: String): FieldValidationResult {
        return validateOptionalText(performedBy, "Realizado por", 100)
    }

    /**
     * Validates location field.
     */
    fun validateLocation(location: String): FieldValidationResult {
        return validateOptionalText(location, "Ubicación", 100)
    }

    /**
     * Validates notes field.
     */
    fun validateNotes(notes: String): FieldValidationResult {
        return validateOptionalText(notes, "Notas", 1000)
    }

    /**
     * Validates parts replaced field.
     */
    fun validatePartsReplaced(partsReplaced: String): FieldValidationResult {
        return validateOptionalText(partsReplaced, "Piezas reemplazadas", 500)
    }

    /**
     * Validates duration input with flexible formatting support.
     * Supports formats like: "120", "2h", "2.5h", "90m", "1h 30m"
     */
    fun validateDurationInput(durationString: String): FieldValidationResult {
        if (durationString.isBlank()) {
            return FieldValidationResult(isValid = true) // Duration is optional
        }

        val cleanInput = durationString.lowercase().trim()
        
        return try {
            val durationInMinutes = when {
                // Format: "2h 30m" or "2 hours 30 minutes"
                cleanInput.contains(Regex("""(\d+\.?\d*)\s*h.*?(\d+)\s*m""")) -> {
                    val match = Regex("""(\d+\.?\d*)\s*h.*?(\d+)\s*m""").find(cleanInput)
                    val hours = match?.groupValues?.get(1)?.toDouble() ?: 0.0
                    val minutes = match?.groupValues?.get(2)?.toInt() ?: 0
                    (hours * 60 + minutes).toInt()
                }
                // Format: "2h" or "2.5h"
                cleanInput.contains(Regex("""(\d+\.?\d*)\s*h""")) -> {
                    val hours = Regex("""(\d+\.?\d*)\s*h""").find(cleanInput)?.groupValues?.get(1)?.toDouble() ?: 0.0
                    (hours * 60).toInt()
                }
                // Format: "90m"
                cleanInput.contains(Regex("""(\d+)\s*m""")) -> {
                    Regex("""(\d+)\s*m""").find(cleanInput)?.groupValues?.get(1)?.toInt() ?: 0
                }
                // Format: Just numbers (assume minutes)
                cleanInput.matches(Regex("""\d+""")) -> {
                    cleanInput.toInt()
                }
                else -> throw NumberFormatException("Formato no reconocido")
            }

            when {
                durationInMinutes < 0 -> FieldValidationResult(
                    isValid = false,
                    errorMessage = "La duración no puede ser negativa"
                )
                durationInMinutes == 0 -> FieldValidationResult(
                    isValid = true,
                    warningMessage = "Duración de 0 minutos - ¿está seguro?"
                )
                durationInMinutes > 10080 -> FieldValidationResult(
                    isValid = false,
                    errorMessage = "La duración no puede exceder 7 días (10,080 minutos)"
                )
                durationInMinutes > 1440 -> FieldValidationResult(
                    isValid = true,
                    warningMessage = "Duración larga (${formatDuration(durationInMinutes)}) - verifique si es correcta"
                )
                durationInMinutes < 5 -> FieldValidationResult(
                    isValid = true,
                    warningMessage = "Duración muy corta (${durationInMinutes} min) - ¿está seguro?"
                )
                else -> FieldValidationResult(isValid = true)
            }
        } catch (e: NumberFormatException) {
            FieldValidationResult(
                isValid = false,
                errorMessage = "Formato de duración inválido. Ejemplos: '120', '2h', '1h 30m', '90m'"
            )
        }
    }

    /**
     * Helper function to format duration for display.
     */
    private fun formatDuration(minutes: Int): String {
        return when {
            minutes >= 1440 -> "${minutes / 1440}d ${(minutes % 1440) / 60}h ${minutes % 60}m"
            minutes >= 60 -> "${minutes / 60}h ${minutes % 60}m"
            else -> "${minutes}m"
        }
    }

    /**
     * Validates recurrence interval with maintenance-specific business rules.
     */
    fun validateRecurrenceInterval(intervalDays: String, maintenanceType: String = ""): FieldValidationResult {
        if (intervalDays.isBlank()) {
            return FieldValidationResult(isValid = true) // Optional field
        }

        return try {
            val days = intervalDays.toInt()
            
            when {
                days <= 0 -> FieldValidationResult(
                    isValid = false,
                    errorMessage = "El intervalo debe ser mayor a 0 días"
                )
                days > 3650 -> FieldValidationResult(
                    isValid = false,
                    errorMessage = "El intervalo no puede exceder 10 años (3,650 días)"
                )
                // Business logic based on maintenance type
                maintenanceType.lowercase().contains("preventivo") && days < 7 -> FieldValidationResult(
                    isValid = true,
                    warningMessage = "Intervalo muy frecuente para mantenimiento preventivo (${days} días)"
                )
                maintenanceType.lowercase().contains("correctivo") -> FieldValidationResult(
                    isValid = true,
                    warningMessage = "Los mantenimientos correctivos generalmente no son recurrentes"
                )
                days < 1 -> FieldValidationResult(
                    isValid = false,
                    errorMessage = "El intervalo mínimo es 1 día"
                )
                days == 1 -> FieldValidationResult(
                    isValid = true,
                    warningMessage = "Mantenimiento diario - ¿está seguro?"
                )
                days > 365 -> FieldValidationResult(
                    isValid = true,
                    warningMessage = "Intervalo muy largo (${formatDaysInterval(days)}) - verifique si es correcto"
                )
                else -> FieldValidationResult(isValid = true)
            }
        } catch (e: NumberFormatException) {
            FieldValidationResult(
                isValid = false,
                errorMessage = "El intervalo debe ser un número entero de días"
            )
        }
    }

    /**
     * Format days interval for display.
     */
    private fun formatDaysInterval(days: Int): String {
        return when {
            days >= 365 -> "${days / 365} años${if (days % 365 > 0) ", ${days % 365} días" else ""}"
            days >= 30 -> "${days / 30} meses${if (days % 30 > 0) ", ${days % 30} días" else ""}"
            days >= 7 -> "${days / 7} semanas${if (days % 7 > 0) ", ${days % 7} días" else ""}"
            else -> "$days días"
        }
    }

    /**
     * Validates form completeness with business rules.
     */
    fun validateFormCompleteness(
        description: String,
        type: String,
        @Suppress("UNUSED_PARAMETER") cost: String,
        isRecurring: Boolean,
        recurrenceInterval: String
    ): FieldValidationResult {
        val issues = mutableListOf<String>()
        
        // Check required fields
        if (description.isBlank()) issues.add("descripción")
        if (type.isBlank()) issues.add("tipo")
        
        // Check logical consistency
        if (isRecurring && recurrenceInterval.isBlank()) {
            issues.add("intervalo de recurrencia (requerido para mantenimientos recurrentes)")
        }
        
        if (!isRecurring && recurrenceInterval.isNotBlank()) {
            return FieldValidationResult(
                isValid = true,
                warningMessage = "Se especificó intervalo de recurrencia pero el mantenimiento no está marcado como recurrente"
            )
        }

        return if (issues.isEmpty()) {
            FieldValidationResult(isValid = true)
        } else {
            FieldValidationResult(
                isValid = false,
                errorMessage = "Campos faltantes: ${issues.joinToString(", ")}"
            )
        }
    }
}

/**
 * Result of validating a single form field.
 */
data class FieldValidationResult(
    val isValid: Boolean,
    val errorMessage: String? = null,
    val warningMessage: String? = null
)
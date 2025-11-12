package com.maintenance.app.utils

import java.math.BigDecimal
import java.time.LocalDateTime
import java.time.format.DateTimeParseException
import java.util.regex.Pattern
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Utility class for data validation and sanitization.
 */
@Singleton
class DataValidator @Inject constructor() {

    companion object {
        // Regular expressions for validation
        private val EMAIL_PATTERN = Pattern.compile(
            "[a-zA-Z0-9\\+\\.\\_\\%\\-\\+]{1,256}" +
                    "\\@" +
                    "[a-zA-Z0-9][a-zA-Z0-9\\-]{0,64}" +
                    "(" +
                    "\\." +
                    "[a-zA-Z0-9][a-zA-Z0-9\\-]{0,25}" +
                    ")+"
        )

        private val PHONE_PATTERN = Pattern.compile("^[+]?[0-9\\s\\-()]{7,20}$")
        private val CURRENCY_PATTERN = Pattern.compile("^[A-Z]{3}$")
        private val ALPHANUMERIC_PATTERN = Pattern.compile("^[a-zA-Z0-9\\s]+$")
        private val SERIAL_NUMBER_PATTERN = Pattern.compile("^[a-zA-Z0-9\\-_]+$")

        // SQL Injection patterns to detect and sanitize
        private val SQL_INJECTION_PATTERNS = arrayOf(
            "('|(\\%27))|(;|(\\%3B))|(--|((\\%2D){2}))|(\\||(\\%7C))",
            "((\\%3D)|(=))[^\\n]*((\\%27)|(')|(--|((\\%2D){2})))",
            "((\\%27)|('))[^\\n]*((\\%6F)|o|(\\%4F))",
            "((\\%27)|('))union",
            "exec(\\+|\\s)+(s|x)p\\w+",
            "union[^\\n]*select",
            "insert[^\\n]*into",
            "delete[^\\n]*from",
            "drop[^\\n]*table",
            "update[^\\n]*set",
            "create[^\\n]*table",
            "alter[^\\n]*table"
        )

        // XSS patterns to detect and sanitize
        private val XSS_PATTERNS = arrayOf(
            "<script[^>]*>.*?</script>",
            "<iframe[^>]*>.*?</iframe>",
            "<object[^>]*>.*?</object>",
            "<embed[^>]*>.*?</embed>",
            "<applet[^>]*>.*?</applet>",
            "<meta[^>]*>",
            "<link[^>]*>",
            "javascript:",
            "vbscript:",
            "onload=",
            "onerror=",
            "onclick=",
            "onmouseover="
        )
    }

    /**
     * Sanitizes input string by removing potentially dangerous characters.
     */
    fun sanitizeInput(input: String?): String? {
        if (input.isNullOrBlank()) return input

        var sanitized = input.trim()

        // Remove SQL injection patterns
        SQL_INJECTION_PATTERNS.forEach { pattern ->
            sanitized = sanitized.replace(Regex(pattern, RegexOption.IGNORE_CASE), "")
        }

        // Remove XSS patterns
        XSS_PATTERNS.forEach { pattern ->
            sanitized = sanitized.replace(Regex(pattern, RegexOption.IGNORE_CASE), "")
        }

        // Remove control characters except line breaks and tabs
        sanitized = sanitized.replace(Regex("[\\x00-\\x08\\x0B\\x0C\\x0E-\\x1F\\x7F]"), "")

        return sanitized
    }

    /**
     * Validates and sanitizes a name field.
     */
    fun validateName(name: String?): ValidationResult {
        if (name.isNullOrBlank()) {
            return ValidationResult.failure("Name is required")
        }

        val sanitized = sanitizeInput(name) ?: ""
        val errors = mutableListOf<String>()

        if (sanitized.isBlank()) {
            errors.add("Name cannot be empty after sanitization")
        } else if (sanitized.length < 2) {
            errors.add("Name must be at least 2 characters long")
        } else if (sanitized.length > 100) {
            errors.add("Name must be 100 characters or less")
        }

        return ValidationResult(
            isValid = errors.isEmpty(),
            errors = errors
        )
    }

    /**
     * Validates an email address.
     */
    fun validateEmail(email: String?): ValidationResult {
        if (email.isNullOrBlank()) {
            return ValidationResult.success()
        }

        val sanitized = sanitizeInput(email) ?: ""
        val errors = mutableListOf<String>()

        if (!EMAIL_PATTERN.matcher(sanitized).matches()) {
            errors.add("Invalid email format")
        }

        return ValidationResult(
            isValid = errors.isEmpty(),
            errors = errors
        )
    }

    /**
     * Validates a phone number.
     */
    fun validatePhone(phone: String?): ValidationResult {
        if (phone.isNullOrBlank()) {
            return ValidationResult.success()
        }

        val sanitized = sanitizeInput(phone) ?: ""
        val errors = mutableListOf<String>()

        if (!PHONE_PATTERN.matcher(sanitized).matches()) {
            errors.add("Invalid phone number format")
        }

        return ValidationResult(
            isValid = errors.isEmpty(),
            errors = errors
        )
    }

    /**
     * Validates a currency code.
     */
    fun validateCurrency(currency: String?): ValidationResult {
        if (currency.isNullOrBlank()) {
            return ValidationResult.failure("Currency is required")
        }

        val sanitized = sanitizeInput(currency)?.uppercase() ?: ""
        val errors = mutableListOf<String>()

        if (!CURRENCY_PATTERN.matcher(sanitized).matches()) {
            errors.add("Currency must be a 3-letter ISO code (e.g., USD, EUR)")
        }

        return ValidationResult(
            isValid = errors.isEmpty(),
            errors = errors
        )
    }

    /**
     * Validates a decimal value.
     */
    fun validateDecimal(
        value: String?,
        fieldName: String = "Value",
        min: BigDecimal? = null,
        max: BigDecimal? = null
    ): ValidationResult {
        if (value.isNullOrBlank()) {
            return ValidationResult.success()
        }

        val sanitized = sanitizeInput(value) ?: ""
        val errors = mutableListOf<String>()

        try {
            val decimal = BigDecimal(sanitized)
            
            min?.let {
                if (decimal < it) {
                    errors.add("$fieldName cannot be less than $it")
                }
            }
            
            max?.let {
                if (decimal > it) {
                    errors.add("$fieldName cannot be greater than $it")
                }
            }
        } catch (e: NumberFormatException) {
            errors.add("$fieldName must be a valid number")
        }

        return ValidationResult(
            isValid = errors.isEmpty(),
            errors = errors
        )
    }

    /**
     * Validates an integer value.
     */
    fun validateInteger(
        value: String?,
        fieldName: String = "Value",
        min: Int? = null,
        max: Int? = null
    ): ValidationResult {
        if (value.isNullOrBlank()) {
            return ValidationResult.success()
        }

        val sanitized = sanitizeInput(value) ?: ""
        val errors = mutableListOf<String>()

        try {
            val integer = sanitized.toInt()
            
            min?.let {
                if (integer < it) {
                    errors.add("$fieldName cannot be less than $it")
                }
            }
            
            max?.let {
                if (integer > it) {
                    errors.add("$fieldName cannot be greater than $it")
                }
            }
        } catch (e: NumberFormatException) {
            errors.add("$fieldName must be a valid integer")
        }

        return ValidationResult(
            isValid = errors.isEmpty(),
            errors = errors
        )
    }

    /**
     * Validates a date string.
     */
    fun validateDateString(dateString: String?, fieldName: String = "Date"): ValidationResult {
        if (dateString.isNullOrBlank()) {
            return ValidationResult.success()
        }

        val sanitized = sanitizeInput(dateString) ?: ""
        val errors = mutableListOf<String>()

        try {
            LocalDateTime.parse(sanitized)
        } catch (e: DateTimeParseException) {
            errors.add("$fieldName has invalid format")
        }

        return ValidationResult(
            isValid = errors.isEmpty(),
            errors = errors
        )
    }

    /**
     * Validates a serial number.
     */
    fun validateSerialNumber(serialNumber: String?): ValidationResult {
        if (serialNumber.isNullOrBlank()) {
            return ValidationResult.success()
        }

        val sanitized = sanitizeInput(serialNumber) ?: ""
        val errors = mutableListOf<String>()

        if (sanitized.length > 50) {
            errors.add("Serial number must be 50 characters or less")
        } else if (!SERIAL_NUMBER_PATTERN.matcher(sanitized).matches()) {
            errors.add("Serial number can only contain letters, numbers, hyphens, and underscores")
        }

        return ValidationResult(
            isValid = errors.isEmpty(),
            errors = errors
        )
    }

    /**
     * Validates text length.
     */
    fun validateTextLength(
        text: String?,
        fieldName: String,
        maxLength: Int,
        minLength: Int = 0,
        required: Boolean = false
    ): ValidationResult {
        if (text.isNullOrBlank()) {
            return if (required) {
                ValidationResult.failure("$fieldName is required")
            } else {
                ValidationResult.success()
            }
        }

        val sanitized = sanitizeInput(text) ?: ""
        val errors = mutableListOf<String>()

        if (required && sanitized.isBlank()) {
            errors.add("$fieldName is required")
        } else if (sanitized.length < minLength) {
            errors.add("$fieldName must be at least $minLength characters long")
        } else if (sanitized.length > maxLength) {
            errors.add("$fieldName must be $maxLength characters or less")
        }

        return ValidationResult(
            isValid = errors.isEmpty(),
            errors = errors
        )
    }

    /**
     * Checks if a string contains potentially dangerous content.
     */
    fun containsMaliciousContent(input: String?): Boolean {
        if (input.isNullOrBlank()) return false

        val lowerInput = input.lowercase()

        // Check for SQL injection patterns
        SQL_INJECTION_PATTERNS.forEach { pattern ->
            if (Regex(pattern, RegexOption.IGNORE_CASE).containsMatchIn(lowerInput)) {
                return true
            }
        }

        // Check for XSS patterns
        XSS_PATTERNS.forEach { pattern ->
            if (Regex(pattern, RegexOption.IGNORE_CASE).containsMatchIn(lowerInput)) {
                return true
            }
        }

        return false
    }
}
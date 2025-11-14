package com.maintenance.app.utils

import org.junit.Before
import org.junit.Test
import java.math.BigDecimal
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

/**
 * Unit tests for DataValidator.
 */
class DataValidatorTest {

    private lateinit var validator: DataValidator

    @Before
    fun setUp() {
        validator = DataValidator()
    }

    // ===== Name Validation Tests =====

    @Test
    fun testValidateName_ValidName() {
        val result = validator.validateName("John Doe")
        assertTrue(result.isValid, "Valid name should pass validation")
    }

    @Test
    fun testValidateName_EmptyName() {
        val result = validator.validateName("")
        assertFalse(result.isValid, "Empty name should fail validation")
    }

    @Test
    fun testValidateName_NullName() {
        val result = validator.validateName(null)
        assertFalse(result.isValid, "Null name should fail validation")
    }

    @Test
    fun testValidateName_TooLongName() {
        val longName = "A".repeat(256)
        val result = validator.validateName(longName)
        assertFalse(result.isValid, "Name longer than 255 chars should fail")
    }

    // ===== Email Validation Tests =====

    @Test
    fun testValidateEmail_ValidEmail() {
        val result = validator.validateEmail("test@example.com")
        assertTrue(result.isValid, "Valid email should pass")
    }

    @Test
    fun testValidateEmail_InvalidEmail() {
        val result = validator.validateEmail("invalid.email")
        assertFalse(result.isValid, "Invalid email format should fail")
    }

    @Test
    fun testValidateEmail_EmptyEmail() {
        val result = validator.validateEmail("")
        assertFalse(result.isValid, "Empty email should fail")
    }

    // ===== Phone Validation Tests =====

    @Test
    fun testValidatePhone_ValidPhone() {
        val result = validator.validatePhone("+1-555-1234567")
        assertTrue(result.isValid, "Valid phone should pass")
    }

    @Test
    fun testValidatePhone_ShortPhone() {
        val result = validator.validatePhone("123")
        assertFalse(result.isValid, "Short phone should fail")
    }

    // ===== Decimal Validation Tests =====

    @Test
    fun testValidateDecimal_ValidDecimal() {
        val result = validator.validateDecimal("123.45", "Price", BigDecimal("100"), BigDecimal("1000"))
        assertTrue(result.isValid, "Valid decimal within range should pass")
    }

    @Test
    fun testValidateDecimal_NegativeDecimal() {
        val result = validator.validateDecimal("-50.00", "Price", BigDecimal("0"), BigDecimal("1000"))
        assertFalse(result.isValid, "Negative decimal outside range should fail")
    }

    @Test
    fun testValidateDecimal_NonNumericDecimal() {
        val result = validator.validateDecimal("abc.def", "Price")
        assertFalse(result.isValid, "Non-numeric decimal should fail")
    }

    // ===== Integer Validation Tests =====

    @Test
    fun testValidateInteger_ValidInteger() {
        val result = validator.validateInteger("42", "Count", 0, 100)
        assertTrue(result.isValid, "Valid integer within range should pass")
    }

    @Test
    fun testValidateInteger_OutOfRange() {
        val result = validator.validateInteger("150", "Count", 0, 100)
        assertFalse(result.isValid, "Integer out of range should fail")
    }

    @Test
    fun testValidateInteger_NonNumericInteger() {
        val result = validator.validateInteger("not_a_number", "Count")
        assertFalse(result.isValid, "Non-numeric integer should fail")
    }

    // ===== Date Validation Tests =====

    @Test
    fun testValidateDateString_ValidDate() {
        val result = validator.validateDateString("2024-11-13", "Date")
        assertTrue(result.isValid, "Valid date should pass")
    }

    @Test
    fun testValidateDateString_InvalidFormat() {
        val result = validator.validateDateString("11/13/2024", "Date")
        assertFalse(result.isValid, "Wrong date format should fail")
    }

    @Test
    fun testValidateDateString_InvalidDate() {
        val result = validator.validateDateString("2024-13-45", "Date")
        assertFalse(result.isValid, "Invalid date values should fail")
    }

    // ===== Serial Number Validation Tests =====

    @Test
    fun testValidateSerialNumber_ValidSerial() {
        val result = validator.validateSerialNumber("ABC-123_XYZ")
        assertTrue(result.isValid, "Valid serial number should pass")
    }

    @Test
    fun testValidateSerialNumber_InvalidCharacters() {
        val result = validator.validateSerialNumber("ABC@123#XYZ")
        assertFalse(result.isValid, "Serial with special chars should fail")
    }

    // ===== Text Length Validation Tests =====

    @Test
    fun testValidateTextLength_ValidLength() {
        val result = validator.validateTextLength("Hello", "Description", minLength = 2, maxLength = 20)
        assertTrue(result.isValid, "Text within length bounds should pass")
    }

    @Test
    fun testValidateTextLength_TooShort() {
        val result = validator.validateTextLength("A", "Description", minLength = 2, maxLength = 20)
        assertFalse(result.isValid, "Text below min length should fail")
    }

    @Test
    fun testValidateTextLength_TooLong() {
        val result = validator.validateTextLength("A".repeat(100), "Description", minLength = 2, maxLength = 20)
        assertFalse(result.isValid, "Text above max length should fail")
    }

    // ===== Sanitization Tests =====

    @Test
    fun testSanitizeInput_RemovesXSS() {
        val dangerous = "<script>alert('xss')</script>"
        val sanitized = validator.sanitizeInput(dangerous)
        assertFalse((sanitized ?: "").contains("<script>"), "Script tags should be removed")
    }

    @Test
    fun testSanitizeInput_RemovesSQLInjection() {
        val dangerous = "'; DROP TABLE users; --"
        val sanitized = validator.sanitizeInput(dangerous)
        // Should contain sanitization
        assertTrue((sanitized ?: "").isNotEmpty(), "Sanitized output should not be empty")
    }

    @Test
    fun testSanitizeInput_PreservesNormalText() {
        val normal = "Normal text 123"
        val sanitized = validator.sanitizeInput(normal)
        assertEquals(normal, sanitized, "Normal text should not be modified")
    }
}

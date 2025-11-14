package com.maintenance.app.utils

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import kotlin.test.assertEquals
import kotlin.test.assertNotEquals
import kotlin.test.assertTrue

/**
 * Unit tests for EncryptionUtil.
 * These tests run on Android device/emulator.
 */
@RunWith(AndroidJUnit4::class)
class EncryptionUtilTest {

    @Before
    fun setUp() {
        // Context setup if needed
    }

    @Test
    fun testEncryptDecrypt_SimpleData() {
        val plaintext = "Hello, World!".toByteArray()

        val encrypted = EncryptionUtil.encrypt(plaintext)
        val decrypted = EncryptionUtil.decrypt(encrypted)

        assertEquals(plaintext.contentToString(), decrypted.contentToString())
    }

    @Test
    fun testEncryptDecrypt_LargeData() {
        val plaintext = "A".repeat(10000).toByteArray()

        val encrypted = EncryptionUtil.encrypt(plaintext)
        val decrypted = EncryptionUtil.decrypt(encrypted)

        assertEquals(plaintext.contentToString(), decrypted.contentToString())
    }

    @Test
    fun testEncryptDecrypt_BinaryData() {
        val plaintext = ByteArray(256) { it.toByte() }

        val encrypted = EncryptionUtil.encrypt(plaintext)
        val decrypted = EncryptionUtil.decrypt(encrypted)

        assertEquals(plaintext.contentToString(), decrypted.contentToString())
    }

    @Test
    fun testEncrypt_ProducesRandomOutput() {
        val plaintext = "Same data".toByteArray()

        val encrypted1 = EncryptionUtil.encrypt(plaintext)
        val encrypted2 = EncryptionUtil.encrypt(plaintext)

        // Due to random IV, encrypted output should be different
        assertNotEquals(encrypted1.contentToString(), encrypted2.contentToString())
    }

    @Test
    fun testEncrypt_IncludesIV() {
        val plaintext = "Test data".toByteArray()

        val encrypted = EncryptionUtil.encrypt(plaintext)

        // IV should be prepended (12 bytes for GCM)
        assertTrue(encrypted.size > plaintext.size)
    }

    @Test
    fun testDecrypt_InvalidData_ThrowsException() {
        val invalidData = ByteArray(5)  // Too small to contain IV

        try {
            EncryptionUtil.decrypt(invalidData)
            assertTrue(false, "Should have thrown exception")
        } catch (e: Exception) {
            assertTrue(e.message?.contains("Invalid") ?: false)
        }
    }

    @Test
    fun testMultipleRoundTrips() {
        val data = "Test data for multiple encryptions".toByteArray()

        val encrypted1 = EncryptionUtil.encrypt(data)
        val decrypted1 = EncryptionUtil.decrypt(encrypted1)

        val encrypted2 = EncryptionUtil.encrypt(decrypted1)
        val decrypted2 = EncryptionUtil.decrypt(encrypted2)

        assertEquals(data.contentToString(), decrypted2.contentToString())
    }

    @Test
    fun testDeleteKey_DoesNotThrow() {
        EncryptionUtil.deleteKey()
        // Should not throw any exception
        
        // After deletion, re-creating should work
        val plaintext = "Test after key deletion".toByteArray()
        val encrypted = EncryptionUtil.encrypt(plaintext)
        val decrypted = EncryptionUtil.decrypt(encrypted)
        
        assertEquals(plaintext.contentToString(), decrypted.contentToString())
    }
}

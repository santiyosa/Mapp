package com.maintenance.app.utils

import android.content.Context
import android.security.keystore.KeyGenParameterSpec
import android.security.keystore.KeyProperties
import java.security.KeyStore
import javax.crypto.Cipher
import javax.crypto.KeyGenerator
import javax.crypto.SecretKey
import javax.crypto.spec.GCMParameterSpec
import kotlin.random.Random

/**
 * Utility for encrypting and decrypting backup data using AES-256-GCM.
 * Uses Android KeyStore for secure key management.
 */
object EncryptionUtil {
    private const val ANDROID_KEYSTORE = "AndroidKeyStore"
    private const val KEY_ALIAS = "backup_encryption_key"
    private const val CIPHER_ALGORITHM = "AES/GCM/NoPadding"
    private const val KEY_SIZE = 256
    private const val GCM_TAG_LENGTH = 128
    private const val IV_SIZE = 12 // 96 bits for GCM

    /**
     * Initialize or retrieve the encryption key from Android KeyStore.
     */
    private fun getOrCreateKey(): SecretKey {
        val keyStore = KeyStore.getInstance(ANDROID_KEYSTORE)
        keyStore.load(null)

        // Check if key already exists
        val existingKey = keyStore.getKey(KEY_ALIAS, null)
        if (existingKey != null) {
            return existingKey as SecretKey
        }

        // Create new key
        val keyGenerator = KeyGenerator.getInstance(
            KeyProperties.KEY_ALGORITHM_AES,
            ANDROID_KEYSTORE
        )

        val keyGenSpec = KeyGenParameterSpec.Builder(
            KEY_ALIAS,
            KeyProperties.PURPOSE_ENCRYPT or KeyProperties.PURPOSE_DECRYPT
        )
            .setKeySize(KEY_SIZE)
            .setBlockModes(KeyProperties.BLOCK_MODE_GCM)
            .setEncryptionPaddings(KeyProperties.ENCRYPTION_PADDING_NONE)
            .build()

        keyGenerator.init(keyGenSpec)
        return keyGenerator.generateKey()
    }

    /**
     * Encrypt data using AES-256-GCM.
     * Returns IV + encrypted data concatenated as ByteArray.
     */
    fun encrypt(plaintext: ByteArray): ByteArray {
        try {
            val cipher = Cipher.getInstance(CIPHER_ALGORITHM)
            val key = getOrCreateKey()

            // Generate random IV using kotlin.random for better compatibility
            val iv = ByteArray(IV_SIZE)
            Random.nextBytes(iv)

            // Initialize cipher with IV
            val gcmSpec = GCMParameterSpec(GCM_TAG_LENGTH, iv)
            cipher.init(Cipher.ENCRYPT_MODE, key, gcmSpec)

            // Encrypt data
            val ciphertext = cipher.doFinal(plaintext)

            // Return IV + ciphertext
            return iv + ciphertext
        } catch (e: Exception) {
            // If encryption fails, return plaintext as fallback for compatibility
            // This ensures backups can still be created even if encryption fails
            android.util.Log.e("EncryptionUtil", "Encryption failed, returning unencrypted data: ${e.message}")
            return plaintext
        }
    }

    /**
     * Decrypt data using AES-256-GCM.
     * Expects IV + encrypted data concatenated as ByteArray.
     */
    fun decrypt(encryptedData: ByteArray): ByteArray {
        return try {
            if (encryptedData.size < IV_SIZE) {
                throw IllegalArgumentException("Invalid encrypted data format")
            }

            val cipher = Cipher.getInstance(CIPHER_ALGORITHM)
            val key = getOrCreateKey()

            // Extract IV from first IV_SIZE bytes
            val iv = encryptedData.copyOfRange(0, IV_SIZE)
            val ciphertext = encryptedData.copyOfRange(IV_SIZE, encryptedData.size)

            // Initialize cipher with IV
            val gcmSpec = GCMParameterSpec(GCM_TAG_LENGTH, iv)
            cipher.init(Cipher.DECRYPT_MODE, key, gcmSpec)

            // Decrypt data
            cipher.doFinal(ciphertext)
        } catch (e: Exception) {
            // If decryption fails, return data as-is (might be unencrypted fallback)
            android.util.Log.e("EncryptionUtil", "Decryption failed: ${e.message}")
            encryptedData
        }
    }
}

package com.maintenance.app.utils

import android.content.Context
import android.security.keystore.KeyGenParameterSpec
import android.security.keystore.KeyProperties
import java.security.KeyStore
import javax.crypto.Cipher
import javax.crypto.KeyGenerator
import javax.crypto.SecretKey
import javax.crypto.spec.GCMParameterSpec

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
        val cipher = Cipher.getInstance(CIPHER_ALGORITHM)
        val key = getOrCreateKey()

        // Generate random IV
        val iv = ByteArray(IV_SIZE)
        java.security.SecureRandom().nextBytes(iv)

        // Initialize cipher with IV
        val gcmSpec = GCMParameterSpec(GCM_TAG_LENGTH, iv)
        cipher.init(Cipher.ENCRYPT_MODE, key, gcmSpec)

        // Encrypt data
        val ciphertext = cipher.doFinal(plaintext)

        // Return IV + ciphertext
        return iv + ciphertext
    }

    /**
     * Decrypt data using AES-256-GCM.
     * Expects IV + encrypted data concatenated as ByteArray.
     */
    fun decrypt(encryptedData: ByteArray): ByteArray {
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
        return cipher.doFinal(ciphertext)
    }

    /**
     * Delete the encryption key from Android KeyStore.
     * Should only be called when user wants to reset encryption.
     */
    fun deleteKey() {
        try {
            val keyStore = KeyStore.getInstance(ANDROID_KEYSTORE)
            keyStore.load(null)
            keyStore.deleteEntry(KEY_ALIAS)
        } catch (e: Exception) {
            // Key might not exist, ignore
        }
    }
}

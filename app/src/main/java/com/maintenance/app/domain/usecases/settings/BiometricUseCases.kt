package com.maintenance.app.domain.usecases.settings

import android.content.Context
import com.maintenance.app.domain.repository.SettingsRepository
import com.maintenance.app.domain.usecases.base.UseCase
import com.maintenance.app.utils.BiometricAuthManager
import com.maintenance.app.utils.Result
import javax.inject.Inject

/**
 * Use case for enabling biometric authentication.
 */
class EnableBiometricUseCase @Inject constructor(
    private val settingsRepository: SettingsRepository,
    private val context: Context
) : UseCase<Unit, Unit>() {

    override suspend fun execute(parameters: Unit): Unit {
        val biometricManager = BiometricAuthManager(context)
        
        if (!biometricManager.isBiometricAvailable()) {
            throw Exception("Biometric authentication is not available on this device")
        }
        
        if (!biometricManager.hasEnrolledBiometrics()) {
            throw Exception("No biometric data enrolled on this device")
        }
        
        // Update settings to enable biometric
        return when (val result = settingsRepository.getSettings()) {
            is Result.Success -> {
                val updatedSettings = result.data.copy(enableBiometric = true)
                when (val updateResult = settingsRepository.updateSettings(updatedSettings)) {
                    is Result.Success -> Unit
                    is Result.Error -> throw updateResult.exception ?: Exception(updateResult.message)
                    is Result.Loading -> throw Exception("Unexpected loading state")
                }
            }
            is Result.Error -> throw result.exception ?: Exception(result.message)
            is Result.Loading -> throw Exception("Unexpected loading state")
        }
    }
}

/**
 * Use case for disabling biometric authentication.
 */
class DisableBiometricUseCase @Inject constructor(
    private val settingsRepository: SettingsRepository
) : UseCase<Unit, Unit>() {

    override suspend fun execute(parameters: Unit): Unit {
        return when (val result = settingsRepository.getSettings()) {
            is Result.Success -> {
                val updatedSettings = result.data.copy(enableBiometric = false)
                when (val updateResult = settingsRepository.updateSettings(updatedSettings)) {
                    is Result.Success -> Unit
                    is Result.Error -> throw updateResult.exception ?: Exception(updateResult.message)
                    is Result.Loading -> throw Exception("Unexpected loading state")
                }
            }
            is Result.Error -> throw result.exception ?: Exception(result.message)
            is Result.Loading -> throw Exception("Unexpected loading state")
        }
    }
}

/**
 * Use case for checking if biometric is available.
 */
class CheckBiometricAvailabilityUseCase @Inject constructor(
    private val context: Context
) : UseCase<Unit, Boolean>() {

    override suspend fun execute(parameters: Unit): Boolean {
        val biometricManager = BiometricAuthManager(context)
        return biometricManager.isBiometricAvailable()
    }
}

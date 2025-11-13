package com.maintenance.app.domain.usecases.settings

import com.maintenance.app.domain.model.AppLanguage
import com.maintenance.app.domain.model.AppSettings
import com.maintenance.app.domain.model.ThemeMode
import com.maintenance.app.domain.repository.SettingsRepository
import com.maintenance.app.domain.usecases.base.UseCase
import com.maintenance.app.utils.Result
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

/**
 * Use case for retrieving application settings.
 */
class GetSettingsUseCase @Inject constructor(
    private val settingsRepository: SettingsRepository
) : UseCase<Unit, AppSettings>() {

    override suspend fun execute(parameters: Unit): AppSettings {
        return when (val result = settingsRepository.getSettings()) {
            is Result.Success -> result.data
            is Result.Error -> throw result.exception ?: Exception(result.message)
            is Result.Loading -> throw Exception("Unexpected loading state")
        }
    }

    fun getSettingsFlow(): Flow<AppSettings> {
        return settingsRepository.getSettingsFlow()
    }
}

/**
 * Use case for updating application theme.
 */
class UpdateThemeUseCase @Inject constructor(
    private val settingsRepository: SettingsRepository
) : UseCase<UpdateThemeUseCase.Params, Unit>() {

    override suspend fun execute(parameters: Params): Unit {
        return when (val result = settingsRepository.getSettings()) {
            is Result.Success -> {
                val updatedSettings = result.data.copy(themeMode = parameters.themeMode)
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

    data class Params(val themeMode: ThemeMode)
}

/**
 * Use case for updating application language.
 */
class UpdateLanguageUseCase @Inject constructor(
    private val settingsRepository: SettingsRepository
) : UseCase<UpdateLanguageUseCase.Params, Unit>() {

    override suspend fun execute(parameters: Params): Unit {
        return when (val result = settingsRepository.getSettings()) {
            is Result.Success -> {
                val updatedSettings = result.data.copy(language = parameters.language)
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

    data class Params(val language: AppLanguage)
}

/**
 * Use case for updating notification settings.
 */
class UpdateNotificationSettingsUseCase @Inject constructor(
    private val settingsRepository: SettingsRepository
) : UseCase<UpdateNotificationSettingsUseCase.Params, Unit>() {

    override suspend fun execute(parameters: Params): Unit {
        return when (val result = settingsRepository.getSettings()) {
            is Result.Success -> {
                val updatedSettings = result.data.copy(enableNotifications = parameters.enableNotifications)
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

    data class Params(val enableNotifications: Boolean)
}

/**
 * Use case for updating biometric settings.
 */
class UpdateBiometricSettingsUseCase @Inject constructor(
    private val settingsRepository: SettingsRepository
) : UseCase<UpdateBiometricSettingsUseCase.Params, Unit>() {

    override suspend fun execute(parameters: Params): Unit {
        return when (val result = settingsRepository.getSettings()) {
            is Result.Success -> {
                val updatedSettings = result.data.copy(enableBiometric = parameters.enableBiometric)
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

    data class Params(val enableBiometric: Boolean)
}

/**
 * Use case for resetting settings to defaults.
 */
class ResetSettingsUseCase @Inject constructor(
    private val settingsRepository: SettingsRepository
) : UseCase<Unit, Unit>() {

    override suspend fun execute(parameters: Unit): Unit {
        return when (val result = settingsRepository.resetToDefaults()) {
            is Result.Success -> Unit
            is Result.Error -> throw result.exception ?: Exception(result.message)
            is Result.Loading -> throw Exception("Unexpected loading state")
        }
    }
}

/**
 * Use case for clearing all user data.
 */
class ClearUserDataUseCase @Inject constructor(
    private val settingsRepository: SettingsRepository
) : UseCase<Unit, Unit>() {

    override suspend fun execute(parameters: Unit): Unit {
        return when (val result = settingsRepository.clearAllData()) {
            is Result.Success -> Unit
            is Result.Error -> throw result.exception ?: Exception(result.message)
            is Result.Loading -> throw Exception("Unexpected loading state")
        }
    }
}

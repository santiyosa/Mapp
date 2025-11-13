package com.maintenance.app.presentation.settings

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.maintenance.app.domain.model.AppLanguage
import com.maintenance.app.domain.model.AppSettings
import com.maintenance.app.domain.model.NotificationSettings
import com.maintenance.app.domain.usecases.settings.CheckBiometricAvailabilityUseCase
import com.maintenance.app.domain.usecases.settings.ClearUserDataUseCase
import com.maintenance.app.domain.usecases.settings.DisableBiometricUseCase
import com.maintenance.app.domain.usecases.settings.EnableBiometricUseCase
import com.maintenance.app.domain.usecases.settings.GetSettingsUseCase
import com.maintenance.app.domain.usecases.settings.ResetSettingsUseCase
import com.maintenance.app.domain.usecases.settings.UpdateLanguageUseCase
import com.maintenance.app.domain.usecases.settings.UpdateNotificationSettingsUseCase
import com.maintenance.app.domain.usecases.settings.UpdateThemeUseCase
import com.maintenance.app.utils.Result
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * Presentation state for settings.
 */
data class SettingsUiState(
    val appSettings: AppSettings? = null,
    val notificationSettings: NotificationSettings = NotificationSettings(),
    val isBiometricAvailable: Boolean = false,
    val isBiometricEnabled: Boolean = false,
    val isLoading: Boolean = false,
    val errorMessage: String? = null,
    val successMessage: String? = null
)

/**
 * ViewModel for managing application settings UI and business logic.
 */
@HiltViewModel
class SettingsViewModel @Inject constructor(
    private val getSettingsUseCase: GetSettingsUseCase,
    private val updateThemeUseCase: UpdateThemeUseCase,
    private val updateLanguageUseCase: UpdateLanguageUseCase,
    private val updateNotificationSettingsUseCase: UpdateNotificationSettingsUseCase,
    private val resetSettingsUseCase: ResetSettingsUseCase,
    private val clearUserDataUseCase: ClearUserDataUseCase,
    private val checkBiometricAvailabilityUseCase: CheckBiometricAvailabilityUseCase,
    private val enableBiometricUseCase: EnableBiometricUseCase,
    private val disableBiometricUseCase: DisableBiometricUseCase
) : ViewModel() {

    private val _uiState = MutableStateFlow(SettingsUiState())
    val uiState: StateFlow<SettingsUiState> = _uiState.asStateFlow()

    init {
        loadSettings()
        checkBiometricAvailability()
    }

    /**
     * Load application settings from repository.
     */
    private fun loadSettings() {
        viewModelScope.launch {
            getSettingsUseCase.getSettingsFlow().collect { settings ->
                _uiState.value = _uiState.value.copy(
                    appSettings = settings,
                    isBiometricEnabled = settings.enableBiometric
                )
            }
        }
    }

    /**
     * Check if biometric authentication is available on the device.
     */
    private fun checkBiometricAvailability() {
        viewModelScope.launch {
            val result = checkBiometricAvailabilityUseCase.invoke(Unit)
            when (result) {
                is Result.Success<*> -> {
                    _uiState.value = _uiState.value.copy(isBiometricAvailable = result.data as? Boolean ?: false)
                }
                is Result.Error -> {
                    // Biometric not available
                    _uiState.value = _uiState.value.copy(isBiometricAvailable = false)
                }
                is Result.Loading -> {}
            }
        }
    }

    /**
     * Update the application theme.
     */
    fun updateTheme(themeMode: com.maintenance.app.domain.model.ThemeMode) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, errorMessage = null)
            val result = updateThemeUseCase.invoke(
                UpdateThemeUseCase.Params(themeMode)
            )
            when (result) {
                is Result.Success<*> -> {
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        successMessage = "Theme updated"
                    )
                    clearSuccessMessage()
                }
                is Result.Error -> {
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        errorMessage = result.message ?: "Failed to update theme"
                    )
                }
                is Result.Loading -> {}
            }
        }
    }

    /**
     * Update the application language.
     */
    fun updateLanguage(language: AppLanguage) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, errorMessage = null)
            val result = updateLanguageUseCase.invoke(
                UpdateLanguageUseCase.Params(language)
            )
            when (result) {
                is Result.Success<*> -> {
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        successMessage = "Language updated"
                    )
                    clearSuccessMessage()
                }
                is Result.Error -> {
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        errorMessage = result.message ?: "Failed to update language"
                    )
                }
                is Result.Loading -> {}
            }
        }
    }

    /**
     * Update notification settings.
     */
    fun updateNotifications(enableNotifications: Boolean) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, errorMessage = null)
            val result = updateNotificationSettingsUseCase.invoke(
                UpdateNotificationSettingsUseCase.Params(enableNotifications)
            )
            when (result) {
                is Result.Success<*> -> {
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        successMessage = "Notifications updated"
                    )
                    clearSuccessMessage()
                }
                is Result.Error -> {
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        errorMessage = result.message ?: "Failed to update notifications"
                    )
                }
                is Result.Loading -> {}
            }
        }
    }

    /**
     * Enable biometric authentication.
     */
    fun enableBiometric() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, errorMessage = null)
            val result = enableBiometricUseCase.invoke(Unit)
            when (result) {
                is Result.Success<*> -> {
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        isBiometricEnabled = true,
                        successMessage = "Biometric enabled"
                    )
                    clearSuccessMessage()
                }
                is Result.Error -> {
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        errorMessage = result.message ?: "Failed to enable biometric"
                    )
                }
                is Result.Loading -> {}
            }
        }
    }

    /**
     * Disable biometric authentication.
     */
    fun disableBiometric() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, errorMessage = null)
            val result = disableBiometricUseCase.invoke(Unit)
            when (result) {
                is Result.Success<*> -> {
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        isBiometricEnabled = false,
                        successMessage = "Biometric disabled"
                    )
                    clearSuccessMessage()
                }
                is Result.Error -> {
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        errorMessage = result.message ?: "Failed to disable biometric"
                    )
                }
                is Result.Loading -> {}
            }
        }
    }

    /**
     * Reset all settings to default values.
     */
    fun resetSettings() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, errorMessage = null)
            val result = resetSettingsUseCase.invoke(Unit)
            when (result) {
                is Result.Success<*> -> {
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        successMessage = "Settings reset to default"
                    )
                    clearSuccessMessage()
                }
                is Result.Error -> {
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        errorMessage = result.message ?: "Failed to reset settings"
                    )
                }
                is Result.Loading -> {}
            }
        }
    }

    /**
     * Clear all user data.
     */
    fun clearAllData() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, errorMessage = null)
            val result = clearUserDataUseCase.invoke(Unit)
            when (result) {
                is Result.Success<*> -> {
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        successMessage = "All data cleared"
                    )
                    clearSuccessMessage()
                }
                is Result.Error -> {
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        errorMessage = result.message ?: "Failed to clear data"
                    )
                }
                is Result.Loading -> {}
            }
        }
    }

    /**
     * Clear error message after displaying.
     */
    fun clearErrorMessage() {
        _uiState.value = _uiState.value.copy(errorMessage = null)
    }

    /**
     * Clear success message after displaying.
     */
    private fun clearSuccessMessage() {
        viewModelScope.launch {
            kotlinx.coroutines.delay(2000) // Show message for 2 seconds
            if (_uiState.value.successMessage != null) {
                _uiState.value = _uiState.value.copy(successMessage = null)
            }
        }
    }
}

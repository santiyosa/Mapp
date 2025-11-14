package com.maintenance.app.presentation.viewmodels.settings

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * ViewModel for managing application settings.
 */
@HiltViewModel
class SettingsViewModel @Inject constructor() : ViewModel() {

    private val _themeMode = MutableStateFlow(ThemeMode.SYSTEM)
    val themeMode: StateFlow<ThemeMode> = _themeMode.asStateFlow()

    private val _fontSize = MutableStateFlow(1.0f)
    val fontSize: StateFlow<Float> = _fontSize.asStateFlow()

    private val _biometricEnabled = MutableStateFlow(false)
    val biometricEnabled: StateFlow<Boolean> = _biometricEnabled.asStateFlow()

    init {
        loadSettings()
    }

    private fun loadSettings() {
        viewModelScope.launch {
            // Load settings from preferences or database
            // For now, using default values
        }
    }

    fun setThemeMode(mode: ThemeMode) {
        viewModelScope.launch {
            _themeMode.value = mode
            // Persist the setting
        }
    }

    fun setFontSize(size: Float) {
        viewModelScope.launch {
            _fontSize.value = size
            // Persist the setting
        }
    }

    fun setBiometricEnabled(enabled: Boolean) {
        viewModelScope.launch {
            _biometricEnabled.value = enabled
            // Persist the setting
        }
    }
}

/**
 * Theme mode options for the application.
 */
enum class ThemeMode {
    LIGHT,
    DARK,
    SYSTEM
}

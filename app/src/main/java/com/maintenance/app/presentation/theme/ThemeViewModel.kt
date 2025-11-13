package com.maintenance.app.presentation.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.runtime.Composable
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.maintenance.app.domain.usecases.settings.GetSettingsUseCase
import com.maintenance.app.domain.usecases.settings.UpdateThemeUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * ViewModel for managing theme state and preferences.
 */
@HiltViewModel
class ThemeViewModel @Inject constructor(
    private val getSettingsUseCase: GetSettingsUseCase,
    private val updateThemeUseCase: UpdateThemeUseCase
) : ViewModel() {

    private val _themeState = MutableStateFlow(ThemeState())
    val themeState: StateFlow<ThemeState> = _themeState.asStateFlow()

    init {
        loadSettings()
    }

    /**
     * Load theme settings from repository.
     */
    private fun loadSettings() {
        viewModelScope.launch {
            getSettingsUseCase.getSettingsFlow().collect { settings ->
                _themeState.value = _themeState.value.copy(
                    themeMode = convertToThemeMode(settings.themeMode),
                    isSystemInDarkTheme = isSystemDarkMode()
                )
            }
        }
    }

    /**
     * Convert domain ThemeMode to presentation ThemeMode.
     */
    private fun convertToThemeMode(domainTheme: com.maintenance.app.domain.model.ThemeMode): ThemeMode {
        return when (domainTheme) {
            com.maintenance.app.domain.model.ThemeMode.LIGHT -> ThemeMode.LIGHT
            com.maintenance.app.domain.model.ThemeMode.DARK -> ThemeMode.DARK
            com.maintenance.app.domain.model.ThemeMode.SYSTEM -> ThemeMode.SYSTEM
        }
    }

    /**
     * Updates the theme mode.
     */
    fun updateThemeMode(themeMode: ThemeMode) {
        viewModelScope.launch {
            try {
                val domainTheme = when (themeMode) {
                    ThemeMode.LIGHT -> com.maintenance.app.domain.model.ThemeMode.LIGHT
                    ThemeMode.DARK -> com.maintenance.app.domain.model.ThemeMode.DARK
                    ThemeMode.SYSTEM -> com.maintenance.app.domain.model.ThemeMode.SYSTEM
                }
                updateThemeUseCase.invoke(UpdateThemeUseCase.Params(domainTheme))
                _themeState.value = _themeState.value.copy(themeMode = themeMode)
            } catch (exception: Exception) {
                // Handle error
            }
        }
    }

    /**
     * Updates the dynamic colors preference.
     */
    fun updateDynamicColors(useDynamicColors: Boolean) {
        viewModelScope.launch {
            _themeState.value = _themeState.value.copy(useDynamicColors = useDynamicColors)
        }
    }

    /**
     * Updates the font scale.
     */
    fun updateFontScale(fontScale: Float) {
        viewModelScope.launch {
            _themeState.value = _themeState.value.copy(fontScale = fontScale)
        }
    }

    /**
     * Updates the system dark theme detection.
     */
    @Composable
    fun updateSystemDarkTheme() {
        val isSystemInDarkTheme = isSystemInDarkTheme()
        if (_themeState.value.isSystemInDarkTheme != isSystemInDarkTheme) {
            _themeState.value = _themeState.value.copy(isSystemInDarkTheme = isSystemInDarkTheme)
        }
    }

    /**
     * Check if system is in dark mode.
     */
    private fun isSystemDarkMode(): Boolean {
        return false // Will be set by Composable
    }
}
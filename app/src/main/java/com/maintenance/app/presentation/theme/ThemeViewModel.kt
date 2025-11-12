package com.maintenance.app.presentation.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.runtime.Composable
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
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
    // We'll inject the settings repository later
) : ViewModel() {

    private val _themeState = MutableStateFlow(ThemeState())
    val themeState: StateFlow<ThemeState> = _themeState.asStateFlow()

    /**
     * Updates the theme mode.
     */
    fun updateThemeMode(themeMode: ThemeMode) {
        viewModelScope.launch {
            _themeState.value = _themeState.value.copy(themeMode = themeMode)
            // TODO: Save to preferences using settings repository
        }
    }

    /**
     * Updates the dynamic colors preference.
     */
    fun updateDynamicColors(useDynamicColors: Boolean) {
        viewModelScope.launch {
            _themeState.value = _themeState.value.copy(useDynamicColors = useDynamicColors)
            // TODO: Save to preferences using settings repository
        }
    }

    /**
     * Updates the font scale.
     */
    fun updateFontScale(fontScale: Float) {
        viewModelScope.launch {
            _themeState.value = _themeState.value.copy(fontScale = fontScale)
            // TODO: Save to preferences using settings repository
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
}
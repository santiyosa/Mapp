package com.maintenance.app.presentation.theme

/**
 * Enum representing the different theme modes available.
 */
enum class ThemeMode {
    LIGHT,
    DARK,
    SYSTEM
}

/**
 * Data class representing the current theme state.
 */
data class ThemeState(
    val themeMode: ThemeMode = ThemeMode.SYSTEM,
    val useDynamicColors: Boolean = true,
    val fontScale: Float = 1.0f,
    val isSystemInDarkTheme: Boolean = false
)
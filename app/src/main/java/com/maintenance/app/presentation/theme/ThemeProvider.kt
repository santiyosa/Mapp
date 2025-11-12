package com.maintenance.app.presentation.theme

import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.platform.LocalContext
import androidx.hilt.navigation.compose.hiltViewModel

/**
 * Theme provider that handles dynamic theme selection based on user preferences.
 */
@Composable
fun ThemeProvider(
    themeViewModel: ThemeViewModel = hiltViewModel(),
    content: @Composable () -> Unit
) {
    val themeState by themeViewModel.themeState.collectAsState()
    val context = LocalContext.current

    MaintenanceAppTheme(
        darkTheme = when (themeState.themeMode) {
            ThemeMode.LIGHT -> false
            ThemeMode.DARK -> true
            ThemeMode.SYSTEM -> themeState.isSystemInDarkTheme
        },
        dynamicColor = themeState.useDynamicColors,
        fontScale = themeState.fontScale,
        content = content
    )
}
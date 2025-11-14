package com.maintenance.app.presentation.ui.screens.settings

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.Divider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.maintenance.app.presentation.ui.components.MaintenanceAppBar
import com.maintenance.app.presentation.ui.components.SectionHeader
import com.maintenance.app.presentation.viewmodels.settings.SettingsViewModel
import com.maintenance.app.presentation.viewmodels.settings.ThemeMode

/**
 * Settings screen for user preferences and app configuration.
 */
@Composable
fun SettingsScreenComplete(
    navController: NavController,
    viewModel: SettingsViewModel = hiltViewModel()
) {
    val themeMode by viewModel.themeMode.collectAsState()
    val fontSize by viewModel.fontSize.collectAsState()
    val biometricEnabled by viewModel.biometricEnabled.collectAsState()

    Scaffold(
        topBar = {
            MaintenanceAppBar(
                title = "Settings",
                onNavigateBack = { navController.navigateUp() }
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .verticalScroll(rememberScrollState())
        ) {
            // Appearance section
            SectionHeader(title = "Appearance")

            // Theme selector
            ThemeSetting(
                currentTheme = themeMode,
                onThemeChange = viewModel::setThemeMode
            )

            Divider(modifier = Modifier.padding(vertical = 8.dp))

            // Font size selector
            FontSizeSetting(
                currentSize = fontSize,
                onSizeChange = viewModel::setFontSize
            )

            Divider(modifier = Modifier.padding(vertical = 8.dp))

            // Security section
            SectionHeader(title = "Security")

            BiometricSetting(
                enabled = biometricEnabled,
                onToggle = viewModel::setBiometricEnabled
            )

            Divider(modifier = Modifier.padding(vertical = 8.dp))

            // About section
            SectionHeader(title = "About")

            AboutInfo()
        }
    }
}

@Composable
private fun ThemeSetting(
    currentTheme: ThemeMode,
    onThemeChange: (ThemeMode) -> Unit
) {
    Column(modifier = Modifier.fillMaxWidth()) {
        Text(
            text = "Theme",
            style = MaterialTheme.typography.titleSmall,
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
        )

        ThemeOption(
            label = "Light",
            isSelected = currentTheme == ThemeMode.LIGHT,
            onClick = { onThemeChange(ThemeMode.LIGHT) }
        )

        ThemeOption(
            label = "Dark",
            isSelected = currentTheme == ThemeMode.DARK,
            onClick = { onThemeChange(ThemeMode.DARK) }
        )

        ThemeOption(
            label = "System",
            isSelected = currentTheme == ThemeMode.SYSTEM,
            onClick = { onThemeChange(ThemeMode.SYSTEM) }
        )
    }
}

@Composable
private fun ThemeOption(
    label: String,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .selectable(
                selected = isSelected,
                onClick = onClick,
                role = Role.RadioButton
            )
            .padding(horizontal = 16.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        RadioButton(
            selected = isSelected,
            onClick = null
        )
        Spacer(modifier = Modifier.width(16.dp))
        Text(
            text = label,
            style = MaterialTheme.typography.bodyMedium
        )
    }
}

@Composable
private fun FontSizeSetting(
    currentSize: Float,
    onSizeChange: (Float) -> Unit
) {
    Column(modifier = Modifier.fillMaxWidth()) {
        Text(
            text = "Font Size: ${"%.1f".format(currentSize)}x",
            style = MaterialTheme.typography.titleSmall,
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
        )

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            listOf(0.8f, 1.0f, 1.2f).forEach { size ->
                FontSizeOption(
                    size = size,
                    isSelected = currentSize == size,
                    onClick = { onSizeChange(size) },
                    modifier = Modifier.weight(1f)
                )
            }
        }
    }
}

@Composable
private fun FontSizeOption(
    size: Float,
    isSelected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .clickable(onClick = onClick)
            .padding(8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        RadioButton(
            selected = isSelected,
            onClick = null
        )
        Spacer(modifier = Modifier.width(8.dp))
        Text(
            text = when (size) {
                0.8f -> "Small"
                1.0f -> "Normal"
                1.2f -> "Large"
                else -> "${"%.1f".format(size)}x"
            },
            style = MaterialTheme.typography.bodySmall
        )
    }
}

@Composable
private fun BiometricSetting(
    enabled: Boolean,
    onToggle: (Boolean) -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onToggle(!enabled) }
            .padding(horizontal = 16.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = androidx.compose.foundation.layout.Arrangement.SpaceBetween
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = "Biometric Authentication",
                style = MaterialTheme.typography.bodyMedium
            )
            Text(
                text = "Use fingerprint or face to unlock",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
        Switch(
            checked = enabled,
            onCheckedChange = onToggle
        )
    }
}

@Composable
private fun AboutInfo() {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp)
    ) {
        InfoRow(label = "App Name", value = "Maintenance Records")
        Spacer(modifier = Modifier.height(12.dp))
        InfoRow(label = "Version", value = "1.0.0")
        Spacer(modifier = Modifier.height(12.dp))
        InfoRow(label = "Database Version", value = "1")
        Spacer(modifier = Modifier.height(16.dp))
        Text(
            text = "Made with ❤️ for maintaining your records",
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.align(Alignment.CenterHorizontally)
        )
    }
}

@Composable
private fun InfoRow(
    label: String,
    value: String
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = androidx.compose.foundation.layout.Arrangement.SpaceBetween
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Text(
            text = value,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurface
        )
    }
}

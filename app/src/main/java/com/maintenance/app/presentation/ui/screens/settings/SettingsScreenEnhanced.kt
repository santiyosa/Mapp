package com.maintenance.app.presentation.ui.screens.settings

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.maintenance.app.presentation.settings.SettingsViewModel
import com.maintenance.app.presentation.ui.components.BiometricSettingCard
import com.maintenance.app.presentation.ui.components.FontSizeSelector
import com.maintenance.app.presentation.ui.components.LanguageSelector
import com.maintenance.app.presentation.ui.components.NotificationPreference
import com.maintenance.app.presentation.ui.components.PersonalizationDivider
import com.maintenance.app.presentation.ui.components.ThemeSelector

/**
 * Improved Settings Screen with better UI/UX
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreenEnhanced(
    viewModel: SettingsViewModel = hiltViewModel(),
    navController: NavController? = null,
    onNavigateBack: () -> Unit = {}
) {
    val uiState by viewModel.uiState.collectAsState()
    var showResetDialog by remember { mutableStateOf(false) }
    var showClearDataDialog by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Settings & Personalization") },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer,
                    titleContentColor = MaterialTheme.colorScheme.primary
                )
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .verticalScroll(rememberScrollState())
        ) {
            // Error Message
            if (uiState.errorMessage != null) {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(12.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.errorContainer
                    )
                ) {
                    Text(
                        text = uiState.errorMessage!!,
                        color = MaterialTheme.colorScheme.error,
                        style = MaterialTheme.typography.bodySmall,
                        modifier = Modifier.padding(12.dp)
                    )
                }
            }

            // Success Message
            if (uiState.successMessage != null) {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(12.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.primaryContainer
                    )
                ) {
                    Text(
                        text = uiState.successMessage!!,
                        color = MaterialTheme.colorScheme.primary,
                        style = MaterialTheme.typography.bodySmall,
                        modifier = Modifier.padding(12.dp)
                    )
                }
            }

            // Loading Indicator
            if (uiState.isLoading) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    horizontalArrangement = Arrangement.Center
                ) {
                    CircularProgressIndicator()
                }
            }

            // APPEARANCE SECTION
            PersonalizationDivider("Appearance")

            // Theme Selector
            uiState.appSettings?.let { settings ->
                ThemeSelector(
                    selectedTheme = settings.themeMode,
                    onThemeSelected = { viewModel.updateTheme(it) }
                )
            }

            // Language Selector
            uiState.appSettings?.let { settings ->
                LanguageSelector(
                    selectedLanguage = settings.language,
                    onLanguageSelected = { viewModel.updateLanguage(it) }
                )
            }

            // Font Size Selector
            FontSizeSelector(
                currentSize = 1.0f,
                onSizeChanged = { /* TODO: Implement font size persistence */ }
            )

            // SECURITY SECTION
            PersonalizationDivider("Security & Privacy")

            // Notification Preference
            uiState.appSettings?.let { settings ->
                NotificationPreference(
                    isEnabled = settings.enableNotifications,
                    onToggle = { viewModel.updateNotifications(it) }
                )
            }

            // Biometric Setting
            BiometricSettingCard(
                isEnabled = uiState.isBiometricEnabled,
                isAvailable = uiState.isBiometricAvailable,
                onToggle = { enabled ->
                    if (enabled) {
                        viewModel.enableBiometric()
                    } else {
                        viewModel.disableBiometric()
                    }
                }
            )

            // DATA & BACKUP SECTION
            PersonalizationDivider("Data & Backup")

            Button(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp),
                onClick = {
                    navController?.navigate("backup")
                }
            ) {
                Text("Manage Backups")
            }

            // AUTO BACKUP TOGGLE
            uiState.appSettings?.let { settings ->
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "Auto Backup",
                            style = MaterialTheme.typography.bodyMedium
                        )
                        Text(
                            text = "Backup weekly",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.padding(top = 4.dp)
                        )
                    }
                    Switch(
                        checked = settings.enableAutoBackup,
                        onCheckedChange = { /* TODO: Implement auto backup setting */ },
                        modifier = Modifier.padding(start = 16.dp)
                    )
                }
            }

            // DANGER ZONE SECTION
            PersonalizationDivider("Danger Zone")

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                OutlinedButton(
                    modifier = Modifier.weight(1f),
                    onClick = { showResetDialog = true }
                ) {
                    Text("Reset Settings")
                }

                Button(
                    modifier = Modifier.weight(1f),
                    onClick = { showClearDataDialog = true },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.error
                    )
                ) {
                    Text("Clear All Data")
                }
            }

            // Bottom Spacer
            Spacer(modifier = Modifier.height(32.dp))
        }
    }

    // Reset Dialog
    if (showResetDialog) {
        AlertDialog(
            onDismissRequest = { showResetDialog = false },
            icon = { Icon(Icons.Default.ArrowBack, contentDescription = null) },
            title = { Text("Reset Settings?") },
            text = { Text("All settings will be restored to default values. This action cannot be undone.") },
            confirmButton = {
                TextButton(
                    onClick = {
                        viewModel.resetSettings()
                        showResetDialog = false
                    }
                ) {
                    Text("Reset", color = MaterialTheme.colorScheme.error)
                }
            },
            dismissButton = {
                TextButton(onClick = { showResetDialog = false }) {
                    Text("Cancel")
                }
            }
        )
    }

    // Clear Data Dialog
    if (showClearDataDialog) {
        AlertDialog(
            onDismissRequest = { showClearDataDialog = false },
            icon = { Icon(Icons.Default.ArrowBack, contentDescription = null) },
            title = { Text("Delete All Data?") },
            text = { Text("This will permanently delete all records and settings. This action cannot be undone.") },
            confirmButton = {
                TextButton(
                    onClick = {
                        viewModel.clearAllData()
                        showClearDataDialog = false
                    }
                ) {
                    Text("Delete", color = MaterialTheme.colorScheme.error)
                }
            },
            dismissButton = {
                TextButton(onClick = { showClearDataDialog = false }) {
                    Text("Cancel")
                }
            }
        )
    }
}

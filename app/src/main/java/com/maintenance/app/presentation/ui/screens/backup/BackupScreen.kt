package com.maintenance.app.presentation.ui.screens.backup

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.FileDownload
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.maintenance.app.R
import com.maintenance.app.domain.model.BackupFrequency
import com.maintenance.app.domain.model.BackupMetadata
import com.maintenance.app.presentation.viewmodels.backup.BackupViewModel
import java.time.format.DateTimeFormatter

/**
 * Backup screen UI for managing backups and scheduling.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BackupScreen(
    viewModel: BackupViewModel = hiltViewModel(),
    onNavigateBack: () -> Unit = {}
) {
    val uiState by viewModel.uiState.collectAsState()
    var showCreateDialog by remember { mutableStateOf(false) }
    var backupName by remember { mutableStateOf("") }
    var enableEncryption by remember { mutableStateOf(true) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(stringResource(R.string.backup_share_title)) },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = stringResource(R.string.nav_back))
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer,
                    titleContentColor = MaterialTheme.colorScheme.primary
                )
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
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

            // Loading State
            if (uiState.isLoading) {
                Box(
                    modifier = Modifier
                        .fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator()
                }
            } else {
                LazyColumn(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(12.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    // Storage Info Section
                    item {
                        Card {
                            Column(modifier = Modifier.padding(16.dp)) {
                                Text(
                                    "Storage Information",
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Bold
                                )
                                Spacer(modifier = Modifier.height(12.dp))
                                
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Column {
                                        Text(
                                            "Available Storage",
                                            style = MaterialTheme.typography.labelSmall
                                        )
                                        Text(
                                            formatBytes(uiState.availableStorage),
                                            style = MaterialTheme.typography.bodyMedium
                                        )
                                    }
                                    Column {
                                        Text(
                                            "Database Size",
                                            style = MaterialTheme.typography.labelSmall
                                        )
                                        Text(
                                            formatBytes(uiState.databaseSize),
                                            style = MaterialTheme.typography.bodyMedium
                                        )
                                    }
                                }
                            }
                        }
                    }

                    // Create Backup Section
                    item {
                        Card {
                            Column(modifier = Modifier.padding(16.dp)) {
                                Text(
                                    "Create Backup",
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Bold
                                )
                                Spacer(modifier = Modifier.height(12.dp))
                                
                                Button(
                                    modifier = Modifier.fillMaxWidth(),
                                    onClick = { showCreateDialog = true },
                                    enabled = !uiState.isCreatingBackup
                                ) {
                                    if (uiState.isCreatingBackup) {
                                        CircularProgressIndicator(
                                            modifier = Modifier.padding(end = 8.dp),
                                            strokeWidth = 2.dp
                                        )
                                    }
                                    Text(stringResource(R.string.create_backup_now))
                                }
                            }
                        }
                    }

                    // Auto Backup Schedule Section
                    item {
                        Card {
                            Column(modifier = Modifier.padding(16.dp)) {
                                Text(
                                    "Auto Backup Schedule",
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Bold
                                )
                                Spacer(modifier = Modifier.height(12.dp))
                                
                                uiState.backupSchedule?.let { schedule ->
                                    // Enable Auto Backup
                                    Row(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(bottom = 12.dp),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Text(stringResource(R.string.enable_auto_backup))
                                        Switch(
                                            checked = schedule.enabled,
                                            onCheckedChange = { enabled ->
                                                viewModel.updateBackupSchedule(
                                                    enabled = enabled,
                                                    frequency = schedule.frequency,
                                                    wifiOnly = schedule.wifiOnly,
                                                    chargingOnly = schedule.chargingOnly
                                                )
                                            }
                                        )
                                    }

                                    if (schedule.enabled) {
                                        // Frequency Selector
                                        Text(
                                            "Frequency",
                                            style = MaterialTheme.typography.labelSmall,
                                            modifier = Modifier.padding(bottom = 8.dp)
                                        )
                                        Row(
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .padding(bottom = 12.dp),
                                            horizontalArrangement = Arrangement.spacedBy(4.dp)
                                        ) {
                                            BackupFrequency.values().forEach { freq ->
                                                Button(
                                                    modifier = Modifier
                                                        .weight(1f)
                                                        .height(36.dp),
                                                    onClick = {
                                                        viewModel.updateBackupSchedule(
                                                            enabled = schedule.enabled,
                                                            frequency = freq,
                                                            wifiOnly = schedule.wifiOnly,
                                                            chargingOnly = schedule.chargingOnly
                                                        )
                                                    },
                                                    enabled = freq != schedule.frequency,
                                                    colors = if (freq == schedule.frequency) {
                                                        ButtonDefaults.buttonColors()
                                                    } else {
                                                        ButtonDefaults.outlinedButtonColors()
                                                    },
                                                    shape = MaterialTheme.shapes.small
                                                ) {
                                                    Text(
                                                        freq.name,
                                                        style = MaterialTheme.typography.labelSmall
                                                    )
                                                }
                                            }
                                        }

                                        // WiFi Only
                                        Row(
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .padding(bottom = 8.dp),
                                            horizontalArrangement = Arrangement.SpaceBetween,
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            Text(stringResource(R.string.wifi_only))
                                            Switch(
                                                checked = schedule.wifiOnly,
                                                onCheckedChange = { wifiOnly ->
                                                    viewModel.updateBackupSchedule(
                                                        enabled = schedule.enabled,
                                                        frequency = schedule.frequency,
                                                        wifiOnly = wifiOnly,
                                                        chargingOnly = schedule.chargingOnly
                                                    )
                                                }
                                            )
                                        }

                                        // Charging Only
                                        Row(
                                            modifier = Modifier.fillMaxWidth(),
                                            horizontalArrangement = Arrangement.SpaceBetween,
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            Text(stringResource(R.string.charging_only))
                                            Switch(
                                                checked = schedule.chargingOnly,
                                                onCheckedChange = { chargingOnly ->
                                                    viewModel.updateBackupSchedule(
                                                        enabled = schedule.enabled,
                                                        frequency = schedule.frequency,
                                                        wifiOnly = schedule.wifiOnly,
                                                        chargingOnly = chargingOnly
                                                    )
                                                }
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }

                    // Backups List Section
                    item {
                        Text(
                            "Recent Backups",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.padding(bottom = 8.dp)
                        )
                    }

                    if (uiState.backups.isEmpty()) {
                        item {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(32.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    "No backups yet",
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                    } else {
                        items(uiState.backups) { backup ->
                            BackupListItem(
                                backup = backup,
                                onRestore = {
                                    viewModel.restoreBackup(backup.id)
                                },
                                onDelete = {
                                    viewModel.deleteBackup(backup.id)
                                }
                            )
                        }
                    }
                }
            }
        }
    }

    // Create Backup Dialog
    if (showCreateDialog) {
        CreateBackupDialog(
            onConfirm = { name, encrypted ->
                viewModel.createBackup(name, encrypted)
                showCreateDialog = false
            },
            onDismiss = {
                showCreateDialog = false
            }
        )
    }
}

@Composable
private fun BackupListItem(
    backup: BackupMetadata,
    onRestore: () -> Unit,
    onDelete: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(
                    modifier = Modifier.weight(1f)
                ) {
                    Text(
                        text = backup.name,
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = formatBackupDate(backup.createdDate),
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "Size: ${formatBytes(backup.size)}",
                        style = MaterialTheme.typography.labelSmall
                    )
                }
                Row {
                    IconButton(onClick = onRestore) {
                        Icon(
                            Icons.Default.FileDownload,
                            contentDescription = stringResource(R.string.restore_backup),
                            tint = MaterialTheme.colorScheme.primary
                        )
                    }
                    IconButton(onClick = onDelete) {
                        Icon(
                            Icons.Default.Delete,
                            contentDescription = stringResource(R.string.delete_backup),
                            tint = MaterialTheme.colorScheme.error
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun CreateBackupDialog(
    onConfirm: (name: String, encrypted: Boolean) -> Unit,
    onDismiss: () -> Unit
) {
    var backupName by remember { mutableStateOf("") }
    var enableEncryption by remember { mutableStateOf(true) }

    androidx.compose.material3.AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(stringResource(R.string.create_backup_title)) },
        text = {
            Column {
                Text(
                    "Backup Name",
                    style = MaterialTheme.typography.labelSmall
                )
                androidx.compose.material3.TextField(
                    value = backupName,
                    onValueChange = { backupName = it },
                    placeholder = { Text(stringResource(R.string.backup_placeholder)) },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 8.dp)
                )

                Spacer(modifier = Modifier.height(16.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(stringResource(R.string.enable_encryption))
                    Switch(
                        checked = enableEncryption,
                        onCheckedChange = { enableEncryption = it }
                    )
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    if (backupName.isNotBlank()) {
                        onConfirm(backupName, enableEncryption)
                    }
                }
            ) {
                Text(stringResource(R.string.save))
            }
        },
        dismissButton = {
            Button(onClick = onDismiss) {
                Text(stringResource(R.string.cancel))
            }
        }
    )
}

private fun formatBytes(bytes: Long): String {
    return when {
        bytes < 1024 -> "$bytes B"
        bytes < 1024 * 1024 -> "${bytes / 1024} KB"
        bytes < 1024 * 1024 * 1024 -> "${bytes / (1024 * 1024)} MB"
        else -> "${bytes / (1024 * 1024 * 1024)} GB"
    }
}

private fun formatBackupDate(localDateTime: java.time.LocalDateTime): String {
    return localDateTime.format(DateTimeFormatter.ofPattern("dd MMM yyyy, HH:mm"))
}

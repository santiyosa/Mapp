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
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.FileDownload
import androidx.compose.material3.Button
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
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.maintenance.app.R
import com.maintenance.app.domain.model.BackupFrequency
import com.maintenance.app.domain.model.BackupMetadata
import java.time.format.DateTimeFormatter

/**
 * Backup screen UI for managing backups and scheduling.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BackupScreen() {
    var isLoading by remember { mutableStateOf(false) }
    var backupName by remember { mutableStateOf("") }
    var enableEncryption by remember { mutableStateOf(true) }
    var enableAutoBackup by remember { mutableStateOf(false) }
    var selectedFrequency by remember { mutableStateOf(BackupFrequency.WEEKLY) }
    var wifiOnly by remember { mutableStateOf(true) }
    var backupList by remember { mutableStateOf(emptyList<BackupMetadata>()) }
    var isGoogleDriveConnected by remember { mutableStateOf(false) }
    var availableStorage by remember { mutableStateOf(0L) }
    var databaseSize by remember { mutableStateOf(0L) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Backup & Restore") }
            )
        }
    ) { paddingValues ->
        if (!isGoogleDriveConnected) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues),
                contentAlignment = Alignment.Center
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center,
                    modifier = Modifier.padding(16.dp)
                ) {
                    Text(
                        "Google Drive Not Connected",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    Text(
                        "Please connect your Google Drive account to enable backup features.",
                        style = MaterialTheme.typography.bodyMedium,
                        modifier = Modifier.padding(horizontal = 16.dp)
                    )
                    Spacer(modifier = Modifier.height(24.dp))
                    Button(onClick = { /* TODO: Implement Google Drive sign-in */ }) {
                        Text("Connect Google Drive")
                    }
                }
            }
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // Storage Info Section
                item {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.surfaceVariant
                        )
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Text(
                                "Storage Information",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            StorageInfoRow("Database Size:", formatBytes(databaseSize))
                            StorageInfoRow("Available Storage:", formatBytes(availableStorage))
                            Spacer(modifier = Modifier.height(8.dp))
                            val usagePercent = if (availableStorage > 0) {
                                ((availableStorage - databaseSize) * 100 / availableStorage)
                            } else {
                                0
                            }
                            Text(
                                "Usage: $usagePercent% available",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }

                // Create Backup Section
                item {
                    Card(modifier = Modifier.fillMaxWidth()) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Text(
                                "Create New Backup",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold
                            )
                            Spacer(modifier = Modifier.height(12.dp))

                            androidx.compose.material3.TextField(
                                value = backupName,
                                onValueChange = { backupName = it },
                                label = { Text("Backup Name") },
                                modifier = Modifier.fillMaxWidth(),
                                singleLine = true
                            )
                            Spacer(modifier = Modifier.height(12.dp))

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text("Enable Encryption")
                                Switch(
                                    checked = enableEncryption,
                                    onCheckedChange = { enableEncryption = it }
                                )
                            }
                            Spacer(modifier = Modifier.height(12.dp))

                            Button(
                                onClick = { isLoading = true /* TODO: Implement create backup */ },
                                modifier = Modifier.fillMaxWidth(),
                                enabled = backupName.isNotEmpty() && !isLoading
                            ) {
                                if (isLoading) {
                                    CircularProgressIndicator(
                                        modifier = Modifier.padding(end = 8.dp),
                                        strokeWidth = 2.dp
                                    )
                                }
                                Text("Create Backup")
                            }
                        }
                    }
                }

                // Auto Backup Schedule Section
                item {
                    Card(modifier = Modifier.fillMaxWidth()) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Text(
                                "Auto Backup Schedule",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold
                            )
                            Spacer(modifier = Modifier.height(12.dp))

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text("Enable Auto Backup")
                                Switch(
                                    checked = enableAutoBackup,
                                    onCheckedChange = { enableAutoBackup = it }
                                )
                            }
                            Spacer(modifier = Modifier.height(12.dp))

                            if (enableAutoBackup) {
                                FrequencySelector(
                                    selected = selectedFrequency,
                                    onSelect = { selectedFrequency = it }
                                )
                                Spacer(modifier = Modifier.height(12.dp))

                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text("Wi-Fi Only")
                                    Switch(
                                        checked = wifiOnly,
                                        onCheckedChange = { wifiOnly = it }
                                    )
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
                        fontWeight = FontWeight.Bold
                    )
                }

                if (backupList.isEmpty()) {
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
                    items(backupList) { backup ->
                        BackupListItem(
                            backup = backup,
                            onRestore = { /* TODO: Implement restore */ },
                            onDelete = { /* TODO: Implement delete */ }
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun StorageInfoRow(label: String, value: String) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(label, style = MaterialTheme.typography.bodySmall)
        Text(value, style = MaterialTheme.typography.bodySmall, fontWeight = FontWeight.SemiBold)
    }
}

@Composable
private fun FrequencySelector(
    selected: BackupFrequency,
    onSelect: (BackupFrequency) -> Unit
) {
    Column {
        Text(
            "Backup Frequency",
            style = MaterialTheme.typography.labelMedium,
            fontWeight = FontWeight.SemiBold
        )
        Spacer(modifier = Modifier.height(8.dp))
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            BackupFrequency.values().forEach { freq ->
                Button(
                    onClick = { onSelect(freq) },
                    modifier = Modifier
                        .weight(1f)
                        .height(36.dp),
                    enabled = freq != selected,
                    shape = MaterialTheme.shapes.small
                ) {
                    Text(
                        freq.name,
                        style = MaterialTheme.typography.labelSmall
                    )
                }
            }
        }
    }
}

@Composable
private fun BackupListItem(
    backup: BackupMetadata,
    onRestore: () -> Unit,
    onDelete: () -> Unit
) {
    Card(modifier = Modifier.fillMaxWidth()) {
        Column(modifier = Modifier.padding(12.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Top
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        backup.name,
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        formatDate(backup.createdDate),
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        "Size: ${formatBytes(backup.size)}",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                Row {
                    IconButton(onClick = onRestore) {
                        Icon(
                            Icons.Default.FileDownload,
                            contentDescription = "Restore backup"
                        )
                    }
                    IconButton(onClick = onDelete) {
                        Icon(
                            Icons.Default.Delete,
                            contentDescription = "Delete backup",
                            tint = MaterialTheme.colorScheme.error
                        )
                    }
                }
            }
        }
    }
}

private fun formatBytes(bytes: Long): String {
    if (bytes <= 0) return "0 B"
    val k = 1024
    val sizes = arrayOf("B", "KB", "MB", "GB")
    val i = (Math.log(bytes.toDouble()) / Math.log(k.toDouble())).toInt()
    return String.format("%.2f %s", bytes / Math.pow(k.toDouble(), i.toDouble()), sizes[i])
}

private fun formatDate(date: java.time.LocalDateTime): String {
    return date.format(DateTimeFormatter.ofPattern("MMM dd, yyyy HH:mm"))
}

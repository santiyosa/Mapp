package com.maintenance.app.presentation.ui.components

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.maintenance.app.domain.model.Maintenance
import java.time.format.DateTimeFormatter

/**
 * Dialog for selecting multiple maintenances for sharing.
 */
@Composable
fun MaintenanceSelectionDialog(
    maintenances: List<Maintenance>,
    onConfirm: (List<Maintenance>) -> Unit,
    onDismiss: () -> Unit
) {
    val selectedMaintenances = remember { mutableStateMapOf<Long, Boolean>() }
    
    // Initialize selections
    LaunchedEffect(maintenances) {
        maintenances.forEach { maintenance ->
            selectedMaintenances[maintenance.id] = false
        }
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Seleccionar Mantenimientos") },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(max = 400.dp)
            ) {
                if (maintenances.isEmpty()) {
                    Text(
                        text = "No hay mantenimientos disponibles",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp)
                    )
                } else {
                    LazyColumn(
                        modifier = Modifier.fillMaxWidth(),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        items(maintenances) { maintenance ->
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(horizontal = 8.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Checkbox(
                                    checked = selectedMaintenances[maintenance.id] ?: false,
                                    onCheckedChange = { isChecked ->
                                        selectedMaintenances[maintenance.id] = isChecked
                                    }
                                )
                                Column(
                                    modifier = Modifier
                                        .weight(1f)
                                        .padding(start = 8.dp)
                                ) {
                                    Text(
                                        text = maintenance.type,
                                        style = MaterialTheme.typography.titleSmall
                                    )
                                    Text(
                                        text = maintenance.maintenanceDate.format(
                                            DateTimeFormatter.ofPattern("dd/MM/yyyy")
                                        ),
                                        style = MaterialTheme.typography.labelSmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                    if (!maintenance.description.isNullOrBlank()) {
                                        Text(
                                            text = maintenance.description,
                                            style = MaterialTheme.typography.bodySmall,
                                            maxLines = 1
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    val selected = maintenances.filter { 
                        selectedMaintenances[it.id] ?: false 
                    }
                    onConfirm(selected)
                }
            ) {
                Text("Compartir")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancelar")
            }
        }
    )
}

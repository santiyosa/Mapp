package com.maintenance.app.presentation.ui.screens.trash

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Restore
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.maintenance.app.R
import com.maintenance.app.domain.model.Record
import com.maintenance.app.presentation.ui.components.MainScaffold
import com.maintenance.app.presentation.viewmodels.trash.TrashViewModel

/**
 * Trash screen for viewing and managing deleted records.
 */
@Composable
fun TrashScreen(
    navController: NavController,
    viewModel: TrashViewModel = hiltViewModel()
) {
    val deletedRecords by viewModel.deletedRecords.collectAsState()
    var showConfirmDialog by remember { mutableStateOf<Long?>(null) }
    var confirmAction by remember { mutableStateOf<ConfirmAction?>(null) }

    if (showConfirmDialog != null && confirmAction != null) {
        AlertDialog(
            onDismissRequest = { showConfirmDialog = null },
            title = { 
                Text(
                    if (confirmAction == ConfirmAction.RESTORE) 
                        stringResource(R.string.restore_record) 
                    else 
                        stringResource(R.string.delete)
                )
            },
            text = { 
                Text(
                    if (confirmAction == ConfirmAction.RESTORE)
                        "¿Restaurar este registro?"
                    else
                        "¿Eliminar permanentemente? Esta acción no se puede deshacer."
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        showConfirmDialog?.let { recordId ->
                            when (confirmAction) {
                                ConfirmAction.RESTORE -> viewModel.restoreRecord(recordId)
                                ConfirmAction.DELETE -> viewModel.permanentlyDeleteRecord(recordId)
                                null -> {}
                            }
                        }
                        showConfirmDialog = null
                        confirmAction = null
                    }
                ) {
                    Text(stringResource(R.string.confirm_action))
                }
            },
            dismissButton = {
                TextButton(
                    onClick = { 
                        showConfirmDialog = null
                        confirmAction = null
                    }
                ) {
                    Text(stringResource(R.string.cancel))
                }
            }
        )
    }

    MainScaffold(
        title = "Papelera",
        navController = navController,
        showBottomBar = false,
        showBackButton = true,
        onBackClick = { navController.navigateUp() }
    ) { paddingValues ->
        when {
            deletedRecords.isEmpty() -> {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(paddingValues),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "No hay registros eliminados",
                        style = MaterialTheme.typography.bodyLarge,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
            else -> {
                LazyColumn(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(paddingValues),
                    contentPadding = PaddingValues(8.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(deletedRecords) { record ->
                        TrashRecordCard(
                            record = record,
                            onRestore = {
                                showConfirmDialog = record.id
                                confirmAction = ConfirmAction.RESTORE
                            },
                            onPermanentlyDelete = {
                                showConfirmDialog = record.id
                                confirmAction = ConfirmAction.DELETE
                            }
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun TrashRecordCard(
    record: Record,
    onRestore: () -> Unit,
    onPermanentlyDelete: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 8.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant
        )
    ) {
        Column(
            modifier = Modifier.padding(12.dp)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(
                    modifier = Modifier.weight(1f)
                ) {
                    Text(
                        text = record.name,
                        style = MaterialTheme.typography.titleMedium,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    
                    if (!record.description.isNullOrBlank()) {
                        Text(
                            text = record.description,
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                            modifier = Modifier.padding(top = 4.dp)
                        )
                    }
                }
            }

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 8.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Button(
                    onClick = onRestore,
                    modifier = Modifier.weight(1f)
                ) {
                    Icon(
                        imageVector = Icons.Default.Restore,
                        contentDescription = null,
                        modifier = Modifier
                            .size(18.dp)
                            .padding(end = 4.dp)
                    )
                    Text(stringResource(R.string.restore_record))
                }

                OutlinedButton(
                    onClick = onPermanentlyDelete,
                    modifier = Modifier.weight(1f),
                    colors = ButtonDefaults.outlinedButtonColors(
                        contentColor = MaterialTheme.colorScheme.error
                    )
                ) {
                    Icon(
                        imageVector = Icons.Default.Delete,
                        contentDescription = null,
                        modifier = Modifier
                            .size(18.dp)
                            .padding(end = 4.dp)
                    )
                    Text("Borrar")
                }
            }
        }
    }
}

enum class ConfirmAction {
    RESTORE,
    DELETE
}

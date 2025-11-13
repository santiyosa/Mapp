package com.maintenance.app.presentation.ui.screens.maintenance.edit

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.maintenance.app.domain.model.Maintenance
import com.maintenance.app.presentation.ui.components.MainScaffold
import com.maintenance.app.presentation.ui.components.images.ImagePicker
import com.maintenance.app.presentation.viewmodels.maintenance.edit.EditMaintenanceUiState
import com.maintenance.app.presentation.viewmodels.maintenance.edit.EditMaintenanceViewModel
import com.maintenance.app.utils.ImageManager

/**
 * Screen for editing an existing maintenance record.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EditMaintenanceScreen(
    navController: NavController,
    @Suppress("UNUSED_PARAMETER") maintenanceId: Long,
    viewModel: EditMaintenanceViewModel = hiltViewModel()
) {
    val context = LocalContext.current
    val imageManager = remember { ImageManager(context) }
    val uiState by viewModel.uiState.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(uiState) {
        val currentState = uiState
        when (currentState) {
            is EditMaintenanceUiState.Success -> {
                navController.navigateUp()
            }
            is EditMaintenanceUiState.Error -> {
                snackbarHostState.showSnackbar(
                    message = currentState.message,
                    duration = SnackbarDuration.Long
                )
            }
            else -> { /* No action needed */ }
        }
    }

    MainScaffold(
        title = "Editar Mantenimiento",
        navController = navController,
        showBottomBar = false,
        showBackButton = true,
        onBackClick = { navController.navigateUp() },
        actions = {
            IconButton(
                onClick = { viewModel.updateMaintenance() },
                enabled = uiState !is EditMaintenanceUiState.Loading
            ) {
                if (uiState is EditMaintenanceUiState.Loading) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(24.dp),
                        strokeWidth = 2.dp
                    )
                } else {
                    Icon(
                        imageVector = Icons.Default.Check,
                        contentDescription = "Guardar cambios"
                    )
                }
            }
        },
        snackbarHost = {
            SnackbarHost(hostState = snackbarHostState)
        }
    ) { paddingValues ->
        when (uiState) {
            is EditMaintenanceUiState.LoadingMaintenance -> {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(paddingValues),
                    contentAlignment = Alignment.Center
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        CircularProgressIndicator()
                        Text("Cargando mantenimiento...")
                    }
                }
            }
            else -> {
                EditMaintenanceContent(
                    viewModel = viewModel,
                    imageManager = imageManager,
                    modifier = Modifier.padding(paddingValues)
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun EditMaintenanceContent(
    viewModel: EditMaintenanceViewModel,
    imageManager: ImageManager,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Description field (required)
        OutlinedTextField(
            value = viewModel.description,
            onValueChange = viewModel::updateDescription,
            label = { Text("Descripción *") },
            placeholder = { Text("Describe el mantenimiento realizado") },
            isError = viewModel.descriptionError != null,
            supportingText = {
                viewModel.descriptionError?.let { error ->
                    Text(error, color = MaterialTheme.colorScheme.error)
                }
            },
            modifier = Modifier.fillMaxWidth(),
            maxLines = 3
        )

        // Type field (required)
        OutlinedTextField(
            value = viewModel.type,
            onValueChange = viewModel::updateType,
            label = { Text("Tipo de Mantenimiento *") },
            placeholder = { Text("Ej: Preventivo, Correctivo, Urgente") },
            isError = viewModel.typeError != null,
            supportingText = {
                viewModel.typeError?.let { error ->
                    Text(error, color = MaterialTheme.colorScheme.error)
                }
            },
            modifier = Modifier.fillMaxWidth()
        )

        // Cost and Currency Row
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            OutlinedTextField(
                value = viewModel.cost,
                onValueChange = viewModel::updateCost,
                label = { Text("Costo") },
                placeholder = { Text("0.00") },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                isError = viewModel.costError != null,
                supportingText = {
                    viewModel.costError?.let { error ->
                        Text(error, color = MaterialTheme.colorScheme.error)
                    }
                },
                modifier = Modifier.weight(2f)
            )

            OutlinedTextField(
                value = viewModel.currency,
                onValueChange = viewModel::updateCurrency,
                label = { Text("Moneda") },
                placeholder = { Text("USD") },
                modifier = Modifier.weight(1f)
            )
        }

        // Performed by field
        OutlinedTextField(
            value = viewModel.performedBy,
            onValueChange = viewModel::updatePerformedBy,
            label = { Text("Realizado por") },
            placeholder = { Text("Nombre del técnico o empresa") },
            modifier = Modifier.fillMaxWidth()
        )

        // Location field
        OutlinedTextField(
            value = viewModel.location,
            onValueChange = viewModel::updateLocation,
            label = { Text("Ubicación") },
            placeholder = { Text("Donde se realizó el mantenimiento") },
            modifier = Modifier.fillMaxWidth()
        )

        // Duration field
        OutlinedTextField(
            value = viewModel.durationMinutes,
            onValueChange = viewModel::updateDuration,
            label = { Text("Duración (minutos)") },
            placeholder = { Text("60") },
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
            modifier = Modifier.fillMaxWidth()
        )

        // Priority selection
        Card(
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(
                modifier = Modifier.padding(16.dp)
            ) {
                Text(
                    text = "Prioridad",
                    style = MaterialTheme.typography.titleSmall,
                    modifier = Modifier.padding(bottom = 8.dp)
                )
                
                Maintenance.Priority.values().forEach { priority ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .selectable(
                                selected = viewModel.priority == priority,
                                onClick = { viewModel.updatePriority(priority) }
                            )
                            .padding(vertical = 4.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        RadioButton(
                            selected = viewModel.priority == priority,
                            onClick = { viewModel.updatePriority(priority) }
                        )
                        Text(
                            text = priority.displayName,
                            modifier = Modifier.padding(start = 8.dp)
                        )
                    }
                }
            }
        }

        // Parts replaced field
        OutlinedTextField(
            value = viewModel.partsReplaced,
            onValueChange = viewModel::updatePartsReplaced,
            label = { Text("Piezas reemplazadas") },
            placeholder = { Text("Lista de componentes cambiados") },
            modifier = Modifier.fillMaxWidth(),
            maxLines = 2
        )

        // Recurring maintenance
        Card(
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(
                modifier = Modifier.padding(16.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Switch(
                        checked = viewModel.isRecurring,
                        onCheckedChange = viewModel::updateRecurring
                    )
                    Text(
                        text = "Mantenimiento recurrente",
                        modifier = Modifier.padding(start = 8.dp),
                        style = MaterialTheme.typography.titleSmall
                    )
                }
                
                if (viewModel.isRecurring) {
                    Spacer(modifier = Modifier.height(8.dp))
                    OutlinedTextField(
                        value = viewModel.recurrenceIntervalDays,
                        onValueChange = viewModel::updateRecurrenceInterval,
                        label = { Text("Intervalo (días)") },
                        placeholder = { Text("30") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            }
        }

        // Image picker
        ImagePicker(
            selectedImages = viewModel.selectedImages,
            imageManager = imageManager,
            onCameraCapture = { viewModel.prepareCameraCapture() },
            onCameraResult = viewModel::handleCameraResult,
            onGalleryResult = viewModel::handleGalleryResult,
            onRemoveImage = viewModel::removeImage
        )

        // Notes field
        OutlinedTextField(
            value = viewModel.notes,
            onValueChange = viewModel::updateNotes,
            label = { Text("Notas adicionales") },
            placeholder = { Text("Observaciones, recomendaciones, etc.") },
            modifier = Modifier.fillMaxWidth(),
            maxLines = 4
        )

        // Add some bottom padding
        Spacer(modifier = Modifier.height(16.dp))
    }
}
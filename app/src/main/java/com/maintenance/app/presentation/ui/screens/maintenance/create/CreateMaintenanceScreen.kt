package com.maintenance.app.presentation.ui.screens.maintenance.create

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
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.maintenance.app.domain.model.Maintenance
import com.maintenance.app.presentation.ui.components.MainScaffold
import com.maintenance.app.presentation.ui.components.ValidatedTextField
import com.maintenance.app.presentation.ui.components.CurrencyTextField
import com.maintenance.app.presentation.ui.components.DurationTextField
import com.maintenance.app.presentation.ui.components.MultiLineTextField
import com.maintenance.app.presentation.ui.components.images.ImagePicker
import com.maintenance.app.presentation.viewmodels.maintenance.create.CreateMaintenanceUiState
import com.maintenance.app.presentation.viewmodels.maintenance.create.CreateMaintenanceViewModel
import com.maintenance.app.utils.ImageManager
import javax.inject.Inject

/**
 * Screen for creating a new maintenance record.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CreateMaintenanceScreen(
    navController: NavController,
    recordId: Long,
    viewModel: CreateMaintenanceViewModel = hiltViewModel()
) {
    val context = LocalContext.current
    val imageManager = remember { ImageManager(context) }
    val uiState by viewModel.uiState.collectAsState()
    // TODO: Add validation states back when FormValidationUseCase is ready
    val snackbarHostState = remember { SnackbarHostState() }
    
    // Initialize ViewModel with record ID and load any existing draft
    LaunchedEffect(recordId) {
        viewModel.initializeWithRecord(recordId)
    }

    LaunchedEffect(uiState) {
        val currentState = uiState
        when (currentState) {
            is CreateMaintenanceUiState.Success -> {
                navController.navigateUp()
            }
            is CreateMaintenanceUiState.Error -> {
                snackbarHostState.showSnackbar(
                    message = currentState.message,
                    duration = SnackbarDuration.Long
                )
            }
            else -> { /* No action needed */ }
        }
    }

    MainScaffold(
        title = "Nuevo Mantenimiento",
        navController = navController,
        showBottomBar = false,
        showBackButton = true,
        onBackClick = { 
            viewModel.saveDraft() // Save draft before leaving
            navController.navigateUp() 
        },
        actions = {
            IconButton(
                onClick = { viewModel.createMaintenance(recordId) },
                enabled = uiState !is CreateMaintenanceUiState.Loading // TODO: Add validation check
            ) {
                if (uiState is CreateMaintenanceUiState.Loading) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(24.dp),
                        strokeWidth = 2.dp
                    )
                } else {
                    Icon(
                        imageVector = Icons.Default.Check,
                        contentDescription = "Guardar",
                        tint = MaterialTheme.colorScheme.primary // TODO: Add validation color
                    )
                }
            }
        },
        snackbarHost = {
            SnackbarHost(hostState = snackbarHostState)
        }
    ) { paddingValues ->
        CreateMaintenanceContent(
            viewModel = viewModel,
            imageManager = imageManager,
            modifier = Modifier.padding(paddingValues)
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun CreateMaintenanceContent(
    viewModel: CreateMaintenanceViewModel,
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
        MultiLineTextField(
            value = viewModel.description,
            onValueChange = viewModel::updateDescription,
            label = "Descripción *",
            placeholder = "Describe el mantenimiento realizado",
            errorMessage = null, // TODO: Add validation
            maxLength = 500,
            minLines = 2,
            maxLines = 4
        )

        // Type field (required)
        ValidatedTextField(
            value = viewModel.type,
            onValueChange = viewModel::updateType,
            label = "Tipo de Mantenimiento *",
            placeholder = "Ej: Preventivo, Correctivo, Urgente",
            errorMessage = null // TODO: Add validation
        )

        // Cost and Currency Row
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            CurrencyTextField(
                value = viewModel.cost,
                onValueChange = viewModel::updateCost,
                currency = viewModel.currency,
                errorMessage = null, // TODO: Add validation
                modifier = Modifier.weight(2f)
            )

            ValidatedTextField(
                value = viewModel.currency,
                onValueChange = viewModel::updateCurrency,
                label = "Moneda",
                placeholder = "USD",
                errorMessage = null, // TODO: Add validation
                modifier = Modifier.weight(1f)
            )
        }

        // Performed by field
        ValidatedTextField(
            value = viewModel.performedBy,
            onValueChange = viewModel::updatePerformedBy,
            label = "Realizado por",
            placeholder = "Nombre del técnico o empresa",
            errorMessage = null // TODO: Add validation
        )

        // Location field
        ValidatedTextField(
            value = viewModel.location,
            onValueChange = viewModel::updateLocation,
            label = "Ubicación",
            placeholder = "Lugar donde se realizó el mantenimiento",
            errorMessage = null // TODO: Add validation
        )

        // Duration field
        DurationTextField(
            value = viewModel.durationMinutes,
            onValueChange = viewModel::updateDuration,
            errorMessage = null // TODO: Add validation
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
        MultiLineTextField(
            value = viewModel.partsReplaced,
            onValueChange = viewModel::updatePartsReplaced,
            label = "Piezas reemplazadas",
            placeholder = "Lista de componentes cambiados",
            errorMessage = null, // TODO: Add validation
            maxLength = 500,
            minLines = 2,
            maxLines = 3
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
        MultiLineTextField(
            value = viewModel.notes,
            onValueChange = viewModel::updateNotes,
            label = "Notas adicionales",
            placeholder = "Observaciones, recomendaciones, etc.",
            errorMessage = null, // TODO: Add validation
            maxLength = 1000,
            minLines = 3,
            maxLines = 5
        )

        // Add some bottom padding
        Spacer(modifier = Modifier.height(16.dp))
    }
}
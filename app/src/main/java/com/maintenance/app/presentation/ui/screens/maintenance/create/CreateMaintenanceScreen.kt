package com.maintenance.app.presentation.ui.screens.maintenance.create

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.compose.ui.res.stringResource
import androidx.navigation.NavController
import com.maintenance.app.R
import com.maintenance.app.domain.model.Maintenance
import com.maintenance.app.presentation.ui.components.LoadingIndicator
import com.maintenance.app.presentation.ui.components.MaintenanceButton
import com.maintenance.app.presentation.ui.components.MaintenanceTextField
import com.maintenance.app.presentation.viewmodels.maintenance.create.CreateMaintenanceViewModel

import com.maintenance.app.presentation.viewmodels.maintenance.create.CreateMaintenanceUiState

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
    val uiState by viewModel.uiState.collectAsState()

    LaunchedEffect(recordId) {
        viewModel.loadDraft(recordId)
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(stringResource(R.string.create_maintenance)) },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.Default.ArrowBack, contentDescription = stringResource(R.string.nav_back))
                    }
                },
                actions = {
                    IconButton(
                        onClick = { viewModel.createMaintenance(recordId) },
                        enabled = uiState !is CreateMaintenanceUiState.Loading
                    ) {
                        Icon(Icons.Default.Check, contentDescription = stringResource(R.string.save))
                    }
                }
            )
        }
    ) { paddingValues ->
        when (uiState) {
            is CreateMaintenanceUiState.Loading -> {
                LoadingIndicator()
            }

            is CreateMaintenanceUiState.Success -> {
                LaunchedEffect(Unit) {
                    navController.popBackStack()
                }
            }

            is CreateMaintenanceUiState.Error -> {
                Text(
                    text = "Error: ${(uiState as CreateMaintenanceUiState.Error).message}",
                    color = MaterialTheme.colorScheme.error,
                    modifier = Modifier.padding(16.dp)
                )
            }

            else -> {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .verticalScroll(rememberScrollState())
                        .padding(paddingValues)
                        .padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    // Type field
                    MaintenanceTextField(
                        value = viewModel.type,
                        onValueChange = viewModel::updateType,
                        label = stringResource(R.string.maintenance_type_label),
                        placeholder = stringResource(R.string.maintenance_type_placeholder),
                        isError = false,
                        keyboardType = KeyboardType.Text,
                        modifier = Modifier.fillMaxWidth()
                    )

                    // Description field
                    MaintenanceTextField(
                        value = viewModel.description,
                        onValueChange = viewModel::updateDescription,
                        label = stringResource(R.string.maintenance_description_label),
                        placeholder = stringResource(R.string.maintenance_description_placeholder),
                        isError = false,
                        keyboardType = KeyboardType.Text,
                        maxLines = 3,
                        modifier = Modifier.fillMaxWidth()
                    )

                    // Maintenance date field
                    OutlinedTextField(
                        value = viewModel.maintenanceDate?.toString() ?: "",
                        onValueChange = { },
                        label = { Text(stringResource(R.string.maintenance_date_label)) },
                        readOnly = true,
                        enabled = false,
                        modifier = Modifier.fillMaxWidth()
                    )

                    // Cost field
                    MaintenanceTextField(
                        value = viewModel.cost,
                        onValueChange = viewModel::updateCost,
                        label = stringResource(R.string.maintenance_cost_label),
                        placeholder = stringResource(R.string.maintenance_cost_placeholder),
                        isError = false,
                        keyboardType = KeyboardType.Decimal,
                        modifier = Modifier.fillMaxWidth()
                    )

                    // Performed by field
                    MaintenanceTextField(
                        value = viewModel.performedBy,
                        onValueChange = viewModel::updatePerformedBy,
                        label = stringResource(R.string.maintenance_performed_by_label),
                        placeholder = stringResource(R.string.maintenance_performed_by_placeholder),
                        isError = false,
                        keyboardType = KeyboardType.Text,
                        modifier = Modifier.fillMaxWidth()
                    )

                    // Location field
                    MaintenanceTextField(
                        value = viewModel.location,
                        onValueChange = viewModel::updateLocation,
                        label = stringResource(R.string.maintenance_location_label),
                        placeholder = stringResource(R.string.maintenance_location_placeholder),
                        isError = false,
                        keyboardType = KeyboardType.Text,
                        modifier = Modifier.fillMaxWidth()
                    )

                    // Parts replaced field
                    MaintenanceTextField(
                        value = viewModel.partsReplaced,
                        onValueChange = viewModel::updatePartsReplaced,
                        label = stringResource(R.string.maintenance_parts_replaced_label),
                        placeholder = stringResource(R.string.maintenance_parts_replaced_placeholder),
                        isError = false,
                        keyboardType = KeyboardType.Text,
                        maxLines = 2,
                        modifier = Modifier.fillMaxWidth()
                    )

                    // Notes field
                    MaintenanceTextField(
                        value = viewModel.notes,
                        onValueChange = viewModel::updateNotes,
                        label = stringResource(R.string.maintenance_notes_label),
                        placeholder = stringResource(R.string.maintenance_notes_placeholder),
                        isError = false,
                        keyboardType = KeyboardType.Text,
                        maxLines = 3,
                        modifier = Modifier.fillMaxWidth()
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    // Save button
                    MaintenanceButton(
                        text = if (uiState is CreateMaintenanceUiState.Loading) "Guardando..." else "Guardar Mantenimiento",
                        onClick = { viewModel.createMaintenance(recordId) },
                        enabled = uiState !is CreateMaintenanceUiState.Loading && 
                                viewModel.description.isNotBlank() &&
                                viewModel.type.isNotBlank(),
                        isLoading = uiState is CreateMaintenanceUiState.Loading,
                        modifier = Modifier.fillMaxWidth()
                    )

                    Spacer(modifier = Modifier.height(32.dp))
                }
            }
        }
    }
}

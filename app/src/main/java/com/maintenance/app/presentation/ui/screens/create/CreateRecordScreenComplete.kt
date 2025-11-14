package com.maintenance.app.presentation.ui.screens.create

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.maintenance.app.presentation.ui.components.MaintenanceAppBar
import com.maintenance.app.presentation.ui.components.MaintenanceButton
import com.maintenance.app.presentation.ui.components.MaintenanceTextField
import com.maintenance.app.presentation.viewmodels.create.CreateRecordUiState
import com.maintenance.app.presentation.viewmodels.create.CreateRecordViewModel

/**
 * Complete screen for creating a new maintenance record.
 */
@Composable
fun CreateRecordScreenComplete(
    navController: NavController,
    viewModel: CreateRecordViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(uiState) {
        when (val currentState = uiState) {
            is CreateRecordUiState.Success -> {
                navController.navigateUp()
            }

            is CreateRecordUiState.Error -> {
                snackbarHostState.showSnackbar(
                    message = currentState.message,
                    duration = androidx.compose.material3.SnackbarDuration.Long
                )
            }

            else -> {}
        }
    }

    Scaffold(
        topBar = {
            MaintenanceAppBar(
                title = "Create Record",
                onNavigateBack = { navController.navigateUp() },
                actions = {
                    IconButton(
                        onClick = { viewModel.saveRecord() },
                        enabled = uiState !is CreateRecordUiState.Loading
                    ) {
                        Icon(
                            imageVector = Icons.Default.Check,
                            contentDescription = "Save",
                            tint = MaterialTheme.colorScheme.primary
                        )
                    }
                }
            )
        },
        snackbarHost = {
            SnackbarHost(hostState = snackbarHostState)
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .verticalScroll(rememberScrollState())
        ) {
            when (uiState) {
                is CreateRecordUiState.Loading -> {
                    // Loading state - disable input
                    CreateRecordForm(
                        viewModel = viewModel,
                        enabled = false,
                        modifier = Modifier.padding(16.dp)
                    )
                }

                else -> {
                    CreateRecordForm(
                        viewModel = viewModel,
                        enabled = uiState !is CreateRecordUiState.Loading,
                        modifier = Modifier.padding(16.dp)
                    )
                }
            }
        }
    }
}

@Composable
private fun CreateRecordForm(
    viewModel: CreateRecordViewModel,
    enabled: Boolean,
    modifier: Modifier = Modifier
) {
    val uiState by viewModel.uiState.collectAsState()
    
    Column(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = androidx.compose.foundation.layout.Arrangement.spacedBy(16.dp)
    ) {
        // Title field (required)
        MaintenanceTextField(
            value = viewModel.title,
            onValueChange = viewModel::updateTitle,
            label = "Record Name *",
            placeholder = "e.g., Car, Air Conditioner",
            isError = viewModel.titleError != null,
            errorMessage = viewModel.titleError ?: "",
            keyboardType = KeyboardType.Text,
            modifier = Modifier.fillMaxWidth()
        )

        // Description field
        MaintenanceTextField(
            value = viewModel.description,
            onValueChange = viewModel::updateDescription,
            label = "Description",
            placeholder = "Enter details about this record",
            isError = false,
            keyboardType = KeyboardType.Text,
            maxLines = 3,
            modifier = Modifier.fillMaxWidth()
        )

        // Category field
        MaintenanceTextField(
            value = viewModel.category,
            onValueChange = viewModel::updateCategory,
            label = "Category",
            placeholder = "e.g., Vehicle, Home Appliance",
            isError = false,
            keyboardType = KeyboardType.Text,
            modifier = Modifier.fillMaxWidth()
        )

        // Location field
        MaintenanceTextField(
            value = viewModel.location,
            onValueChange = viewModel::updateLocation,
            label = "Location",
            placeholder = "e.g., Garage, Kitchen",
            isError = false,
            keyboardType = KeyboardType.Text,
            modifier = Modifier.fillMaxWidth()
        )

        // Brand/Model field
        MaintenanceTextField(
            value = viewModel.brandModel,
            onValueChange = viewModel::updateBrandModel,
            label = "Brand / Model",
            placeholder = "e.g., Honda Civic 2020",
            isError = false,
            keyboardType = KeyboardType.Text,
            modifier = Modifier.fillMaxWidth()
        )

        // Serial Number field
        MaintenanceTextField(
            value = viewModel.serialNumber,
            onValueChange = viewModel::updateSerialNumber,
            label = "Serial Number",
            placeholder = "Optional",
            isError = false,
            keyboardType = KeyboardType.Text,
            modifier = Modifier.fillMaxWidth()
        )

        // Notes field
        MaintenanceTextField(
            value = viewModel.notes,
            onValueChange = viewModel::updateNotes,
            label = "Notes",
            placeholder = "Additional notes",
            isError = false,
            keyboardType = KeyboardType.Text,
            maxLines = 3,
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(16.dp))

        // Save button
        MaintenanceButton(
            text = if (uiState is CreateRecordUiState.Loading) "Saving..." else "Save Record",
            onClick = { viewModel.saveRecord() },
            enabled = enabled && viewModel.titleError == null,
            isLoading = uiState is CreateRecordUiState.Loading,
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(8.dp))

        Text(
            text = "* Required field",
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

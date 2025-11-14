package com.maintenance.app.presentation.ui.screens.edit

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.maintenance.app.presentation.ui.components.LoadingIndicator
import com.maintenance.app.presentation.ui.components.MaintenanceButton
import com.maintenance.app.presentation.ui.components.MaintenanceTextField
import com.maintenance.app.presentation.viewmodels.edit.EditRecordViewModel
import com.maintenance.app.presentation.viewmodels.edit.EditRecordUiState

/**
 * Screen for editing an existing maintenance record.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EditRecordScreenSimple(
    recordId: Long,
    navController: NavController,
    viewModel: EditRecordViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    var showDeleteDialog by remember { mutableStateOf(false) }
    
    LaunchedEffect(recordId) {
        viewModel.loadRecord(recordId)
    }

    if (showDeleteDialog) {
        AlertDialog(
            onDismissRequest = { showDeleteDialog = false },
            title = { Text("Delete Record") },
            text = { Text("Are you sure you want to delete this record? This action cannot be undone.") },
            confirmButton = {
                Button(
                    onClick = {
                        showDeleteDialog = false
                        viewModel.deleteRecord()
                    }
                ) {
                    Text("Delete")
                }
            },
            dismissButton = {
                TextButton(
                    onClick = { showDeleteDialog = false }
                ) {
                    Text("Cancel")
                }
            }
        )
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Edit Record") },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                },
                actions = {
                    IconButton(
                        onClick = { showDeleteDialog = true },
                        enabled = uiState !is EditRecordUiState.Loading
                    ) {
                        Icon(Icons.Default.Delete, contentDescription = "Delete")
                    }
                    IconButton(
                        onClick = { viewModel.saveRecord() },
                        enabled = uiState !is EditRecordUiState.Loading
                    ) {
                        Icon(Icons.Default.Check, contentDescription = "Save")
                    }
                }
            )
        }
    ) { paddingValues ->
        when (uiState) {
            is EditRecordUiState.Loading -> {
                LoadingIndicator()
            }
            
            is EditRecordUiState.Success -> {
                LaunchedEffect(Unit) {
                    navController.popBackStack()
                }
            }
            
            is EditRecordUiState.Error -> {
                Text(
                    text = "Error: ${(uiState as EditRecordUiState.Error).message}",
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
                    // Record name field
                    MaintenanceTextField(
                        value = viewModel.recordName,
                        onValueChange = viewModel::updateRecordName,
                        label = "Record Name*",
                        placeholder = "e.g., Laptop HP",
                        isError = false,
                        keyboardType = KeyboardType.Text,
                        modifier = Modifier.fillMaxWidth()
                    )

                    // Description field
                    MaintenanceTextField(
                        value = viewModel.description,
                        onValueChange = viewModel::updateDescription,
                        label = "Description",
                        placeholder = "Describe the equipment",
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
                        placeholder = "e.g., Electronics, Machinery",
                        isError = false,
                        keyboardType = KeyboardType.Text,
                        modifier = Modifier.fillMaxWidth()
                    )

                    // Location field
                    MaintenanceTextField(
                        value = viewModel.location,
                        onValueChange = viewModel::updateLocation,
                        label = "Location",
                        placeholder = "Where is it located?",
                        isError = false,
                        keyboardType = KeyboardType.Text,
                        modifier = Modifier.fillMaxWidth()
                    )

                    // Brand/Model field
                    MaintenanceTextField(
                        value = viewModel.brandModel,
                        onValueChange = viewModel::updateBrandModel,
                        label = "Brand/Model",
                        placeholder = "e.g., HP Pavilion 15",
                        isError = false,
                        keyboardType = KeyboardType.Text,
                        modifier = Modifier.fillMaxWidth()
                    )

                    // Serial Number field
                    MaintenanceTextField(
                        value = viewModel.serialNumber,
                        onValueChange = viewModel::updateSerialNumber,
                        label = "Serial Number",
                        placeholder = "Device serial number",
                        isError = false,
                        keyboardType = KeyboardType.Text,
                        modifier = Modifier.fillMaxWidth()
                    )

                    // Purchase Date field
                    MaintenanceTextField(
                        value = viewModel.purchaseDate,
                        onValueChange = viewModel::updatePurchaseDate,
                        label = "Purchase Date",
                        placeholder = "YYYY-MM-DD",
                        isError = false,
                        keyboardType = KeyboardType.Text,
                        modifier = Modifier.fillMaxWidth()
                    )

                    // Warranty Expiry field
                    MaintenanceTextField(
                        value = viewModel.warrantyExpiry,
                        onValueChange = viewModel::updateWarrantyExpiry,
                        label = "Warranty Expiry",
                        placeholder = "YYYY-MM-DD",
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
                        text = "Save Changes",
                        onClick = { viewModel.saveRecord() },
                        enabled = uiState !is EditRecordUiState.Loading && 
                                viewModel.recordName.isNotBlank(),
                        isLoading = uiState is EditRecordUiState.Loading,
                        modifier = Modifier.fillMaxWidth()
                    )

                    Spacer(modifier = Modifier.height(32.dp))
                }
            }
        }
    }
}

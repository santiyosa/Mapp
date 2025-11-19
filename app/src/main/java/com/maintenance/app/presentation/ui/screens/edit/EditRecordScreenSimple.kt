package com.maintenance.app.presentation.ui.screens.edit

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.maintenance.app.R
import com.maintenance.app.presentation.navigation.Screen
import com.maintenance.app.presentation.ui.components.LoadingIndicator
import com.maintenance.app.presentation.ui.components.MainScaffold
import com.maintenance.app.presentation.viewmodels.edit.EditRecordViewModel
import com.maintenance.app.presentation.viewmodels.edit.EditRecordUiState

/**
 * Screen for editing an existing maintenance record.
 */
@Composable
fun EditRecordScreenSimple(
    recordId: Long,
    navController: NavController,
    viewModel: EditRecordViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    var showDeleteDialog by remember { mutableStateOf(false) }
    val snackbarHostState = remember { SnackbarHostState() }
    
    LaunchedEffect(recordId) {
        viewModel.loadRecord(recordId)
    }

    if (showDeleteDialog) {
        AlertDialog(
            onDismissRequest = { showDeleteDialog = false },
            title = { Text(stringResource(R.string.delete_record)) },
            text = { Text(stringResource(R.string.delete_record_confirmation)) },
            confirmButton = {
                Button(
                    onClick = {
                        showDeleteDialog = false
                        viewModel.deleteRecord()
                    }
                ) {
                    Text(stringResource(R.string.delete))
                }
            },
            dismissButton = {
                TextButton(
                    onClick = { showDeleteDialog = false }
                ) {
                    Text(stringResource(R.string.cancel))
                }
            }
        )
    }

    MainScaffold(
        title = stringResource(R.string.edit_record),
        navController = navController,
        showBottomBar = false,
        showBackButton = true,
        onBackClick = { navController.navigateUp() },
        actions = {
            IconButton(
                onClick = { showDeleteDialog = true },
                enabled = uiState !is EditRecordUiState.Loading
            ) {
                Icon(Icons.Default.Delete, contentDescription = stringResource(R.string.delete))
            }
        },
        snackbarHost = {
            SnackbarHost(hostState = snackbarHostState)
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
            
            is EditRecordUiState.Deleted -> {
                LaunchedEffect(Unit) {
                    navController.navigate(Screen.Home.route) {
                        popUpTo(Screen.Home.route) { inclusive = true }
                    }
                }
            }
            
            is EditRecordUiState.Error -> {
                LaunchedEffect(uiState) {
                    snackbarHostState.showSnackbar(
                        message = (uiState as EditRecordUiState.Error).message,
                        duration = SnackbarDuration.Long
                    )
                }
            }
            
            else -> {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(paddingValues)
                        .padding(16.dp)
                        .verticalScroll(rememberScrollState()),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    // Title field
                    OutlinedTextField(
                        value = viewModel.title,
                        onValueChange = viewModel::updateTitle,
                        label = { Text(stringResource(R.string.title)) },
                        modifier = Modifier.fillMaxWidth(),
                        keyboardOptions = KeyboardOptions(
                            capitalization = KeyboardCapitalization.Words,
                            imeAction = ImeAction.Next
                        ),
                        singleLine = true,
                        isError = viewModel.titleError != null
                    )
                    
                    viewModel.titleError?.let { error ->
                        Text(
                            text = error,
                            color = MaterialTheme.colorScheme.error,
                            style = MaterialTheme.typography.bodySmall
                        )
                    }
                    
                    // Category field
                    OutlinedTextField(
                        value = viewModel.category,
                        onValueChange = viewModel::updateCategory,
                        label = { Text(stringResource(R.string.category)) },
                        modifier = Modifier.fillMaxWidth(),
                        keyboardOptions = KeyboardOptions(
                            capitalization = KeyboardCapitalization.Words,
                            imeAction = ImeAction.Next
                        ),
                        singleLine = true,
                        isError = viewModel.categoryError != null
                    )
                    
                    viewModel.categoryError?.let { error ->
                        Text(
                            text = error,
                            color = MaterialTheme.colorScheme.error,
                            style = MaterialTheme.typography.bodySmall
                        )
                    }
                    
                    // Description field
                    OutlinedTextField(
                        value = viewModel.description,
                        onValueChange = viewModel::updateDescription,
                        label = { Text(stringResource(R.string.description)) },
                        modifier = Modifier.fillMaxWidth(),
                        keyboardOptions = KeyboardOptions(
                            capitalization = KeyboardCapitalization.Sentences,
                            imeAction = ImeAction.Done
                        ),
                        minLines = 3,
                        maxLines = 5
                    )
                    
                    // Loading indicator
                    if (uiState is EditRecordUiState.Loading) {
                        Box(
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            LinearProgressIndicator(
                                modifier = Modifier.fillMaxWidth()
                            )
                        }
                    }
                    
                    // Save button
                    Button(
                        onClick = { viewModel.saveRecord() },
                        modifier = Modifier.fillMaxWidth(),
                        enabled = uiState !is EditRecordUiState.Loading
                    ) {
                        if (uiState is EditRecordUiState.Loading) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(16.dp),
                                strokeWidth = 2.dp
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                        }
                        Text(stringResource(R.string.save_record))
                    }
                }
            }
        }
    }
}

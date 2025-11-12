package com.maintenance.app.presentation.ui.screens.edit

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.maintenance.app.R
import com.maintenance.app.presentation.ui.components.LoadingState
import com.maintenance.app.presentation.ui.components.MainScaffold
import com.maintenance.app.presentation.viewmodels.edit.EditRecordViewModel
import com.maintenance.app.presentation.viewmodels.edit.EditRecordUiState

/**
 * Screen for editing an existing maintenance record.
 */
@Composable
fun EditRecordScreen(
    recordId: Long,
    navController: NavController,
    viewModel: EditRecordViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }
    var showDeleteDialog by remember { mutableStateOf(false) }
    
    LaunchedEffect(recordId) {
        viewModel.loadRecord(recordId)
    }
    
    LaunchedEffect(uiState) {
        val currentState = uiState
        when (currentState) {
            is EditRecordUiState.Success -> {
                navController.navigateUp()
            }
            is EditRecordUiState.Deleted -> {
                navController.popBackStack()
            }
            is EditRecordUiState.Error -> {
                snackbarHostState.showSnackbar(
                    message = currentState.message,
                    duration = SnackbarDuration.Long
                )
            }
            else -> { /* No action needed */ }
        }
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
                enabled = uiState is EditRecordUiState.Loaded
            ) {
                Icon(
                    imageVector = Icons.Default.Delete,
                    contentDescription = stringResource(R.string.delete_record)
                )
            }
            IconButton(
                onClick = { viewModel.saveRecord() },
                enabled = uiState is EditRecordUiState.Loaded
            ) {
                Icon(
                    imageVector = Icons.Default.Check,
                    contentDescription = stringResource(R.string.save)
                )
            }
        },
        snackbarHost = {
            SnackbarHost(hostState = snackbarHostState)
        }
    ) { paddingValues ->
        when (uiState) {
            is EditRecordUiState.Loading -> {
                LoadingState(message = stringResource(R.string.loading_record))
            }
            is EditRecordUiState.Loaded, is EditRecordUiState.Saving -> {
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
                        isError = viewModel.titleError != null,
                        enabled = uiState !is EditRecordUiState.Saving
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
                        isError = viewModel.categoryError != null,
                        enabled = uiState !is EditRecordUiState.Saving
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
                        maxLines = 5,
                        enabled = uiState !is EditRecordUiState.Saving
                    )
                    
                    // Loading indicator
                    if (uiState is EditRecordUiState.Saving) {
                        LinearProgressIndicator(
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                    
                    // Save button
                    Button(
                        onClick = { viewModel.saveRecord() },
                        modifier = Modifier.fillMaxWidth(),
                        enabled = uiState !is EditRecordUiState.Saving
                    ) {
                        if (uiState is EditRecordUiState.Saving) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(16.dp),
                                strokeWidth = 2.dp
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                        }
                        Text(stringResource(R.string.save_changes))
                    }
                }
            }
            else -> { /* Handle other states if needed */ }
        }
    }
    
    // Delete confirmation dialog
    if (showDeleteDialog) {
        AlertDialog(
            onDismissRequest = { showDeleteDialog = false },
            title = { Text(stringResource(R.string.confirm_delete)) },
            text = { Text(stringResource(R.string.delete_record_confirmation)) },
            confirmButton = {
                TextButton(
                    onClick = {
                        showDeleteDialog = false
                        viewModel.deleteRecord()
                    }
                ) {
                    Text(
                        text = stringResource(R.string.delete),
                        color = MaterialTheme.colorScheme.error
                    )
                }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteDialog = false }) {
                    Text(stringResource(R.string.cancel))
                }
            }
        )
    }
}
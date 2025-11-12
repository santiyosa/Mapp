package com.maintenance.app.presentation.ui.screens.create

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Check
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
import com.maintenance.app.presentation.ui.components.MainScaffold
import com.maintenance.app.presentation.viewmodels.create.CreateRecordViewModel
import com.maintenance.app.presentation.viewmodels.create.CreateRecordUiState

/**
 * Screen for creating a new maintenance record.
 */
@Composable
fun CreateRecordScreen(
    navController: NavController,
    viewModel: CreateRecordViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }
    
    LaunchedEffect(uiState) {
        val currentState = uiState
        when (currentState) {
            is CreateRecordUiState.Success -> {
                navController.navigateUp()
            }
            is CreateRecordUiState.Error -> {
                snackbarHostState.showSnackbar(
                    message = currentState.message,
                    duration = SnackbarDuration.Long
                )
            }
            else -> { /* No action needed */ }
        }
    }
    
    MainScaffold(
        title = stringResource(R.string.create_record),
        navController = navController,
        showBottomBar = false,
        showBackButton = true,
        onBackClick = { navController.navigateUp() },
        actions = {
            IconButton(
                onClick = { viewModel.saveRecord() },
                enabled = uiState !is CreateRecordUiState.Loading
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
            if (uiState is CreateRecordUiState.Loading) {
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
                enabled = uiState !is CreateRecordUiState.Loading
            ) {
                if (uiState is CreateRecordUiState.Loading) {
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
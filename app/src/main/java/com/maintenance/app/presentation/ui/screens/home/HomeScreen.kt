package com.maintenance.app.presentation.ui.screens.home

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.maintenance.app.R
import com.maintenance.app.presentation.navigation.Screen
import com.maintenance.app.presentation.ui.components.*
import com.maintenance.app.presentation.viewmodels.home.HomeViewModel
import com.maintenance.app.presentation.viewmodels.home.HomeUiState

/**
 * Home screen that displays the list of maintenance records.
 */
@Composable
fun HomeScreen(
    navController: NavController,
    viewModel: HomeViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    
    MainScaffold(
        title = stringResource(R.string.app_name),
        navController = navController,
        showBottomBar = true,
        actions = {
            IconButton(
                onClick = { 
                    navController.navigate(Screen.Search.route)
                }
            ) {
                Icon(
                    imageVector = Icons.Default.Search,
                    contentDescription = stringResource(R.string.search)
                )
            }
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = {
                    navController.navigate(Screen.CreateRecord.route)
                }
            ) {
                Icon(
                    imageVector = Icons.Default.Add,
                    contentDescription = stringResource(R.string.add_record)
                )
            }
        }
    ) { paddingValues ->
        val currentState = uiState
        when (currentState) {
            is HomeUiState.Loading -> {
                LoadingState(message = stringResource(R.string.loading_records))
            }
            is HomeUiState.Error -> {
                ErrorState(
                    message = currentState.message,
                    onRetry = { viewModel.loadRecords() }
                )
            }
            is HomeUiState.Empty -> {
                EmptyState(
                    message = stringResource(R.string.no_records),
                    actionText = stringResource(R.string.create_first_record),
                    onActionClick = {
                        navController.navigate(Screen.CreateRecord.route)
                    }
                )
            }
            is HomeUiState.Success -> {
                LazyColumn(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(paddingValues),
                    contentPadding = PaddingValues(16.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(currentState.records) { record ->
                        RecordCard(
                            record = record,
                            onClick = {
                                navController.navigate(
                                    Screen.RecordDetail.createRoute(record.id)
                                )
                            }
                        )
                    }
                }
            }
        }
    }
}

/**
 * Card component for displaying a maintenance record.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun RecordCard(
    record: com.maintenance.app.domain.model.Record,
    onClick: () -> Unit
) {
    Card(
        onClick = onClick,
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = record.name,
                    style = MaterialTheme.typography.titleMedium
                )
                AssistChip(
                    onClick = { },
                    label = { 
                        Text(
                            text = record.category ?: "Sin categoría",
                            style = MaterialTheme.typography.labelSmall
                        )
                    }
                )
            }
            
            Spacer(modifier = Modifier.height(8.dp))
            
            if (!record.description.isNullOrBlank()) {
                Text(
                    text = record.description,
                    style = MaterialTheme.typography.bodyMedium,
                    maxLines = 2
                )
                Spacer(modifier = Modifier.height(8.dp))
            }
            
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = "Created: ${record.createdDate}",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                
                record.lastMaintenanceDate?.let { lastMaintenance ->
                    Text(
                        text = "Last: $lastMaintenance",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }
    }
}
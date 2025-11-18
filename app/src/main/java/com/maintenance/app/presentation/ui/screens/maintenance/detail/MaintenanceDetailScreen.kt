package com.maintenance.app.presentation.ui.screens.maintenance.detail

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.maintenance.app.R
import com.maintenance.app.presentation.navigation.Screen
import com.maintenance.app.presentation.ui.components.MainScaffold
import com.maintenance.app.presentation.viewmodels.maintenance.detail.MaintenanceDetailViewModel
import com.maintenance.app.presentation.viewmodels.maintenance.detail.MaintenanceDetailUiState

/**
 * Screen for displaying maintenance details.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MaintenanceDetailScreen(
    maintenanceId: Long,
    navController: NavController,
    viewModel: MaintenanceDetailViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    
    LaunchedEffect(maintenanceId) {
        viewModel.loadMaintenance(maintenanceId)
    }

    MainScaffold(
        title = "Detalles del Mantenimiento",
        navController = navController,
        showBottomBar = true,
        showBackButton = true,
        onBackClick = { navController.navigateUp() },
        actions = {
            if (uiState is MaintenanceDetailUiState.Success) {
                IconButton(
                    onClick = {
                        val maintenance = (uiState as MaintenanceDetailUiState.Success).maintenance
                        navController.navigate(
                            Screen.EditMaintenance.createRoute(maintenance.id)
                        )
                    }
                ) {
                    Icon(
                        imageVector = Icons.Default.Edit,
                        contentDescription = "Editar"
                    )
                }
            }
        }
    ) { paddingValues ->
        when (uiState) {
            is MaintenanceDetailUiState.Loading -> {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(paddingValues),
                    contentAlignment = androidx.compose.ui.Alignment.Center
                ) {
                    CircularProgressIndicator()
                }
            }
            
            is MaintenanceDetailUiState.Success -> {
                val maintenance = (uiState as MaintenanceDetailUiState.Success).maintenance
                LazyColumn(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(paddingValues)
                        .padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    item {
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            colors = CardDefaults.cardColors(
                                containerColor = MaterialTheme.colorScheme.surfaceVariant
                            )
                        ) {
                            Column(
                                modifier = Modifier.padding(16.dp),
                                verticalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Text(
                                    text = "Tipo: ${maintenance.type}",
                                    style = MaterialTheme.typography.bodyMedium
                                )
                                Text(
                                    text = "Descripción: ${maintenance.description}",
                                    style = MaterialTheme.typography.bodyMedium
                                )
                                Text(
                                    text = "Fecha: ${maintenance.maintenanceDate}",
                                    style = MaterialTheme.typography.bodyMedium
                                )
                                if (maintenance.cost != null) {
                                    Text(
                                        text = "Costo: ${maintenance.cost} ${maintenance.currency}",
                                        style = MaterialTheme.typography.bodyMedium
                                    )
                                }
                                if (!maintenance.notes.isNullOrEmpty()) {
                                    Text(
                                        text = "Notas: ${maintenance.notes}",
                                        style = MaterialTheme.typography.bodyMedium
                                    )
                                }
                            }
                        }
                    }
                }
            }
            
            is MaintenanceDetailUiState.Error -> {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(paddingValues),
                    contentAlignment = androidx.compose.ui.Alignment.Center
                ) {
                    Text(
                        text = (uiState as MaintenanceDetailUiState.Error).message,
                        style = MaterialTheme.typography.bodyMedium
                    )
                }
            }
        }
    }
}

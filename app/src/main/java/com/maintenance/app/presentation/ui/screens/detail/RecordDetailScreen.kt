package com.maintenance.app.presentation.ui.screens.detail

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.*
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.runtime.*
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import kotlinx.coroutines.launch
import com.maintenance.app.R
import com.maintenance.app.presentation.navigation.Screen
import com.maintenance.app.presentation.ui.components.*
import com.maintenance.app.presentation.ui.screens.maintenance.detail.MaintenanceDetailDialog
import com.maintenance.app.presentation.viewmodels.detail.RecordDetailViewModel
import com.maintenance.app.presentation.viewmodels.detail.RecordDetailUiState
import com.maintenance.app.utils.ImageManager

/**
 * Screen for displaying record details and maintenance history.
 */
@Composable
fun RecordDetailScreen(
    recordId: Long,
    navController: NavController,
    initialMaintenanceId: Long? = null,
    viewModel: RecordDetailViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    val isWhatsAppAvailable by viewModel.isWhatsAppAvailable.collectAsState()
    val shareLoading by viewModel.shareLoading.collectAsState()
    val shareError by viewModel.shareError.collectAsState()
    val context = LocalContext.current
    
    var selectedMaintenanceId by remember { mutableStateOf(initialMaintenanceId) }
    var showMaintenanceSelectionDialog by remember { mutableStateOf(false) }
    
    LaunchedEffect(recordId) {
        viewModel.loadRecord(recordId)
    }

    // Show error message as snackbar
    shareError?.let { error ->
        LaunchedEffect(error) {
            // Can show snackbar here if needed
        }
    }
    
    // Show maintenance detail dialog
    selectedMaintenanceId?.let { maintenanceId ->
        MaintenanceDetailDialog(
            maintenanceId = maintenanceId,
            navController = navController,
            onDismiss = { selectedMaintenanceId = null }
        )
    }

    // Show maintenance selection dialog for sharing
    if (showMaintenanceSelectionDialog && uiState is RecordDetailUiState.Success) {
        val maintenances = (uiState as RecordDetailUiState.Success).maintenances
        MaintenanceSelectionDialog(
            maintenances = maintenances,
            onConfirm = { selectedMaintenances ->
                showMaintenanceSelectionDialog = false
                
                // Build share message with selected maintenances
                val record = (uiState as RecordDetailUiState.Success).record
                val messageBuilder = StringBuilder()
                messageBuilder.append("Registro: ${record.name}\n")
                messageBuilder.append("Descripción: ${record.description ?: "Sin descripción"}\n")
                messageBuilder.append("Categoría: ${record.category ?: "Sin categoría"}\n")
                
                if (selectedMaintenances.isNotEmpty()) {
                    messageBuilder.append("\n--- Mantenimientos Seleccionados ---\n\n")
                    selectedMaintenances.forEach { maintenance ->
                        messageBuilder.append(buildMaintenanceDetailMessage(maintenance))
                        messageBuilder.append("\n")
                    }
                }
                
                val message = messageBuilder.toString()
                val intent = android.content.Intent().apply {
                    action = android.content.Intent.ACTION_SEND
                    putExtra(android.content.Intent.EXTRA_TEXT, message)
                    type = "text/plain"
                }
                context.startActivity(android.content.Intent.createChooser(intent, "Compartir Registro"))
            },
            onDismiss = { showMaintenanceSelectionDialog = false }
        )
    }
    
    MainScaffold(
        title = stringResource(R.string.record_details),
        navController = navController,
        showBottomBar = true,
        showBackButton = true,
        recordId = recordId,
        onBackClick = { navController.navigateUp() },
        actions = {
            // Edit button - this works fine
            IconButton(
                onClick = {
                    android.util.Log.d("RecordDetailScreen", "Edit button clicked")
                    navController.navigate(
                        Screen.EditRecord.createRoute(recordId)
                    )
                }
            ) {
                Icon(
                    imageVector = Icons.Default.Edit,
                    contentDescription = stringResource(R.string.edit_record)
                )
            }
        },
        floatingActionButton = {
            if (uiState is RecordDetailUiState.Success) {
                FloatingActionButton(
                    onClick = {
                        navController.navigate(
                            Screen.CreateMaintenance.createRoute(recordId)
                        )
                    }
                ) {
                    Icon(
                        imageVector = Icons.Default.Add,
                        contentDescription = stringResource(R.string.add_maintenance)
                    )
                }
            }
        }
    ) { paddingValues ->
        val currentState = uiState
        when (currentState) {
            is RecordDetailUiState.Loading -> {
                LoadingState(message = stringResource(R.string.loading_record))
            }
            is RecordDetailUiState.Error -> {
                ErrorState(
                    message = currentState.message,
                    onRetry = { viewModel.loadRecord(recordId) }
                )
            }
            is RecordDetailUiState.Success -> {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(paddingValues)
                ) {
                    // Buttons row
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp, vertical = 8.dp),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        // Schedule maintenance button (for future use)
                        Button(
                            onClick = { 
                                // TODO: Implement scheduling functionality
                                // For now, this button is placeholder for future maintenance scheduling
                            },
                            modifier = Modifier.weight(1f)
                        ) {
                            Text("Programar")
                        }
                        
                        // Share with maintenances button
                        Button(
                            onClick = { 
                                if (currentState.maintenances.isNotEmpty()) {
                                    showMaintenanceSelectionDialog = true
                                }
                            },
                            modifier = Modifier.weight(1f),
                            enabled = currentState.maintenances.isNotEmpty()
                        ) {
                            Text("Compartir +")
                        }
                    }
                    
                    LazyColumn(
                        modifier = Modifier
                            .fillMaxSize(),
                        contentPadding = PaddingValues(16.dp),
                        verticalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        // Record information
                        item {
                            RecordInfoCard(record = currentState.record)
                        }
                    
                    // Maintenance history section
                    item {
                        Text(
                            text = stringResource(R.string.maintenance_history),
                            style = MaterialTheme.typography.titleMedium
                        )
                    }
                    
                    if (currentState.maintenances.isEmpty()) {
                        item {
                            EmptyMaintenanceCard {
                                navController.navigate(
                                    Screen.CreateMaintenance.createRoute(recordId)
                                )
                            }
                        }
                    } else {
                        items(currentState.maintenances) { maintenance ->
                            MaintenanceCard(
                                maintenance = maintenance,
                                onClick = {
                                    selectedMaintenanceId = maintenance.id
                                },
                                onEditClick = {
                                    navController.navigate(
                                        Screen.EditMaintenance.createRoute(maintenance.id)
                                    )
                                }
                            )
                        }
                    }
                }
                }
            }
        }
    }
}

/**
 * Card displaying record information.
 */
@Composable
private fun RecordInfoCard(
    record: com.maintenance.app.domain.model.Record
) {
    Card(
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Text(
                text = record.name,
                style = MaterialTheme.typography.headlineSmall
            )
            
            Spacer(modifier = Modifier.height(8.dp))
            
            AssistChip(
                onClick = { },
                label = { 
                    Text(
                        text = record.category ?: "Sin categoría",
                        style = MaterialTheme.typography.labelMedium
                    )
                }
            )
            
            Spacer(modifier = Modifier.height(16.dp))
            
            if (!record.description.isNullOrBlank()) {
                Text(
                    text = record.description,
                    style = MaterialTheme.typography.bodyMedium
                )
                Spacer(modifier = Modifier.height(16.dp))
            }
            
            Divider()
            Spacer(modifier = Modifier.height(8.dp))
            
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column {
                    Text(
                        text = stringResource(R.string.created),
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        text = record.createdDate.toString(),
                        style = MaterialTheme.typography.bodySmall
                    )
                }
                
                record.lastMaintenanceDate?.let { lastMaintenance ->
                    Column {
                        Text(
                            text = stringResource(R.string.last_maintenance),
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Text(
                            text = lastMaintenance.toString(),
                            style = MaterialTheme.typography.bodySmall
                        )
                    }
                }
            }
        }
    }
}

/**
 * Card for empty maintenance state.
 */
@Composable
private fun EmptyMaintenanceCard(
    onAddClick: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = stringResource(R.string.no_maintenance_records),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(modifier = Modifier.height(16.dp))
            Button(onClick = onAddClick) {
                Text(stringResource(R.string.add_first_maintenance))
            }
        }
    }
}

/**
 * Card for displaying maintenance information.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun MaintenanceCard(
    maintenance: com.maintenance.app.domain.model.Maintenance,
    onClick: () -> Unit,
    onEditClick: (() -> Unit)? = null
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
                verticalAlignment = Alignment.Top
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = maintenance.type,
                            style = MaterialTheme.typography.titleSmall,
                            modifier = Modifier.weight(1f)
                        )
                        Text(
                            text = maintenance.maintenanceDate.format(
                                java.time.format.DateTimeFormatter.ofPattern("dd/MM/yyyy")
                            ),
                            style = MaterialTheme.typography.labelMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    
                    if (maintenance.description.isNotBlank()) {
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = maintenance.description,
                            style = MaterialTheme.typography.bodySmall,
                            maxLines = 2
                        )
                    }
                    
                    maintenance.cost?.let { cost ->
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "${maintenance.currency.takeIf { it.isNotBlank() } ?: "COP"} $cost",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.primary
                        )
                    }
                    
                    // Image thumbnails
                    if (maintenance.imagesPaths.isNotEmpty()) {
                        Spacer(modifier = Modifier.height(8.dp))
                        ImageThumbnails(
                            imagePaths = maintenance.imagesPaths.take(3), // Show max 3 thumbnails
                            totalImages = maintenance.imagesPaths.size
                        )
                    }
                }
                
                // Edit button
                onEditClick?.let { editClick ->
                    IconButton(onClick = editClick) {
                        Icon(
                            imageVector = Icons.Default.Edit,
                            contentDescription = "Editar mantenimiento",
                            tint = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun ImageThumbnails(
    imagePaths: List<String>,
    totalImages: Int
) {
    val context = LocalContext.current
    val imageManager = remember { ImageManager(context) }
    val coroutineScope = rememberCoroutineScope()
    
    LazyRow(
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        items(imagePaths) { imagePath ->
            var bitmap by remember(imagePath) { mutableStateOf<ImageBitmap?>(null) }
            
            LaunchedEffect(imagePath) {
                coroutineScope.launch {
                    bitmap = imageManager.loadBitmapFromPath(imagePath)?.asImageBitmap()
                }
            }
            
            bitmap?.let { bmp ->
                Image(
                    bitmap = bmp,
                    contentDescription = "Mantenimiento imagen",
                    modifier = Modifier
                        .size(60.dp)
                        .clip(RoundedCornerShape(8.dp))
                        .background(MaterialTheme.colorScheme.surfaceVariant),
                    contentScale = ContentScale.Crop
                )
            } ?: Box(
                modifier = Modifier
                    .size(60.dp)
                    .clip(RoundedCornerShape(8.dp))
                    .background(MaterialTheme.colorScheme.surfaceVariant),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator(
                    modifier = Modifier.size(20.dp),
                    color = MaterialTheme.colorScheme.primary
                )
            }
        }
        
        // Show "+X more" indicator if there are more images
        if (totalImages > imagePaths.size) {
            item {
                Box(
                    modifier = Modifier
                        .size(60.dp)
                        .clip(RoundedCornerShape(8.dp))
                        .background(MaterialTheme.colorScheme.secondaryContainer),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "+${totalImages - imagePaths.size}",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSecondaryContainer,
                        textAlign = TextAlign.Center
                    )
                }
            }
        }
    }
}

/**
 * Builds a detailed message for a maintenance record to be shared.
 */
private fun buildMaintenanceDetailMessage(maintenance: com.maintenance.app.domain.model.Maintenance): String {
    val messageBuilder = StringBuilder()
    
    // Type and date
    messageBuilder.append("📋 ${maintenance.type}\n")
    messageBuilder.append("📅 Fecha: ${maintenance.maintenanceDate.format(java.time.format.DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm"))}\n")
    
    // Status and Priority
    messageBuilder.append("✓ Estado: ${maintenance.status.displayName}\n")
    messageBuilder.append("⚡ Prioridad: ${maintenance.priority.displayName}\n")
    
    // Description
    if (maintenance.description.isNotBlank()) {
        messageBuilder.append("📝 Descripción: ${maintenance.description}\n")
    }
    
    // Performed by
    maintenance.performedBy?.let { performedBy ->
        if (performedBy.isNotBlank()) {
            messageBuilder.append("👤 Realizado por: $performedBy\n")
        }
    }
    
    // Location
    maintenance.location?.let { location ->
        if (location.isNotBlank()) {
            messageBuilder.append("📍 Ubicación: $location\n")
        }
    }
    
    // Cost
    maintenance.cost?.let { cost ->
        val currencyCode = maintenance.currency.takeIf { it.isNotBlank() } ?: "COP"
        messageBuilder.append("💰 Costo: $currencyCode ${cost.setScale(2, java.math.RoundingMode.HALF_UP)}\n")
    }
    
    // Duration
    maintenance.durationMinutes?.let { duration ->
        if (duration > 0) {
            val hours = duration / 60
            val minutes = duration % 60
            val durationStr = when {
                hours > 0 && minutes > 0 -> "$hours h $minutes min"
                hours > 0 -> "$hours h"
                else -> "$minutes min"
            }
            messageBuilder.append("⏱️ Duración: $durationStr\n")
        }
    }
    
    // Parts replaced
    maintenance.partsReplaced?.let { parts ->
        if (parts.isNotBlank()) {
            messageBuilder.append("🔧 Partes reemplazadas: $parts\n")
        }
    }
    
    // Next maintenance due
    maintenance.nextMaintenanceDue?.let { nextMaintenance ->
        messageBuilder.append("🔁 Próximo mantenimiento: ${nextMaintenance.format(java.time.format.DateTimeFormatter.ofPattern("dd/MM/yyyy"))}\n")
    }
    
    // Notes
    maintenance.notes?.let { notes ->
        if (notes.isNotBlank()) {
            messageBuilder.append("📌 Notas: $notes\n")
        }
    }
    
    // Recurrence info
    if (maintenance.isRecurring) {
        maintenance.recurrenceIntervalDays?.let { days ->
            messageBuilder.append("🔄 Recurrencia: Cada $days días\n")
        }
    }
    
    // Images count
    if (maintenance.imagesPaths.isNotEmpty()) {
        messageBuilder.append("📸 Imágenes: ${maintenance.imagesPaths.size}\n")
    }
    
    return messageBuilder.toString()
}
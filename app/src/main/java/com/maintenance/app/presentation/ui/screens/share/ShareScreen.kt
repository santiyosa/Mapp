package com.maintenance.app.presentation.ui.screens.share

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.maintenance.app.presentation.share.ShareViewModel
import com.maintenance.app.presentation.utils.ShareManager

/**
 * Share Screen for generating and sharing maintenance records
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ShareScreen(
    viewModel: ShareViewModel = hiltViewModel(),
    recordId: Long,
    maintenanceId: Long? = null,
    navController: NavController? = null,
    onNavigateBack: () -> Unit = {}
) {
    val uiState by viewModel.uiState.collectAsState()
    val context = LocalContext.current
    var showEditDialog by remember { mutableStateOf(false) }
    var editableText by remember { mutableStateOf("") }

    LaunchedEffect(recordId) {
        viewModel.loadAndGenerateImage(recordId, maintenanceId)
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Share Maintenance") },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer,
                    titleContentColor = MaterialTheme.colorScheme.primary
                )
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .verticalScroll(rememberScrollState())
        ) {
            // Error Message
            if (uiState.errorMessage != null) {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(12.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.errorContainer
                    )
                ) {
                    Text(
                        text = uiState.errorMessage!!,
                        color = MaterialTheme.colorScheme.error,
                        style = MaterialTheme.typography.bodySmall,
                        modifier = Modifier.padding(12.dp)
                    )
                }
            }

            // Success Message
            if (uiState.successMessage != null) {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(12.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.primaryContainer
                    )
                ) {
                    Text(
                        text = uiState.successMessage!!,
                        color = MaterialTheme.colorScheme.primary,
                        style = MaterialTheme.typography.bodySmall,
                        modifier = Modifier.padding(12.dp)
                    )
                }
            }

            // Loading State
            if (uiState.isLoading) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(300.dp),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator()
                }
            } else {
                // Image Preview
                if (uiState.generatedBitmap != null) {
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(12.dp),
                        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
                    ) {
                        Image(
                            bitmap = uiState.generatedBitmap!!.asImageBitmap(),
                            contentDescription = "Generated Maintenance Image",
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(400.dp),
                            contentScale = ContentScale.Crop
                        )
                    }
                }

                // Share Text Preview
                if (uiState.shareText != null) {
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(12.dp),
                        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = "Share Message",
                                    style = MaterialTheme.typography.titleMedium
                                )
                                IconButton(
                                    onClick = {
                                        editableText = uiState.shareText!!
                                        showEditDialog = true
                                    },
                                    modifier = Modifier.size(24.dp)
                                ) {
                                    Icon(
                                        Icons.Default.Edit,
                                        contentDescription = "Edit message",
                                        modifier = Modifier.size(20.dp)
                                    )
                                }
                            }

                            Text(
                                text = uiState.shareText!!,
                                style = MaterialTheme.typography.bodySmall,
                                fontStyle = FontStyle.Italic,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .background(
                                        MaterialTheme.colorScheme.surfaceVariant,
                                        shape = MaterialTheme.shapes.small
                                    )
                                    .padding(8.dp)
                            )
                        }
                    }
                }

                // Share Buttons
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(12.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    // WhatsApp Share Button
                    if (ShareManager.isWhatsAppInstalled(context)) {
                        Button(
                            modifier = Modifier.fillMaxWidth(),
                            onClick = {
                                if (uiState.imageUri != null && uiState.shareText != null) {
                                    val file = java.io.File(
                                        context.cacheDir,
                                        "maintenance_${System.currentTimeMillis()}.jpg"
                                    )
                                    ShareManager.shareToWhatsAppWithImage(
                                        context,
                                        uiState.shareText!!,
                                        file
                                    )
                                }
                            },
                            colors = ButtonDefaults.buttonColors(
                                containerColor = MaterialTheme.colorScheme.tertiary
                            )
                        ) {
                            Text("Share to WhatsApp 📱")
                        }
                    } else {
                        OutlinedButton(
                            modifier = Modifier.fillMaxWidth(),
                            onClick = {
                                // Show message that WhatsApp is not installed
                            },
                            enabled = false
                        ) {
                            Text("WhatsApp not installed")
                        }
                    }

                    // Share to Other Apps Button
                    Button(
                        modifier = Modifier.fillMaxWidth(),
                        onClick = {
                            if (uiState.shareText != null) {
                                ShareManager.shareToOtherApps(context, uiState.shareText!!)
                            }
                        }
                    ) {
                        Text("Share via... 📤")
                    }

                    // Copy to Clipboard Button
                    OutlinedButton(
                        modifier = Modifier.fillMaxWidth(),
                        onClick = {
                            if (uiState.shareText != null) {
                                ShareManager.copyToClipboard(context, uiState.shareText!!)
                            }
                        }
                    ) {
                        Text("Copy Message 📋")
                    }
                }

                // Additional Info
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(12.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.surfaceVariant
                    )
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text(
                            text = "💡 Tip",
                            style = MaterialTheme.typography.labelMedium
                        )
                        Text(
                            text = "You can edit the share message before sending. Images will be saved to your device cache.",
                            style = MaterialTheme.typography.bodySmall,
                            modifier = Modifier.padding(top = 8.dp)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))
            }
        }
    }

    // Edit Text Dialog
    if (showEditDialog) {
        EditMessageDialog(
            initialText = editableText,
            onConfirm = { newText ->
                viewModel.updateShareText(newText)
                showEditDialog = false
            },
            onDismiss = { showEditDialog = false }
        )
    }
}

/**
 * Dialog for editing share message
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EditMessageDialog(
    initialText: String,
    onConfirm: (String) -> Unit,
    onDismiss: () -> Unit
) {
    var text by remember { mutableStateOf(initialText) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Edit Share Message") },
        text = {
            TextField(
                value = text,
                onValueChange = { text = it },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(200.dp),
                label = { Text("Message") },
                maxLines = 8
            )
        },
        confirmButton = {
            TextButton(
                onClick = {
                    onConfirm(text)
                }
            ) {
                Text("Save")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}

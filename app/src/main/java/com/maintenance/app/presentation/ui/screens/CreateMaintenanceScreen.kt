package com.maintenance.app.presentation.ui.screens

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.maintenance.app.presentation.ui.components.MainScaffold

/**
 * Create maintenance screen placeholder - to be implemented later.
 */
@Composable
fun CreateMaintenanceScreen(
    recordId: Long,
    navController: NavController
) {
    MainScaffold(
        title = "Crear Mantenimiento",
        navController = navController,
        showBottomBar = false,
        showBackButton = true,
        onBackClick = { navController.navigateUp() }
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(16.dp),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = "Crear Mantenimiento para Record ID: $recordId\n(Por implementar)",
                style = MaterialTheme.typography.headlineSmall
            )
        }
    }
}
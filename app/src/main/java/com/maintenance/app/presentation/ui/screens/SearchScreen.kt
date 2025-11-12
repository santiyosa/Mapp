package com.maintenance.app.presentation.ui.screens

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.maintenance.app.R
import com.maintenance.app.presentation.ui.components.MainScaffold

/**
 * Search screen placeholder - to be implemented later.
 */
@Composable
fun SearchScreen(
    navController: NavController
) {
    MainScaffold(
        title = stringResource(R.string.nav_search),
        navController = navController,
        showBottomBar = true
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(16.dp),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = "Búsqueda - Por implementar",
                style = MaterialTheme.typography.headlineSmall
            )
        }
    }
}
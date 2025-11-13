package com.maintenance.app.presentation.ui.components

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.maintenance.app.R
import com.maintenance.app.presentation.navigation.MainBottomNavigation

/**
 * Main scaffold wrapper that provides consistent layout structure.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainScaffold(
    title: String = "",
    navController: NavController? = null,
    showBottomBar: Boolean = true,
    showBackButton: Boolean = false,
    onBackClick: (() -> Unit)? = null,
    actions: @Composable () -> Unit = {},
    floatingActionButton: @Composable () -> Unit = {},
    snackbarHost: @Composable () -> Unit = {},
    content: @Composable (PaddingValues) -> Unit
) {
    Scaffold(
        topBar = {
            if (title.isNotEmpty() || showBackButton) {
                TopAppBar(
                    title = {
                        Text(
                            text = title,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    },
                    navigationIcon = {
                        if (showBackButton) {
                            IconButton(
                                onClick = onBackClick ?: {}
                            ) {
                                Icon(
                                    imageVector = Icons.Default.ArrowBack,
                                    contentDescription = stringResource(R.string.nav_back)
                                )
                            }
                        }
                    },
                    actions = { actions() }
                )
            }
        },
        bottomBar = {
            if (showBottomBar && navController != null) {
                MainBottomNavigation(navController = navController)
            }
        },
        floatingActionButton = floatingActionButton,
        snackbarHost = snackbarHost,
        content = content
    )
}

/**
 * Loading state composable.
 */
@Composable
fun LoadingState(
    @Suppress("UNUSED_PARAMETER") message: String = "Loading..."
) {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        CircularProgressIndicator()
    }
}

/**
 * Error state composable.
 */
@Composable
fun ErrorState(
    message: String,
    onRetry: (() -> Unit)? = null
) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        contentAlignment = Alignment.Center
    ) {
        Card {
            Column(
                modifier = Modifier.padding(16.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = message,
                    style = MaterialTheme.typography.bodyMedium
                )
                if (onRetry != null) {
                    Spacer(modifier = Modifier.height(16.dp))
                    Button(onClick = onRetry) {
                        Text(stringResource(R.string.retry))
                    }
                }
            }
        }
    }
}

/**
 * Empty state composable.
 */
@Composable
fun EmptyState(
    message: String,
    actionText: String? = null,
    onActionClick: (() -> Unit)? = null
) {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Card {
            Column(
                modifier = Modifier.padding(16.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = message,
                    style = MaterialTheme.typography.bodyMedium
                )
                if (actionText != null && onActionClick != null) {
                    Spacer(modifier = Modifier.height(16.dp))
                    Button(onClick = onActionClick) {
                        Text(actionText)
                    }
                }
            }
        }
    }
}
package com.maintenance.app.presentation.ui.screens.search

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.background
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.FilterList
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.maintenance.app.presentation.navigation.Screen
import com.maintenance.app.presentation.ui.components.LoadingIndicator
import com.maintenance.app.presentation.ui.components.MaintenanceTextField
import com.maintenance.app.presentation.ui.components.MainScaffold
import com.maintenance.app.presentation.viewmodels.search.SearchViewModel
import com.maintenance.app.presentation.viewmodels.search.SortOption
import com.maintenance.app.presentation.viewmodels.search.AdvancedSearchFilters
import java.math.BigDecimal
import java.time.LocalDate

/**
 * Advanced search screen with filters
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SearchScreenAdvanced(
    navController: NavController,
    viewModel: SearchViewModel = hiltViewModel()
) {
    val searchQuery by viewModel.searchQuery.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()
    val searchResults by viewModel.searchResults.collectAsState()
    val isAdvancedExpanded by viewModel.isAdvancedSearchExpanded.collectAsState()
    val advancedFilters by viewModel.advancedFilters.collectAsState()
    
    var showFilters by remember { mutableStateOf(false) }

    MainScaffold(
        title = "Buscar",
        navController = navController,
        showBottomBar = true,
        showBackButton = false
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            // Search bar
            SearchBar(
                query = searchQuery,
                onQueryChange = { viewModel.updateSearchQuery(it) },
                onSearch = { viewModel.executeSearch(it) },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(8.dp)
            )

            // Filter button
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 8.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                FilterButton(
                    onClick = { showFilters = !showFilters },
                    isActive = !advancedFilters.isEmpty(),
                    modifier = Modifier.weight(1f)
                )
                
                if (!advancedFilters.isEmpty()) {
                    OutlinedButton(
                        onClick = { viewModel.clearFilters() },
                        modifier = Modifier.weight(1f)
                    ) {
                        Text("Limpiar filtros")
                    }
                }
            }

            // Advanced filters panel
            if (showFilters) {
                AdvancedFiltersPanel(
                    filters = advancedFilters,
                    onFiltersChanged = { viewModel.applyFilters(it) },
                    onSortChanged = { viewModel.updateSortBy(it) },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(8.dp)
                        .weight(1f)
                )
            } else {
                // Search results
                when {
                    isLoading -> {
                        LoadingIndicator(
                            modifier = Modifier.weight(1f)
                        )
                    }
                    searchResults.isEmpty() -> {
                        Text(
                            text = "No se encontraron resultados",
                            modifier = Modifier
                                .weight(1f)
                                .padding(16.dp),
                            style = MaterialTheme.typography.bodyMedium
                        )
                    }
                    else -> {
                        LazyColumn(
                            modifier = Modifier
                                .weight(1f)
                                .fillMaxWidth(),
                            contentPadding = PaddingValues(8.dp),
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            items(searchResults.size) { index ->
                                val result = searchResults[index]
                                SearchResultCard(
                                    result = result,
                                    onClick = {
                                        when (result.type) {
                                            com.maintenance.app.domain.model.SearchResultType.RECORD -> {
                                                navController.navigate(
                                                    Screen.RecordDetail.createRoute(result.id)
                                                )
                                            }
                                            com.maintenance.app.domain.model.SearchResultType.MAINTENANCE -> {
                                                // For maintenance, we could navigate to edit or show details
                                                // For now, let's navigate to the maintenance edit screen
                                                navController.navigate(
                                                    Screen.EditMaintenance.createRoute(result.id)
                                                )
                                            }
                                        }
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

@Composable
private fun SearchBar(
    query: String,
    onQueryChange: (String) -> Unit,
    onSearch: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    OutlinedTextField(
        value = query,
        onValueChange = onQueryChange,
        modifier = modifier.height(50.dp),
        placeholder = { Text("Buscar registros...") },
        leadingIcon = {
            Icon(Icons.Default.Search, contentDescription = "Buscar")
        },
        trailingIcon = {
            if (query.isNotEmpty()) {
                IconButton(onClick = { onQueryChange("") }) {
                    Icon(Icons.Default.Close, contentDescription = "Clear")
                }
            }
        },
        singleLine = true,
        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Text)
    )
}

@Composable
private fun FilterButton(
    onClick: () -> Unit,
    isActive: Boolean,
    modifier: Modifier = Modifier
) {
    OutlinedButton(
        onClick = onClick,
        modifier = modifier,
        colors = ButtonDefaults.outlinedButtonColors(
            containerColor = if (isActive) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.background
        )
    ) {
        Icon(
            Icons.Default.FilterList,
            contentDescription = "Filters",
            modifier = Modifier.size(18.dp)
        )
        Spacer(modifier = Modifier.width(4.dp))
        Text("Filters")
    }
}

@Composable
private fun AdvancedFiltersPanel(
    filters: AdvancedSearchFilters,
    onFiltersChanged: (AdvancedSearchFilters) -> Unit,
    onSortChanged: (SortOption) -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .verticalScroll(rememberScrollState())
            .background(MaterialTheme.colorScheme.surfaceVariant, MaterialTheme.shapes.medium)
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Text(
            "Advanced Filters",
            style = MaterialTheme.typography.titleMedium
        )

        // Cost range filter
        Text("Cost Range", style = MaterialTheme.typography.labelMedium)
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            MaintenanceTextField(
                value = filters.minCost?.toString() ?: "",
                onValueChange = { value ->
                    val minCost = value.toBigDecimalOrNull()
                    onFiltersChanged(filters.copy(minCost = minCost))
                },
                label = "Min Cost",
                placeholder = "0",
                isError = false,
                keyboardType = KeyboardType.Decimal,
                modifier = Modifier.weight(1f)
            )
            MaintenanceTextField(
                value = filters.maxCost?.toString() ?: "",
                onValueChange = { value ->
                    val maxCost = value.toBigDecimalOrNull()
                    onFiltersChanged(filters.copy(maxCost = maxCost))
                },
                label = "Max Cost",
                placeholder = "9999",
                isError = false,
                keyboardType = KeyboardType.Decimal,
                modifier = Modifier.weight(1f)
            )
        }

        // Sort option
        Text("Sort By", style = MaterialTheme.typography.labelMedium)
        @OptIn(ExperimentalMaterial3Api::class)
        @Suppress("DEPRECATION_ERROR")
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .horizontalScroll(rememberScrollState()),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            SortOption.entries.forEach { sort ->
                FilterChip(
                    selected = filters.sortBy == sort,
                    onClick = { onSortChanged(sort) },
                    label = { Text(sort.label) }
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Suppress("DEPRECATION")
@Composable
private fun SearchResultCard(
    result: com.maintenance.app.domain.model.SearchResult,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier
            .fillMaxWidth(),
        onClick = onClick
    ) {
        Column(
            modifier = Modifier.padding(12.dp)
        ) {
            Text(
                text = result.title,
                style = MaterialTheme.typography.titleSmall
            )
            Text(
                text = result.subtitle,
                style = MaterialTheme.typography.bodySmall,
                modifier = Modifier.padding(top = 4.dp)
            )
            Text(
                text = result.description,
                style = MaterialTheme.typography.labelSmall,
                modifier = Modifier.padding(top = 4.dp)
            )
        }
    }
}

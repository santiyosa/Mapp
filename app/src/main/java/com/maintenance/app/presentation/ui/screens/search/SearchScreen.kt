package com.maintenance.app.presentation.ui.screens.search

import androidx.compose.animation.*
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import kotlinx.coroutines.FlowPreview
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.maintenance.app.domain.model.SearchResult
import com.maintenance.app.domain.model.SearchResultType
import com.maintenance.app.domain.model.SearchSuggestion
import com.maintenance.app.domain.model.SearchHistoryEntry
import com.maintenance.app.domain.model.SearchCriteria
import com.maintenance.app.presentation.viewmodels.search.SearchViewModel
import com.maintenance.app.presentation.ui.screens.search.components.*

/**
 * Main search screen with input, filters, suggestions, and results.
 */
@OptIn(ExperimentalMaterial3Api::class, ExperimentalFoundationApi::class, FlowPreview::class, ExperimentalComposeUiApi::class)
@Composable
fun SearchScreen(
    onNavigateToRecord: (Long) -> Unit,
    onNavigateToMaintenance: (Long) -> Unit,
    onNavigateBack: () -> Unit,
    viewModel: SearchViewModel = hiltViewModel()
) {
    val searchQuery by viewModel.searchQuery.collectAsStateWithLifecycle()
    val searchResults by viewModel.searchResults.collectAsStateWithLifecycle()
    val suggestions by viewModel.suggestions.collectAsStateWithLifecycle()
    val searchHistory by viewModel.searchHistory.collectAsStateWithLifecycle()
    val isLoading by viewModel.isLoading.collectAsStateWithLifecycle()
    val hasResults by viewModel.hasResults.collectAsStateWithLifecycle()
    val hasQuery by viewModel.hasQuery.collectAsStateWithLifecycle()
    val errorMessage by viewModel.errorMessage.collectAsStateWithLifecycle()
    val isAdvancedSearchExpanded by viewModel.isAdvancedSearchExpanded.collectAsStateWithLifecycle()
    val advancedFilters by viewModel.advancedFilters.collectAsStateWithLifecycle()

    val focusRequester = remember { FocusRequester() }
    val keyboardController = LocalSoftwareKeyboardController.current

    // Show error message as snackbar
    LaunchedEffect(errorMessage) {
        errorMessage?.let {
            // Handle error display
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        // Top App Bar
        TopAppBar(
            title = { 
                Text(
                    text = "Search",
                    style = MaterialTheme.typography.headlineSmall
                ) 
            },
            navigationIcon = {
                IconButton(onClick = onNavigateBack) {
                    Icon(
                        imageVector = Icons.Default.ArrowBack,
                        contentDescription = "Back"
                    )
                }
            },
            actions = {
                // Clear search action
                if (hasQuery) {
                    IconButton(onClick = { viewModel.clearSearch() }) {
                        Icon(
                            imageVector = Icons.Default.Clear,
                            contentDescription = "Clear search"
                        )
                    }
                }
                
                // Advanced search toggle
                IconButton(onClick = { viewModel.toggleAdvancedSearch() }) {
                    Icon(
                        imageVector = if (isAdvancedSearchExpanded) Icons.Outlined.FilterList else Icons.Default.FilterList,
                        contentDescription = "Toggle advanced search",
                        tint = if (isAdvancedSearchExpanded) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface
                    )
                }
            },
            colors = TopAppBarDefaults.topAppBarColors(
                containerColor = MaterialTheme.colorScheme.surface
            )
        )

        // Search Input Section
        SearchInputSection(
            query = searchQuery,
            onQueryChange = viewModel::onSearchQueryChange,
            onSearchClick = { viewModel.executeSearch() },
            suggestions = suggestions,
            onSuggestionClick = viewModel::onSuggestionSelected,
            focusRequester = focusRequester,
            keyboardController = keyboardController,
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 8.dp)
        )

        // Advanced Search Filters
        AnimatedVisibility(
            visible = isAdvancedSearchExpanded,
            enter = expandVertically() + fadeIn(),
            exit = shrinkVertically() + fadeOut()
        ) {
            AdvancedSearchFilters(
                filters = advancedFilters,
                onFiltersChange = viewModel::updateAdvancedFilters,
                onSearchClick = { viewModel.executeAdvancedSearch() },
                onClearClick = { viewModel.clearAdvancedFilters() },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp)
            )
        }

        // Loading Indicator
        if (isLoading) {
            LinearProgressIndicator(
                modifier = Modifier.fillMaxWidth()
            )
        }

        // Content
        when {
            // Show search results
            hasResults -> {
                SearchResultsList(
                    results = searchResults,
                    onRecordClick = onNavigateToRecord,
                    onMaintenanceClick = onNavigateToMaintenance,
                    modifier = Modifier.fillMaxSize()
                )
            }
            
            // Show search history when no query
            !hasQuery && searchHistory.isNotEmpty() -> {
                SearchHistorySection(
                    history = searchHistory,
                    onHistoryItemClick = viewModel::onHistoryItemSelected,
                    onClearHistoryClick = { viewModel.clearSearchHistory() },
                    modifier = Modifier.fillMaxSize()
                )
            }
            
            // Show empty state
            hasQuery && !isLoading -> {
                EmptySearchResults(
                    query = searchQuery,
                    modifier = Modifier.fillMaxSize()
                )
            }
            
            // Show welcome message
            else -> {
                SearchWelcomeMessage(
                    modifier = Modifier.fillMaxSize()
                )
            }
        }
    }

    // Focus search input on first composition
    LaunchedEffect(Unit) {
        focusRequester.requestFocus()
    }
}

@OptIn(ExperimentalMaterial3Api::class, ExperimentalComposeUiApi::class)
@Composable
private fun SearchInputSection(
    query: String,
    onQueryChange: (String) -> Unit,
    onSearchClick: () -> Unit,
    suggestions: List<SearchSuggestion>,
    onSuggestionClick: (SearchSuggestion) -> Unit,
    focusRequester: FocusRequester,
    keyboardController: androidx.compose.ui.platform.SoftwareKeyboardController?,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier,
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            // Search TextField
            OutlinedTextField(
                value = query,
                onValueChange = onQueryChange,
                label = { Text("Search records and maintenances...") },
                leadingIcon = {
                    Icon(
                        imageVector = Icons.Default.Search,
                        contentDescription = "Search"
                    )
                },
                trailingIcon = {
                    if (query.isNotBlank()) {
                        IconButton(onClick = { onQueryChange("") }) {
                            Icon(
                                imageVector = Icons.Default.Clear,
                                contentDescription = "Clear"
                            )
                        }
                    }
                },
                keyboardOptions = KeyboardOptions(
                    imeAction = ImeAction.Search
                ),
                keyboardActions = KeyboardActions(
                    onSearch = { 
                        onSearchClick()
                        keyboardController?.hide()
                    }
                ),
                singleLine = true,
                modifier = Modifier
                    .fillMaxWidth()
                    .focusRequester(focusRequester)
            )

            // Suggestions
            if (suggestions.isNotEmpty() && query.isNotBlank()) {
                Spacer(modifier = Modifier.height(8.dp))
                
                LazyColumn(
                    modifier = Modifier.heightIn(max = 200.dp)
                ) {
                    items(suggestions) { suggestion ->
                        SuggestionItem(
                            suggestion = suggestion,
                            onClick = { onSuggestionClick(suggestion) },
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun SuggestionItem(
    suggestion: SearchSuggestion,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .clickable { onClick() }
            .padding(horizontal = 16.dp, vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = when (suggestion.type) {
                com.maintenance.app.domain.model.SuggestionType.RECORD_NAME -> Icons.Default.Note
                com.maintenance.app.domain.model.SuggestionType.MAINTENANCE_TYPE -> Icons.Default.Build
                com.maintenance.app.domain.model.SuggestionType.PERFORMER -> Icons.Default.Person
                com.maintenance.app.domain.model.SuggestionType.LOCATION -> Icons.Default.LocationOn
                com.maintenance.app.domain.model.SuggestionType.HISTORY -> Icons.Default.Schedule
            },
            contentDescription = null,
            tint = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.size(20.dp)
        )
        
        Spacer(modifier = Modifier.width(12.dp))
        
        Text(
            text = suggestion.text,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurface,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
            modifier = Modifier.weight(1f)
        )
        
        if (suggestion.count > 0) {
            Text(
                text = suggestion.count.toString(),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier
                    .background(
                        MaterialTheme.colorScheme.surfaceVariant,
                        RoundedCornerShape(12.dp)
                    )
                    .padding(horizontal = 8.dp, vertical = 2.dp)
            )
        }
    }
}
package com.maintenance.app.presentation.viewmodels.search

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.maintenance.app.domain.model.SearchCriteria
import com.maintenance.app.domain.model.SearchHistoryEntry
import com.maintenance.app.domain.model.SearchResult
import com.maintenance.app.domain.model.SearchResultType
import com.maintenance.app.domain.model.SearchSuggestion
import com.maintenance.app.domain.usecases.search.FullTextSearchUseCase
import com.maintenance.app.domain.usecases.search.AdvancedSearchUseCase
import com.maintenance.app.domain.usecases.search.GetSearchSuggestionsUseCase
import com.maintenance.app.domain.usecases.search.SearchHistoryUseCase
import com.maintenance.app.domain.usecases.search.GetFilterOptionsUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * ViewModel for managing search functionality.
 * Handles search queries, filters, history, and suggestions with debounced input.
 */
@OptIn(FlowPreview::class, ExperimentalCoroutinesApi::class)
@HiltViewModel
class SearchViewModel @Inject constructor(
    private val fullTextSearchUseCase: FullTextSearchUseCase,
    private val advancedSearchUseCase: AdvancedSearchUseCase,
    private val getSuggestionsUseCase: GetSearchSuggestionsUseCase,
    private val searchHistoryUseCase: SearchHistoryUseCase,
    private val getFilterOptionsUseCase: GetFilterOptionsUseCase
) : ViewModel() {

    companion object {
        private const val SEARCH_DEBOUNCE_TIME_MS = 300L
        private const val MIN_QUERY_LENGTH = 2
        private const val DEFAULT_RESULTS_LIMIT = 50
    }

    // Private mutable states
    private val _searchQuery = MutableStateFlow("")
    private val _isLoading = MutableStateFlow(false)
    private val _searchResults = MutableStateFlow<List<SearchResult>>(emptyList())
    private val _suggestions = MutableStateFlow<List<SearchSuggestion>>(emptyList())
    private val _searchHistory = MutableStateFlow<List<SearchHistoryEntry>>(emptyList())
    private val _errorMessage = MutableStateFlow<String?>(null)
    private val _isAdvancedSearchExpanded = MutableStateFlow(false)
    private val _advancedFilters = MutableStateFlow(SearchCriteria())

    // Public read-only states
    val searchQuery: StateFlow<String> = _searchQuery.asStateFlow()
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()
    val searchResults: StateFlow<List<SearchResult>> = _searchResults.asStateFlow()
    val suggestions: StateFlow<List<SearchSuggestion>> = _suggestions.asStateFlow()
    val searchHistory: StateFlow<List<SearchHistoryEntry>> = _searchHistory.asStateFlow()
    val errorMessage: StateFlow<String?> = _errorMessage.asStateFlow()
    val isAdvancedSearchExpanded: StateFlow<Boolean> = _isAdvancedSearchExpanded.asStateFlow()
    val advancedFilters: StateFlow<SearchCriteria> = _advancedFilters.asStateFlow()

    // Derived states
    val hasResults: StateFlow<Boolean> = _searchResults.map { it.isNotEmpty() }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = false
    )

    val hasQuery: StateFlow<Boolean> = _searchQuery.map { it.isNotBlank() }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = false
    )

    init {
        setupSearchFlow()
        loadSearchHistory()
    }

    /**
     * Set up debounced search flow.
     */
    private fun setupSearchFlow() {
        _searchQuery
            .debounce(SEARCH_DEBOUNCE_TIME_MS)
            .distinctUntilChanged()
            .filter { it.length >= MIN_QUERY_LENGTH }
            .onEach { _ ->
                _isLoading.value = true
                _errorMessage.value = null
            }
            .flatMapLatest { query ->
                fullTextSearchUseCase.executeAsFlow(query, DEFAULT_RESULTS_LIMIT)
                    .catch { exception ->
                        _errorMessage.value = exception.message ?: "Search failed"
                        emit(emptyList())
                    }
            }
            .onEach { results ->
                _searchResults.value = results
                _isLoading.value = false
                
                // Save successful search to history if has results
                if (results.isNotEmpty() && _searchQuery.value.isNotBlank()) {
                    saveSearchToHistory(_searchQuery.value, results.size)
                }
            }
            .launchIn(viewModelScope)
    }

    /**
     * Update search query.
     */
    fun onSearchQueryChange(query: String) {
        _searchQuery.value = query
        
        // Clear results for short queries
        if (query.length < MIN_QUERY_LENGTH) {
            _searchResults.value = emptyList()
            _isLoading.value = false
        }
        
        // Load suggestions for valid queries
        if (query.length >= MIN_QUERY_LENGTH) {
            loadSuggestions(query)
        } else {
            _suggestions.value = emptyList()
        }
    }

    /**
     * Execute search immediately (bypass debounce).
     */
    fun executeSearch(query: String = _searchQuery.value) {
        if (query.isBlank()) return
        
        _searchQuery.value = query
        viewModelScope.launch {
            try {
                _isLoading.value = true
                _errorMessage.value = null
                
                val result = fullTextSearchUseCase.invoke(
                    FullTextSearchUseCase.Params(query, DEFAULT_RESULTS_LIMIT)
                )
                
                val results = when (result) {
                    is com.maintenance.app.utils.Result.Success -> result.data
                    is com.maintenance.app.utils.Result.Error -> {
                        throw result.exception ?: Exception(result.message)
                    }
                    is com.maintenance.app.utils.Result.Loading -> emptyList()
                }
                
                _searchResults.value = results
                saveSearchToHistory(query, results.size)
                
            } catch (exception: Exception) {
                _errorMessage.value = exception.message ?: "Search failed"
                _searchResults.value = emptyList()
            } finally {
                _isLoading.value = false
            }
        }
    }

    /**
     * Execute advanced search with filters.
     */
    fun executeAdvancedSearch() {
        viewModelScope.launch {
            try {
                _isLoading.value = true
                _errorMessage.value = null
                
                val criteria = _advancedFilters.value
                if (criteria.isEmpty()) {
                    _errorMessage.value = "Please specify search criteria"
                    return@launch
                }
                
                val result = advancedSearchUseCase.invoke(
                    AdvancedSearchUseCase.Params(criteria)
                )
                
                // Convert Records to SearchResults since AdvancedSearchUseCase returns List<Record>
                val records = when (result) {
                    is com.maintenance.app.utils.Result.Success -> result.data
                    is com.maintenance.app.utils.Result.Error -> {
                        throw result.exception ?: Exception(result.message)
                    }
                    is com.maintenance.app.utils.Result.Loading -> emptyList()
                }
                
                // Map Records to SearchResults
                val results = records.map { record ->
                    SearchResult(
                        type = SearchResultType.RECORD,
                        id = record.id,
                        title = record.name,
                        subtitle = record.brandModel ?: "",
                        description = record.description ?: "",
                        relevanceScore = 1.0f
                    )
                }
                
                _searchResults.value = results
                
                // Save advanced search to history
                saveAdvancedSearchToHistory(criteria, results.size)
                
            } catch (exception: Exception) {
                _errorMessage.value = exception.message ?: "Advanced search failed"
                _searchResults.value = emptyList()
            } finally {
                _isLoading.value = false
            }
        }
    }

    /**
     * Load search suggestions.
     */
    private fun loadSuggestions(query: String) {
        viewModelScope.launch {
            try {
                val result = getSuggestionsUseCase.invoke(
                    GetSearchSuggestionsUseCase.Params(query)
                )
                
                val suggestions = when (result) {
                    is com.maintenance.app.utils.Result.Success -> result.data
                    is com.maintenance.app.utils.Result.Error -> emptyList()
                    is com.maintenance.app.utils.Result.Loading -> emptyList()
                }
                _suggestions.value = suggestions
            } catch (exception: Exception) {
                // Silently fail for suggestions
                _suggestions.value = emptyList()
            }
        }
    }

    /**
     * Load search history.
     */
    private fun loadSearchHistory() {
        viewModelScope.launch {
            searchHistoryUseCase.getHistoryFlow()
                .catch { _ ->
                    // Silently fail for history
                }
                .collect { history ->
                    _searchHistory.value = history
                }
        }
    }

    /**
     * Save search to history.
     */
    private fun saveSearchToHistory(query: String, resultCount: Int) {
        viewModelScope.launch {
            try {
                val historyEntry = SearchHistoryEntry(
                    query = query,
                    criteria = SearchCriteria(query = query),
                    resultCount = resultCount,
                    timestamp = System.currentTimeMillis()
                )
                searchHistoryUseCase.saveSearch(historyEntry)
            } catch (exception: Exception) {
                // Silently fail for history saving
            }
        }
    }

    /**
     * Save advanced search to history.
     */
    private fun saveAdvancedSearchToHistory(criteria: SearchCriteria, resultCount: Int) {
        viewModelScope.launch {
            try {
                val queryDescription = buildAdvancedSearchDescription(criteria)
                val historyEntry = SearchHistoryEntry(
                    query = queryDescription,
                    criteria = criteria,
                    resultCount = resultCount,
                    timestamp = System.currentTimeMillis()
                )
                searchHistoryUseCase.saveSearch(historyEntry)
            } catch (exception: Exception) {
                // Silently fail for history saving
            }
        }
    }

    /**
     * Select suggestion.
     */
    fun onSuggestionSelected(suggestion: SearchSuggestion) {
        _searchQuery.value = suggestion.text
        executeSearch(suggestion.text)
        _suggestions.value = emptyList()
    }

    /**
     * Select search from history.
     */
    fun onHistoryItemSelected(historyEntry: SearchHistoryEntry) {
        _searchQuery.value = historyEntry.query
        executeSearch(historyEntry.query)
    }

    /**
     * Clear search query and results.
     */
    fun clearSearch() {
        _searchQuery.value = ""
        _searchResults.value = emptyList()
        _suggestions.value = emptyList()
        _errorMessage.value = null
        _isLoading.value = false
    }

    /**
     * Toggle advanced search expansion.
     */
    fun toggleAdvancedSearch() {
        _isAdvancedSearchExpanded.value = !_isAdvancedSearchExpanded.value
    }

    /**
     * Update advanced filters.
     */
    fun updateAdvancedFilters(filters: SearchCriteria) {
        _advancedFilters.value = filters
    }

    /**
     * Clear advanced filters.
     */
    fun clearAdvancedFilters() {
        _advancedFilters.value = SearchCriteria()
    }

    /**
     * Clear search history.
     */
    fun clearSearchHistory() {
        viewModelScope.launch {
            try {
                searchHistoryUseCase.clearHistory()
                _searchHistory.value = emptyList()
            } catch (exception: Exception) {
                _errorMessage.value = "Failed to clear history"
            }
        }
    }

    /**
     * Dismiss error message.
     */
    fun dismissError() {
        _errorMessage.value = null
    }

    /**
     * Build description for advanced search criteria.
     */
    private fun buildAdvancedSearchDescription(criteria: SearchCriteria): String {
        val parts = mutableListOf<String>()
        
        if (criteria.query.isNotBlank()) { parts.add("Query: ${criteria.query}") }
        criteria.minCost?.let { parts.add("Min Cost: $it") }
        criteria.maxCost?.let { parts.add("Max Cost: $it") }
        criteria.startDate?.let { parts.add("From: ${it}") }
        criteria.endDate?.let { parts.add("To: ${it}") }
        
        return if (parts.isNotEmpty()) {
            "Advanced: ${parts.joinToString(", ")}"
        } else {
            "Advanced Search"
        }
    }

    /**
     * Get filter options for advanced search.
     */
    fun getFilterOptions() {
        viewModelScope.launch {
            try {
                val result = getFilterOptionsUseCase.invoke(Unit)
                
                @Suppress("UNUSED_EXPRESSION")
                when (result) {
                    is com.maintenance.app.utils.Result.Success -> result.data
                    is com.maintenance.app.utils.Result.Error -> null
                    is com.maintenance.app.utils.Result.Loading -> null
                }
                // Handle filter options if needed
            } catch (exception: Exception) {
                // Silently fail
            }
        }
    }
}
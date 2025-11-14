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
import java.math.BigDecimal
import java.time.LocalDate
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
    private val _advancedFilters = MutableStateFlow(AdvancedSearchFilters())

    // Public read-only states
    val searchQuery: StateFlow<String> = _searchQuery.asStateFlow()
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()
    val searchResults: StateFlow<List<SearchResult>> = _searchResults.asStateFlow()
    val suggestions: StateFlow<List<SearchSuggestion>> = _suggestions.asStateFlow()
    val searchHistory: StateFlow<List<SearchHistoryEntry>> = _searchHistory.asStateFlow()
    val errorMessage: StateFlow<String?> = _errorMessage.asStateFlow()
    val isAdvancedSearchExpanded: StateFlow<Boolean> = _isAdvancedSearchExpanded.asStateFlow()
    val advancedFilters: StateFlow<AdvancedSearchFilters> = _advancedFilters.asStateFlow()

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
    fun updateSearchQuery(query: String) {
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
     * Load search suggestions.
     */
    private fun loadSuggestions(query: String) {
        viewModelScope.launch {
            try {
                val result = getSuggestionsUseCase.invoke(
                    GetSearchSuggestionsUseCase.Params(query)
                )
                
                _suggestions.value = when (result) {
                    is com.maintenance.app.utils.Result.Success -> result.data
                    is com.maintenance.app.utils.Result.Error -> emptyList()
                    is com.maintenance.app.utils.Result.Loading -> emptyList()
                    else -> emptyList()
                }
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
            try {
                val history = searchHistoryUseCase.getHistory()
                _searchHistory.value = history
            } catch (exception: Exception) {
                _searchHistory.value = emptyList()
            }
        }
    }

    /**
     * Save search to history.
     */
    private fun saveSearchToHistory(query: String, resultCount: Int) {
        viewModelScope.launch {
            try {
                val entry = com.maintenance.app.domain.model.SearchHistoryEntry(
                    query = query,
                    criteria = SearchCriteria(),
                    resultCount = resultCount
                )
                searchHistoryUseCase.saveSearch(entry)
            } catch (exception: Exception) {
                // Silently fail
            }
        }
    }

    /**
     * Clear search query.
     */
    fun clearSearch() {
        _searchQuery.value = ""
        _searchResults.value = emptyList()
        _suggestions.value = emptyList()
        _errorMessage.value = null
        _isLoading.value = false
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

    // Advanced filters methods
    
    /**
     * Toggle advanced search expansion.
     */
    fun toggleAdvancedSearch() {
        _isAdvancedSearchExpanded.value = !_isAdvancedSearchExpanded.value
    }

    /**
     * Apply filters to search.
     */
    fun applyFilters(filters: AdvancedSearchFilters) {
        _advancedFilters.value = filters
        // Execute search with applied filters
        executeAdvancedSearch(filters)
    }

    /**
     * Execute advanced search with filters.
     */
    private fun executeAdvancedSearch(filters: AdvancedSearchFilters) {
        viewModelScope.launch {
            try {
                _isLoading.value = true
                _errorMessage.value = null
                
                if (filters.isEmpty()) {
                    _searchResults.value = emptyList()
                    return@launch
                }
                
                // For now, perform basic search - extend as needed
                val results = _searchResults.value
                val filtered = sortResults(results, filters.sortBy)
                
                _searchResults.value = filtered
                
            } catch (exception: Exception) {
                _errorMessage.value = exception.message ?: "Advanced search failed"
                _searchResults.value = emptyList()
            } finally {
                _isLoading.value = false
            }
        }
    }

    /**
     * Clear all filters.
     */
    fun clearFilters() {
        _advancedFilters.value = AdvancedSearchFilters()
        _searchResults.value = emptyList()
    }

    /**
     * Sort search results.
     */
    private fun sortResults(results: List<SearchResult>, sortOption: SortOption): List<SearchResult> {
        return when (sortOption) {
            SortOption.RELEVANCE -> results.sortedByDescending { it.relevanceScore }
            SortOption.NAME -> results.sortedBy { it.title.lowercase() }
            SortOption.DATE_NEWEST -> results  // Placeholder
            SortOption.DATE_OLDEST -> results  // Placeholder
            SortOption.COST_HIGH -> results    // Placeholder
            SortOption.COST_LOW -> results     // Placeholder
        }
    }

    /**
     * Update cost range filter.
     */
    fun updateCostRange(minCost: BigDecimal?, maxCost: BigDecimal?) {
        val current = _advancedFilters.value
        _advancedFilters.value = current.copy(minCost = minCost, maxCost = maxCost)
    }

    /**
     * Update date range filter.
     */
    fun updateDateRange(startDate: LocalDate?, endDate: LocalDate?) {
        val current = _advancedFilters.value
        _advancedFilters.value = current.copy(startDate = startDate, endDate = endDate)
    }

    /**
     * Update category filter.
     */
    fun updateCategoryFilter(categories: List<String>) {
        val current = _advancedFilters.value
        _advancedFilters.value = current.copy(categories = categories)
    }

    /**
     * Update sort option.
     */
    fun updateSortBy(sortOption: SortOption) {
        val current = _advancedFilters.value
        _advancedFilters.value = current.copy(sortBy = sortOption)
    }
}

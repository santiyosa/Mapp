package com.maintenance.app.domain.usecases.search

import com.maintenance.app.domain.model.SearchCriteria
import com.maintenance.app.domain.model.SearchHistoryEntry
import com.maintenance.app.domain.model.SearchResult
import com.maintenance.app.domain.repository.SearchRepository
import com.maintenance.app.domain.usecases.base.UseCase
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

/**
 * Use case for performing full-text search.
 */
class FullTextSearchUseCase @Inject constructor(
    private val searchRepository: SearchRepository
) : UseCase<FullTextSearchUseCase.Params, List<SearchResult>>() {

    override suspend fun execute(parameters: Params): List<SearchResult> {
        return searchRepository.fullTextSearch(parameters.query, parameters.limit)
    }

    /**
     * Get search results as Flow for real-time updates.
     */
    fun executeAsFlow(query: String, limit: Int = 50): Flow<List<SearchResult>> {
        return searchRepository.fullTextSearchFlow(query, limit)
    }

    /**
     * Get search results for a specific record as Flow for real-time updates.
     */
    fun executeAsFlowByRecordId(recordId: Long, query: String, limit: Int = 50): Flow<List<SearchResult>> {
        return searchRepository.searchMaintenancesByRecordIdFlow(recordId, query, limit)
    }

    data class Params(
        val query: String,
        val limit: Int = 50
    )
}

/**
 * Use case for advanced search with multiple criteria.
 */
class AdvancedSearchUseCase @Inject constructor(
    private val searchRepository: SearchRepository
) : UseCase<AdvancedSearchUseCase.Params, List<com.maintenance.app.domain.model.Record>>() {

    override suspend fun execute(parameters: Params): List<com.maintenance.app.domain.model.Record> {
        // Save search to history if it has meaningful content
        if (!parameters.criteria.isEmpty()) {
            val historyEntry = SearchHistoryEntry(
                query = parameters.criteria.query,
                criteria = parameters.criteria,
                resultCount = 0 // Will be updated after getting results
            )
            searchRepository.saveToHistory(historyEntry)
        }
        
        return searchRepository.advancedSearch(parameters.criteria)
    }

    data class Params(
        val criteria: SearchCriteria
    )
}

/**
 * Use case for getting search suggestions.
 */
class GetSearchSuggestionsUseCase @Inject constructor(
    private val searchRepository: SearchRepository
) : UseCase<GetSearchSuggestionsUseCase.Params, List<com.maintenance.app.domain.model.SearchSuggestion>>() {

    override suspend fun execute(parameters: Params): List<com.maintenance.app.domain.model.SearchSuggestion> {
        return searchRepository.getSearchSuggestions(parameters.query)
    }

    data class Params(
        val query: String
    )
}

/**
 * Use case for getting filter options.
 */
class GetFilterOptionsUseCase @Inject constructor(
    private val searchRepository: SearchRepository
) : UseCase<Unit, GetFilterOptionsUseCase.FilterOptions>() {

    override suspend fun execute(parameters: Unit): FilterOptions {
        return FilterOptions(
            maintenanceTypes = searchRepository.getMaintenanceTypes(),
            performers = searchRepository.getPerformers(),
            locations = searchRepository.getLocations()
        )
    }

    data class FilterOptions(
        val maintenanceTypes: List<String>,
        val performers: List<String>,
        val locations: List<String>
    )
}

/**
 * Use case for managing search history.
 */
class SearchHistoryUseCase @Inject constructor(
    private val searchRepository: SearchRepository
) {
    
    /**
     * Save search to history.
     */
    suspend fun saveSearch(entry: SearchHistoryEntry): Long {
        return searchRepository.saveToHistory(entry)
    }
    
    /**
     * Get search history.
     */
    suspend fun getHistory(limit: Int = 20): List<SearchHistoryEntry> {
        return searchRepository.getSearchHistory(limit)
    }
    
    /**
     * Get search history as Flow.
     */
    fun getHistoryFlow(limit: Int = 20): Flow<List<SearchHistoryEntry>> {
        return searchRepository.getSearchHistoryFlow(limit)
    }
    
    /**
     * Clear all search history.
     */
    suspend fun clearHistory() {
        searchRepository.clearSearchHistory()
    }
    
    /**
     * Delete specific search history entry.
     */
    suspend fun deleteHistoryEntry(entryId: Long) {
        searchRepository.deleteHistoryEntry(entryId)
    }
}

/**
 * Use case for searching records by name.
 */
class SearchRecordsUseCase @Inject constructor(
    private val searchRepository: SearchRepository
) : UseCase<SearchRecordsUseCase.Params, List<com.maintenance.app.domain.model.Record>>() {

    override suspend fun execute(parameters: Params): List<com.maintenance.app.domain.model.Record> {
        return searchRepository.searchRecords(parameters.query, parameters.limit)
    }

    /**
     * Get search results as Flow.
     */
    fun executeAsFlow(query: String, limit: Int = 50): Flow<List<com.maintenance.app.domain.model.Record>> {
        return searchRepository.searchRecordsFlow(query, limit)
    }

    data class Params(
        val query: String,
        val limit: Int = 50
    )
}

/**
 * Use case for searching maintenances.
 */
class SearchMaintenancesUseCase @Inject constructor(
    private val searchRepository: SearchRepository
) : UseCase<SearchMaintenancesUseCase.Params, List<com.maintenance.app.domain.model.Maintenance>>() {

    override suspend fun execute(parameters: Params): List<com.maintenance.app.domain.model.Maintenance> {
        return searchRepository.searchMaintenances(parameters.query, parameters.limit)
    }

    /**
     * Get search results as Flow.
     */
    fun executeAsFlow(query: String, limit: Int = 50): Flow<List<com.maintenance.app.domain.model.Maintenance>> {
        return searchRepository.searchMaintenancesFlow(query, limit)
    }

    data class Params(
        val query: String,
        val limit: Int = 50
    )
}
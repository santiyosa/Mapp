package com.maintenance.app.domain.repository

import com.maintenance.app.domain.model.Maintenance
import com.maintenance.app.domain.model.Record
import com.maintenance.app.domain.model.SearchCriteria
import com.maintenance.app.domain.model.SearchHistoryEntry
import com.maintenance.app.domain.model.SearchResult
import com.maintenance.app.domain.model.SearchSuggestion
import kotlinx.coroutines.flow.Flow

/**
 * Repository interface for search operations.
 */
interface SearchRepository {
    
    /**
     * Perform full-text search across records and maintenances.
     */
    suspend fun fullTextSearch(query: String, limit: Int = 50): List<SearchResult>
    
    /**
     * Full-text search as Flow for real-time results.
     */
    fun fullTextSearchFlow(query: String, limit: Int = 50): Flow<List<SearchResult>>
    
    /**
     * Search maintenances within a specific record.
     */
    suspend fun searchMaintenancesByRecordId(recordId: Long, query: String, limit: Int = 50): List<SearchResult>
    
    /**
     * Search maintenances within a specific record as Flow.
     */
    fun searchMaintenancesByRecordIdFlow(recordId: Long, query: String, limit: Int = 50): Flow<List<SearchResult>>
    
    /**
     * Search records by name.
     */
    suspend fun searchRecords(query: String, limit: Int = 50): List<Record>
    
    /**
     * Search records by name as Flow.
     */
    fun searchRecordsFlow(query: String, limit: Int = 50): Flow<List<Record>>
    
    /**
     * Search maintenances by content.
     */
    suspend fun searchMaintenances(query: String, limit: Int = 50): List<Maintenance>
    
    /**
     * Search maintenances by content as Flow.
     */
    fun searchMaintenancesFlow(query: String, limit: Int = 50): Flow<List<Maintenance>>
    
    /**
     * Advanced search with multiple criteria.
     */
    suspend fun advancedSearch(criteria: SearchCriteria): List<Record>
    
    /**
     * Search maintenances within date range.
     */
    suspend fun searchByDateRange(startDate: Long, endDate: Long, limit: Int = 100): List<Maintenance>
    
    /**
     * Search maintenances within cost range.
     */
    suspend fun searchByCostRange(minCost: Double, maxCost: Double, limit: Int = 100): List<Maintenance>
    
    /**
     * Search maintenances by type.
     */
    suspend fun searchByType(type: String, limit: Int = 50): List<Maintenance>
    
    /**
     * Get search suggestions for autocomplete.
     */
    suspend fun getSearchSuggestions(query: String): List<SearchSuggestion>
    
    /**
     * Get maintenance types for filters.
     */
    suspend fun getMaintenanceTypes(): List<String>
    
    /**
     * Get performers for filters.
     */
    suspend fun getPerformers(): List<String>
    
    /**
     * Get locations for filters.
     */
    suspend fun getLocations(): List<String>
    
    // Search History Operations
    
    /**
     * Save search to history.
     */
    suspend fun saveToHistory(entry: SearchHistoryEntry): Long
    
    /**
     * Get search history.
     */
    suspend fun getSearchHistory(limit: Int = 20): List<SearchHistoryEntry>
    
    /**
     * Get search history as Flow.
     */
    fun getSearchHistoryFlow(limit: Int = 20): Flow<List<SearchHistoryEntry>>
    
    /**
     * Clear search history.
     */
    suspend fun clearSearchHistory()
    
    /**
     * Delete specific search history entry.
     */
    suspend fun deleteHistoryEntry(entryId: Long)
}
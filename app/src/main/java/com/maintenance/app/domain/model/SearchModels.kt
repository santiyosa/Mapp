package com.maintenance.app.domain.model

/**
 * Domain model for search results.
 */
data class SearchResult(
    val type: SearchResultType,
    val id: Long,
    val title: String,
    val subtitle: String,
    val description: String,
    val relevanceScore: Float = 0f,
    val recordId: Long? = null  // For MAINTENANCE type results, the associated record ID
)

/**
 * Types of search results.
 */
enum class SearchResultType {
    RECORD,
    MAINTENANCE
}

/**
 * Search criteria for advanced search.
 */
data class SearchCriteria(
    val query: String = "",
    val searchInRecords: Boolean = true,
    val searchInMaintenances: Boolean = true,
    val minCost: Double? = null,
    val maxCost: Double? = null,
    val startDate: Long? = null,
    val endDate: Long? = null,
    val maintenanceTypes: List<String> = emptyList(),
    val performers: List<String> = emptyList(),
    val locations: List<String> = emptyList(),
    val limit: Int = 50
) {
    fun isEmpty(): Boolean {
        return query.isBlank() && 
               minCost == null && 
               maxCost == null && 
               startDate == null && 
               endDate == null &&
               maintenanceTypes.isEmpty() &&
               performers.isEmpty() &&
               locations.isEmpty()
    }
    
    fun hasFilters(): Boolean {
        return minCost != null || 
               maxCost != null || 
               startDate != null || 
               endDate != null ||
               maintenanceTypes.isNotEmpty() ||
               performers.isNotEmpty() ||
               locations.isNotEmpty()
    }
}

/**
 * Search suggestions for autocomplete.
 */
data class SearchSuggestion(
    val text: String,
    val type: SuggestionType,
    val count: Int = 0
)

/**
 * Types of search suggestions.
 */
enum class SuggestionType {
    RECORD_NAME,
    MAINTENANCE_TYPE,
    PERFORMER,
    LOCATION,
    HISTORY
}

/**
 * Search history entry.
 */
data class SearchHistoryEntry(
    val id: Long = 0,
    val query: String,
    val criteria: SearchCriteria,
    val resultCount: Int,
    val timestamp: Long = System.currentTimeMillis()
)
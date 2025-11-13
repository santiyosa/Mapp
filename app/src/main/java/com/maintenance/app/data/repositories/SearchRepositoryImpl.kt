package com.maintenance.app.data.repositories

import com.maintenance.app.data.database.dao.SearchDAO
import com.maintenance.app.data.database.dao.SearchHistoryDAO
import com.maintenance.app.data.database.entities.SearchHistoryEntity
import com.maintenance.app.data.mappers.toDomain
import com.maintenance.app.domain.model.Maintenance
import com.maintenance.app.domain.model.Record
import com.maintenance.app.domain.model.SearchCriteria
import com.maintenance.app.domain.model.SearchHistoryEntry
import com.maintenance.app.domain.model.SearchResult
import com.maintenance.app.domain.model.SearchResultType
import com.maintenance.app.domain.model.SearchSuggestion
import com.maintenance.app.domain.model.SuggestionType
import com.maintenance.app.domain.repository.SearchRepository
import com.google.gson.Gson
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Implementation of SearchRepository.
 */
@Singleton
class SearchRepositoryImpl @Inject constructor(
    private val searchDao: SearchDAO,
    private val searchHistoryDao: SearchHistoryDAO,
    private val gson: Gson
) : SearchRepository {

    override suspend fun fullTextSearch(query: String, limit: Int): List<SearchResult> {
        return searchDao.fullTextSearch(query, limit).map { entity ->
            SearchResult(
                type = if (entity.type == "record") SearchResultType.RECORD else SearchResultType.MAINTENANCE,
                id = entity.id,
                title = entity.title,
                subtitle = entity.subtitle,
                description = entity.description,
                relevanceScore = calculateRelevanceScore(query, entity.title, entity.description)
            )
        }.sortedByDescending { it.relevanceScore }
    }

    override fun fullTextSearchFlow(query: String, limit: Int): Flow<List<SearchResult>> {
        // For Flow version, we'll use separate queries and combine results
        return searchDao.searchRecordsByNameFlow(query, limit / 2).map { recordEntities ->
            recordEntities.map { entity ->
                SearchResult(
                    type = SearchResultType.RECORD,
                    id = entity.id,
                    title = entity.name,
                    subtitle = entity.brandModel ?: "",
                    description = entity.location ?: "",
                    relevanceScore = calculateRelevanceScore(query, entity.name, entity.location ?: "")
                )
            }
        }
    }

    override suspend fun searchRecords(query: String, limit: Int): List<Record> {
        return searchDao.searchRecordsByName(query, limit).map { it.toDomain() }
    }

    override fun searchRecordsFlow(query: String, limit: Int): Flow<List<Record>> {
        return searchDao.searchRecordsByNameFlow(query, limit).map { entities ->
            entities.map { it.toDomain() }
        }
    }

    override suspend fun searchMaintenances(query: String, limit: Int): List<Maintenance> {
        return searchDao.searchMaintenancesByContent(query, limit).map { it.toDomain() }
    }

    override fun searchMaintenancesFlow(query: String, limit: Int): Flow<List<Maintenance>> {
        return searchDao.searchMaintenancesByContentFlow(query, limit).map { entities ->
            entities.map { it.toDomain() }
        }
    }

    override suspend fun advancedSearch(criteria: SearchCriteria): List<Record> {
        val nameQuery = if (criteria.query.isBlank()) null else criteria.query
        val maintenanceQuery = if (criteria.query.isBlank()) null else criteria.query
        
        return searchDao.advancedSearch(
            nameQuery = nameQuery,
            maintenanceQuery = maintenanceQuery,
            minCost = criteria.minCost,
            maxCost = criteria.maxCost,
            startDate = criteria.startDate,
            endDate = criteria.endDate,
            limit = criteria.limit
        ).map { it.toDomain() }
    }

    override suspend fun searchByDateRange(startDate: Long, endDate: Long, limit: Int): List<Maintenance> {
        return searchDao.searchMaintenancesByDateRange(startDate, endDate, limit)
            .map { it.toDomain() }
    }

    override suspend fun searchByCostRange(minCost: Double, maxCost: Double, limit: Int): List<Maintenance> {
        return searchDao.searchMaintenancesByCostRange(minCost, maxCost, limit)
            .map { it.toDomain() }
    }

    override suspend fun searchByType(type: String, limit: Int): List<Maintenance> {
        return searchDao.searchMaintenancesByType(type, limit).map { it.toDomain() }
    }

    override suspend fun getSearchSuggestions(query: String): List<SearchSuggestion> {
        val suggestions = mutableListOf<SearchSuggestion>()
        
        // Get basic search suggestions from database
        val dbSuggestions = searchDao.getSearchSuggestions(query)
        suggestions.addAll(dbSuggestions.map { 
            SearchSuggestion(
                text = it,
                type = if (it.contains(" ")) SuggestionType.MAINTENANCE_TYPE else SuggestionType.RECORD_NAME
            )
        })
        
        // Add recent search history suggestions
        val recentQueries = searchHistoryDao.getRecentQueries(query, 3)
        suggestions.addAll(recentQueries.map {
            SearchSuggestion(
                text = it,
                type = SuggestionType.HISTORY
            )
        })
        
        return suggestions.distinctBy { it.text }.take(10)
    }

    override suspend fun getMaintenanceTypes(): List<String> {
        return searchDao.getMaintenanceTypes()
    }

    override suspend fun getPerformers(): List<String> {
        return searchDao.getPerformers()
    }

    override suspend fun getLocations(): List<String> {
        return searchDao.getLocations()
    }

    // Search History Operations

    override suspend fun saveToHistory(entry: SearchHistoryEntry): Long {
        val criteriaJson = gson.toJson(entry.criteria)
        val entity = SearchHistoryEntity.fromDomain(entry, criteriaJson)
        
        // Clean old entries periodically (keep last 100)
        val count = searchHistoryDao.getHistoryCount()
        if (count > 100) {
            searchHistoryDao.deleteOldEntries(100)
        }
        
        return searchHistoryDao.insertOrUpdateHistory(entity)
    }

    override suspend fun getSearchHistory(limit: Int): List<SearchHistoryEntry> {
        return searchHistoryDao.getSearchHistory(limit).map { entity ->
            val criteria = try {
                gson.fromJson(entity.searchCriteria, SearchCriteria::class.java)
            } catch (e: Exception) {
                SearchCriteria() // Fallback to empty criteria
            }
            entity.toDomain(criteria)
        }
    }

    override fun getSearchHistoryFlow(limit: Int): Flow<List<SearchHistoryEntry>> {
        return searchHistoryDao.getSearchHistoryFlow(limit).map { entities ->
            entities.map { entity ->
                val criteria = try {
                    gson.fromJson(entity.searchCriteria, SearchCriteria::class.java)
                } catch (e: Exception) {
                    SearchCriteria() // Fallback to empty criteria
                }
                entity.toDomain(criteria)
            }
        }
    }

    override suspend fun clearSearchHistory() {
        searchHistoryDao.clearAllHistory()
    }

    override suspend fun deleteHistoryEntry(entryId: Long) {
        searchHistoryDao.deleteHistoryEntry(entryId)
    }

    /**
     * Calculate relevance score for search results.
     */
    private fun calculateRelevanceScore(query: String, title: String, description: String): Float {
        val queryLower = query.lowercase()
        val titleLower = title.lowercase()
        val descriptionLower = description.lowercase()
        
        var score = 0f
        
        // Exact match in title gets highest score
        if (titleLower == queryLower) {
            score += 10f
        } else if (titleLower.startsWith(queryLower)) {
            score += 8f
        } else if (titleLower.contains(queryLower)) {
            score += 5f
        }
        
        // Matches in description get lower score
        if (descriptionLower.contains(queryLower)) {
            score += 2f
        }
        
        // Boost score based on query word count match
        val queryWords = queryLower.split(" ").filter { it.isNotBlank() }
        queryWords.forEach { word ->
            if (titleLower.contains(word)) score += 1f
            if (descriptionLower.contains(word)) score += 0.5f
        }
        
        return score
    }
}
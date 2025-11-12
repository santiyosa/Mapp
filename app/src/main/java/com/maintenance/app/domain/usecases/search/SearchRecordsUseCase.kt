package com.maintenance.app.domain.usecases.search

import com.maintenance.app.domain.model.Record
import com.maintenance.app.domain.repository.RecordRepository
import com.maintenance.app.domain.usecases.base.FlowUseCase
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flowOf
import javax.inject.Inject

/**
 * Use case for searching records with various search criteria.
 */
class SearchRecordsUseCase @Inject constructor(
    private val recordRepository: RecordRepository
) : FlowUseCase<SearchRecordsUseCase.Params, Flow<List<Record>>>() {

    override suspend fun execute(parameters: Params): Flow<List<Record>> {
        // If search query is blank, return empty results or all records based on parameters
        if (parameters.searchQuery.isBlank()) {
            return if (parameters.includeAllWhenEmpty) {
                recordRepository.getAllActiveRecords()
            } else {
                flowOf(emptyList())
            }
        }

        // Perform the search
        val searchResults = recordRepository.searchRecords(parameters.searchQuery)

        // Apply additional filters if specified
        return if (parameters.categoryFilter != null) {
            combine(
                searchResults,
                recordRepository.getRecordsByCategory(parameters.categoryFilter)
            ) { searchedRecords, categoryRecords ->
                // Return intersection of search results and category filter
                searchedRecords.filter { searchRecord ->
                    categoryRecords.any { categoryRecord -> categoryRecord.id == searchRecord.id }
                }
            }
        } else {
            searchResults
        }
    }

    data class Params(
        val searchQuery: String,
        val categoryFilter: String? = null,
        val includeAllWhenEmpty: Boolean = false
    )
}
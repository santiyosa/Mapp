package com.maintenance.app.domain.usecases.search

import com.maintenance.app.domain.model.Maintenance
import com.maintenance.app.domain.repository.MaintenanceRepository
import com.maintenance.app.domain.usecases.base.FlowUseCase
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flowOf
import javax.inject.Inject

/**
 * Use case for searching maintenances with various search criteria.
 */
class SearchMaintenancesUseCase @Inject constructor(
    private val maintenanceRepository: MaintenanceRepository
) : FlowUseCase<SearchMaintenancesUseCase.Params, Flow<List<Maintenance>>>() {

    override suspend fun execute(parameters: Params): Flow<List<Maintenance>> {
        // If search query is blank, return empty results or all maintenances based on parameters
        if (parameters.searchQuery.isBlank()) {
            return if (parameters.includeAllWhenEmpty) {
                if (parameters.recordId != null) {
                    maintenanceRepository.getMaintenancesByRecordId(parameters.recordId)
                } else {
                    maintenanceRepository.getAllMaintenances()
                }
            } else {
                flowOf(emptyList())
            }
        }

        // Perform the search based on scope
        val searchResults = if (parameters.recordId != null) {
            // Search within a specific record's maintenances
            maintenanceRepository.searchMaintenancesForRecord(parameters.recordId, parameters.searchQuery)
        } else {
            // Search all maintenances
            maintenanceRepository.searchAllMaintenances(parameters.searchQuery)
        }

        // Apply additional filters if specified
        return when {
            parameters.typeFilter != null -> {
                combine(
                    searchResults,
                    maintenanceRepository.getMaintenancesByType(parameters.typeFilter)
                ) { searchedMaintenances, typeMaintenances ->
                    // Return intersection of search results and type filter
                    searchedMaintenances.filter { searchMaintenance ->
                        typeMaintenances.any { typeMaintenance -> typeMaintenance.id == searchMaintenance.id }
                    }
                }
            }
            parameters.statusFilter != null -> {
                combine(
                    searchResults,
                    maintenanceRepository.getMaintenancesByStatus(parameters.statusFilter)
                ) { searchedMaintenances, statusMaintenances ->
                    // Return intersection of search results and status filter
                    searchedMaintenances.filter { searchMaintenance ->
                        statusMaintenances.any { statusMaintenance -> statusMaintenance.id == searchMaintenance.id }
                    }
                }
            }
            parameters.priorityFilter != null -> {
                combine(
                    searchResults,
                    maintenanceRepository.getMaintenancesByPriority(parameters.priorityFilter)
                ) { searchedMaintenances, priorityMaintenances ->
                    // Return intersection of search results and priority filter
                    searchedMaintenances.filter { searchMaintenance ->
                        priorityMaintenances.any { priorityMaintenance -> priorityMaintenance.id == searchMaintenance.id }
                    }
                }
            }
            else -> searchResults
        }
    }

    data class Params(
        val searchQuery: String,
        val recordId: Long? = null, // If provided, search only within this record's maintenances
        val typeFilter: String? = null,
        val statusFilter: String? = null,
        val priorityFilter: String? = null,
        val includeAllWhenEmpty: Boolean = false
    )
}
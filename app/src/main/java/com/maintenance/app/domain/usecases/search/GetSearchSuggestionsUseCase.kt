package com.maintenance.app.domain.usecases.search

import com.maintenance.app.domain.repository.MaintenanceRepository
import com.maintenance.app.domain.repository.RecordRepository
import com.maintenance.app.domain.usecases.base.FlowUseCase
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

/**
 * Use case for getting search suggestions and metadata.
 */
class GetSearchSuggestionsUseCase @Inject constructor(
    private val recordRepository: RecordRepository,
    private val maintenanceRepository: MaintenanceRepository
) : FlowUseCase<GetSearchSuggestionsUseCase.Params, Flow<List<String>>>() {

    override suspend fun execute(parameters: Params): Flow<List<String>> {
        return when (parameters.type) {
            SuggestionType.RECORD_CATEGORIES -> recordRepository.getAllCategories()
            SuggestionType.MAINTENANCE_TYPES -> maintenanceRepository.getAllMaintenanceTypes()
        }
    }

    data class Params(
        val type: SuggestionType
    )

    enum class SuggestionType {
        RECORD_CATEGORIES,
        MAINTENANCE_TYPES
    }
}
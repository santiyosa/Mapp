package com.maintenance.app.domain.usecases.records

import com.maintenance.app.domain.model.Record
import com.maintenance.app.domain.repository.RecordRepository
import com.maintenance.app.domain.usecases.base.FlowUseCase
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

/**
 * Use case for getting records with various filtering options.
 */
class GetRecordsUseCase @Inject constructor(
    private val recordRepository: RecordRepository
) : FlowUseCase<GetRecordsUseCase.Params, Flow<List<Record>>>() {

    override suspend fun execute(parameters: Params): Flow<List<Record>> {
        return when (parameters.filter) {
            FilterType.ALL_ACTIVE -> recordRepository.getAllActiveRecords()
            FilterType.ALL -> recordRepository.getAllRecords()
            FilterType.BY_CATEGORY -> {
                requireNotNull(parameters.category) { "Category is required for BY_CATEGORY filter" }
                recordRepository.getRecordsByCategory(parameters.category)
            }
            FilterType.NEEDING_MAINTENANCE -> {
                requireNotNull(parameters.cutoffDate) { "Cutoff date is required for NEEDING_MAINTENANCE filter" }
                recordRepository.getRecordsNeedingMaintenance(parameters.cutoffDate)
            }
            FilterType.BY_DATE_RANGE -> {
                requireNotNull(parameters.startDate) { "Start date is required for BY_DATE_RANGE filter" }
                requireNotNull(parameters.endDate) { "End date is required for BY_DATE_RANGE filter" }
                recordRepository.getRecordsByDateRange(parameters.startDate, parameters.endDate)
            }
            FilterType.EXPIRING_WARRANTY -> {
                requireNotNull(parameters.startDate) { "Start date is required for EXPIRING_WARRANTY filter" }
                requireNotNull(parameters.endDate) { "End date is required for EXPIRING_WARRANTY filter" }
                recordRepository.getRecordsWithExpiringWarranty(parameters.startDate, parameters.endDate)
            }
        }
    }

    data class Params(
        val filter: FilterType,
        val category: String? = null,
        val cutoffDate: java.time.LocalDateTime? = null,
        val startDate: java.time.LocalDateTime? = null,
        val endDate: java.time.LocalDateTime? = null
    )

    enum class FilterType {
        ALL_ACTIVE,
        ALL,
        BY_CATEGORY,
        NEEDING_MAINTENANCE,
        BY_DATE_RANGE,
        EXPIRING_WARRANTY
    }
}
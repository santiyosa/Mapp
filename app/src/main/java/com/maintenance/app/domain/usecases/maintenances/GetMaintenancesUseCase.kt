package com.maintenance.app.domain.usecases.maintenances

import com.maintenance.app.domain.model.Maintenance
import com.maintenance.app.domain.repository.MaintenanceRepository
import com.maintenance.app.domain.usecases.base.FlowUseCase
import kotlinx.coroutines.flow.Flow
import java.math.BigDecimal
import java.time.LocalDateTime
import javax.inject.Inject

/**
 * Use case for getting maintenances with various filtering options.
 */
class GetMaintenancesUseCase @Inject constructor(
    private val maintenanceRepository: MaintenanceRepository
) : FlowUseCase<GetMaintenancesUseCase.Params, Flow<List<Maintenance>>>() {

    override suspend fun execute(parameters: Params): Flow<List<Maintenance>> {
        return when (parameters.filter) {
            FilterType.ALL -> maintenanceRepository.getAllMaintenances()
            FilterType.BY_RECORD -> {
                requireNotNull(parameters.recordId) { "Record ID is required for BY_RECORD filter" }
                maintenanceRepository.getMaintenancesByRecordId(parameters.recordId)
            }
            FilterType.BY_TYPE -> {
                requireNotNull(parameters.type) { "Type is required for BY_TYPE filter" }
                maintenanceRepository.getMaintenancesByType(parameters.type)
            }
            FilterType.BY_STATUS -> {
                requireNotNull(parameters.status) { "Status is required for BY_STATUS filter" }
                maintenanceRepository.getMaintenancesByStatus(parameters.status)
            }
            FilterType.BY_PRIORITY -> {
                requireNotNull(parameters.priority) { "Priority is required for BY_PRIORITY filter" }
                maintenanceRepository.getMaintenancesByPriority(parameters.priority)
            }
            FilterType.BY_DATE_RANGE -> {
                requireNotNull(parameters.startDate) { "Start date is required for BY_DATE_RANGE filter" }
                requireNotNull(parameters.endDate) { "End date is required for BY_DATE_RANGE filter" }
                maintenanceRepository.getMaintenancesByDateRange(parameters.startDate, parameters.endDate)
            }
            FilterType.BY_RECORD_AND_DATE_RANGE -> {
                requireNotNull(parameters.recordId) { "Record ID is required for BY_RECORD_AND_DATE_RANGE filter" }
                requireNotNull(parameters.startDate) { "Start date is required for BY_RECORD_AND_DATE_RANGE filter" }
                requireNotNull(parameters.endDate) { "End date is required for BY_RECORD_AND_DATE_RANGE filter" }
                maintenanceRepository.getMaintenancesByRecordAndDateRange(
                    parameters.recordId,
                    parameters.startDate,
                    parameters.endDate
                )
            }
            FilterType.BY_COST_RANGE -> {
                requireNotNull(parameters.minCost) { "Min cost is required for BY_COST_RANGE filter" }
                requireNotNull(parameters.maxCost) { "Max cost is required for BY_COST_RANGE filter" }
                maintenanceRepository.getMaintenancesByCostRange(parameters.minCost, parameters.maxCost)
            }
            FilterType.UPCOMING -> {
                requireNotNull(parameters.dueDate) { "Due date is required for UPCOMING filter" }
                maintenanceRepository.getUpcomingMaintenances(parameters.dueDate)
            }
            FilterType.UPCOMING_FOR_RECORD -> {
                requireNotNull(parameters.recordId) { "Record ID is required for UPCOMING_FOR_RECORD filter" }
                requireNotNull(parameters.dueDate) { "Due date is required for UPCOMING_FOR_RECORD filter" }
                maintenanceRepository.getUpcomingMaintenancesForRecord(parameters.recordId, parameters.dueDate)
            }
        }
    }

    data class Params(
        val filter: FilterType,
        val recordId: Long? = null,
        val type: String? = null,
        val status: String? = null,
        val priority: String? = null,
        val startDate: LocalDateTime? = null,
        val endDate: LocalDateTime? = null,
        val minCost: BigDecimal? = null,
        val maxCost: BigDecimal? = null,
        val dueDate: LocalDateTime? = null
    )

    enum class FilterType {
        ALL,
        BY_RECORD,
        BY_TYPE,
        BY_STATUS,
        BY_PRIORITY,
        BY_DATE_RANGE,
        BY_RECORD_AND_DATE_RANGE,
        BY_COST_RANGE,
        UPCOMING,
        UPCOMING_FOR_RECORD
    }
}
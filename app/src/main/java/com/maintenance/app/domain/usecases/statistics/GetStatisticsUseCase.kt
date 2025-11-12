package com.maintenance.app.domain.usecases.statistics

import com.maintenance.app.domain.repository.MaintenanceRepository
import com.maintenance.app.domain.repository.RecordRepository
import com.maintenance.app.domain.usecases.base.UseCase
import com.maintenance.app.utils.Result
import java.math.BigDecimal
import java.time.LocalDateTime
import javax.inject.Inject

/**
 * Use case for getting application statistics and metrics.
 */
class GetStatisticsUseCase @Inject constructor(
    private val recordRepository: RecordRepository,
    private val maintenanceRepository: MaintenanceRepository
) : UseCase<GetStatisticsUseCase.Params, StatisticsData>() {

    override suspend fun execute(parameters: Params): StatisticsData {
        val startDate = parameters.startDate
        val endDate = parameters.endDate

        // Get basic counts
        val totalRecords = when (val result = recordRepository.getTotalRecordsCount()) {
            is Result.Success -> result.data
            is Result.Error -> throw result.exception ?: Exception(result.message)
            is Result.Loading -> throw Exception("Unexpected loading state")
        }

        val activeRecords = when (val result = recordRepository.getActiveRecordsCount()) {
            is Result.Success -> result.data
            is Result.Error -> throw result.exception ?: Exception(result.message)
            is Result.Loading -> throw Exception("Unexpected loading state")
        }

        val totalMaintenances = when (val result = maintenanceRepository.getTotalMaintenanceCount()) {
            is Result.Success -> result.data
            is Result.Error -> throw result.exception ?: Exception(result.message)
            is Result.Loading -> throw Exception("Unexpected loading state")
        }

        // Get cost statistics
        val averageCost = when (val result = maintenanceRepository.getAverageMaintenanceCost()) {
            is Result.Success -> result.data
            is Result.Error -> throw result.exception ?: Exception(result.message)
            is Result.Loading -> throw Exception("Unexpected loading state")
        }

        val totalCost = if (startDate != null && endDate != null) {
            when (val result = maintenanceRepository.getTotalCostByDateRange(startDate, endDate)) {
                is Result.Success -> result.data
                is Result.Error -> throw result.exception ?: Exception(result.message)
                is Result.Loading -> throw Exception("Unexpected loading state")
            }
        } else {
            null
        }

        return StatisticsData(
            totalRecords = totalRecords,
            activeRecords = activeRecords,
            inactiveRecords = totalRecords - activeRecords,
            totalMaintenances = totalMaintenances,
            averageMaintenanceCost = averageCost,
            totalCostInPeriod = totalCost,
            periodStartDate = startDate,
            periodEndDate = endDate,
            generatedAt = LocalDateTime.now()
        )
    }

    data class Params(
        val startDate: LocalDateTime? = null,
        val endDate: LocalDateTime? = null
    )
}

/**
 * Data class representing application statistics.
 */
data class StatisticsData(
    val totalRecords: Long,
    val activeRecords: Long,
    val inactiveRecords: Long,
    val totalMaintenances: Long,
    val averageMaintenanceCost: BigDecimal?,
    val totalCostInPeriod: BigDecimal?,
    val periodStartDate: LocalDateTime?,
    val periodEndDate: LocalDateTime?,
    val generatedAt: LocalDateTime
) {
    /**
     * Gets the maintenance frequency per record.
     */
    fun getMaintenanceFrequency(): Double {
        return if (activeRecords > 0) {
            totalMaintenances.toDouble() / activeRecords.toDouble()
        } else {
            0.0
        }
    }

    /**
     * Gets the percentage of active records.
     */
    fun getActiveRecordsPercentage(): Double {
        return if (totalRecords > 0) {
            (activeRecords.toDouble() / totalRecords.toDouble()) * 100
        } else {
            0.0
        }
    }
}
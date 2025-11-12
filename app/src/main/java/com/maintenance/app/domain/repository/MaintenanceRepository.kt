package com.maintenance.app.domain.repository

import com.maintenance.app.domain.model.Maintenance
import com.maintenance.app.utils.Result
import kotlinx.coroutines.flow.Flow
import java.math.BigDecimal
import java.time.LocalDateTime

/**
 * Repository interface for Maintenance operations.
 */
interface MaintenanceRepository {

    /**
     * Gets all maintenances for a specific record.
     */
    fun getMaintenancesByRecordId(recordId: Long): Flow<List<Maintenance>>

    /**
     * Gets a specific maintenance by ID.
     */
    suspend fun getMaintenanceById(maintenanceId: Long): Result<Maintenance?>

    /**
     * Gets a specific maintenance by ID as a Flow.
     */
    fun getMaintenanceByIdFlow(maintenanceId: Long): Flow<Maintenance?>

    /**
     * Gets all maintenances.
     */
    fun getAllMaintenances(): Flow<List<Maintenance>>

    /**
     * Searches maintenances for a specific record.
     */
    fun searchMaintenancesForRecord(recordId: Long, searchQuery: String): Flow<List<Maintenance>>

    /**
     * Searches all maintenances.
     */
    fun searchAllMaintenances(searchQuery: String): Flow<List<Maintenance>>

    /**
     * Gets maintenances within a date range.
     */
    fun getMaintenancesByDateRange(startDate: LocalDateTime, endDate: LocalDateTime): Flow<List<Maintenance>>

    /**
     * Gets maintenances for a record within a date range.
     */
    fun getMaintenancesByRecordAndDateRange(
        recordId: Long, 
        startDate: LocalDateTime, 
        endDate: LocalDateTime
    ): Flow<List<Maintenance>>

    /**
     * Gets maintenances by type.
     */
    fun getMaintenancesByType(type: String): Flow<List<Maintenance>>

    /**
     * Gets all maintenance types.
     */
    fun getAllMaintenanceTypes(): Flow<List<String>>

    /**
     * Gets maintenances within a cost range.
     */
    fun getMaintenancesByCostRange(minCost: BigDecimal, maxCost: BigDecimal): Flow<List<Maintenance>>

    /**
     * Gets maintenances by status.
     */
    fun getMaintenancesByStatus(status: String): Flow<List<Maintenance>>

    /**
     * Gets maintenances by priority.
     */
    fun getMaintenancesByPriority(priority: String): Flow<List<Maintenance>>

    /**
     * Gets upcoming maintenances that are due.
     */
    fun getUpcomingMaintenances(dueDate: LocalDateTime): Flow<List<Maintenance>>

    /**
     * Gets upcoming maintenances for a specific record.
     */
    fun getUpcomingMaintenancesForRecord(recordId: Long, dueDate: LocalDateTime): Flow<List<Maintenance>>

    /**
     * Gets the count of maintenances for a record.
     */
    suspend fun getMaintenanceCountByRecord(recordId: Long): Result<Long>

    /**
     * Gets the total count of all maintenances.
     */
    suspend fun getTotalMaintenanceCount(): Result<Long>

    /**
     * Gets the total cost of maintenances for a record.
     */
    suspend fun getTotalCostByRecord(recordId: Long): Result<BigDecimal?>

    /**
     * Gets the total cost of maintenances within a date range.
     */
    suspend fun getTotalCostByDateRange(startDate: LocalDateTime, endDate: LocalDateTime): Result<BigDecimal?>

    /**
     * Gets the latest maintenance for a record.
     */
    suspend fun getLatestMaintenanceForRecord(recordId: Long): Result<Maintenance?>

    /**
     * Gets the average maintenance cost.
     */
    suspend fun getAverageMaintenanceCost(): Result<BigDecimal?>

    /**
     * Creates a new maintenance.
     */
    suspend fun createMaintenance(maintenance: Maintenance): Result<Long>

    /**
     * Updates an existing maintenance.
     */
    suspend fun updateMaintenance(maintenance: Maintenance): Result<Unit>

    /**
     * Deletes a maintenance.
     */
    suspend fun deleteMaintenance(maintenance: Maintenance): Result<Unit>

    /**
     * Deletes a maintenance by ID.
     */
    suspend fun deleteMaintenanceById(maintenanceId: Long): Result<Unit>

    /**
     * Deletes all maintenances for a record.
     */
    suspend fun deleteMaintenancesByRecordId(recordId: Long): Result<Unit>
}
package com.maintenance.app.data.repository

import com.maintenance.app.data.database.dao.MaintenanceDAO
import com.maintenance.app.data.mappers.toDomain
import com.maintenance.app.data.mappers.toEntity
import com.maintenance.app.data.mappers.toMaintenanceDomain
import com.maintenance.app.domain.model.Maintenance
import com.maintenance.app.domain.repository.MaintenanceRepository
import com.maintenance.app.utils.Result
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import java.math.BigDecimal
import java.time.LocalDateTime
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Implementation of MaintenanceRepository using Room database.
 */
@Singleton
class MaintenanceRepositoryImpl @Inject constructor(
    private val maintenanceDAO: MaintenanceDAO
) : MaintenanceRepository {

    override fun getMaintenancesByRecordId(recordId: Long): Flow<List<Maintenance>> {
        return maintenanceDAO.getMaintenancesByRecordId(recordId).map { entities ->
            entities.toMaintenanceDomain()
        }
    }

    override suspend fun getMaintenanceById(maintenanceId: Long): Result<Maintenance?> {
        return Result.safeCall {
            maintenanceDAO.getMaintenanceById(maintenanceId)?.toDomain()
        }
    }

    override fun getMaintenanceByIdFlow(maintenanceId: Long): Flow<Maintenance?> {
        return maintenanceDAO.getMaintenanceByIdFlow(maintenanceId).map { entity ->
            entity?.toDomain()
        }
    }

    override fun getAllMaintenances(): Flow<List<Maintenance>> {
        return maintenanceDAO.getAllMaintenances().map { entities ->
            entities.toMaintenanceDomain()
        }
    }

    override fun searchMaintenancesForRecord(
        recordId: Long,
        searchQuery: String
    ): Flow<List<Maintenance>> {
        val formattedQuery = "%$searchQuery%"
        return maintenanceDAO.searchMaintenancesForRecord(recordId, formattedQuery).map { entities ->
            entities.toMaintenanceDomain()
        }
    }

    override fun searchAllMaintenances(searchQuery: String): Flow<List<Maintenance>> {
        val formattedQuery = "%$searchQuery%"
        return maintenanceDAO.searchAllMaintenances(formattedQuery).map { entities ->
            entities.toMaintenanceDomain()
        }
    }

    override fun getMaintenancesByDateRange(
        startDate: LocalDateTime,
        endDate: LocalDateTime
    ): Flow<List<Maintenance>> {
        return maintenanceDAO.getMaintenancesByDateRange(startDate, endDate).map { entities ->
            entities.toMaintenanceDomain()
        }
    }

    override fun getMaintenancesByRecordAndDateRange(
        recordId: Long,
        startDate: LocalDateTime,
        endDate: LocalDateTime
    ): Flow<List<Maintenance>> {
        return maintenanceDAO.getMaintenancesByRecordAndDateRange(recordId, startDate, endDate).map { entities ->
            entities.toMaintenanceDomain()
        }
    }

    override fun getMaintenancesByType(type: String): Flow<List<Maintenance>> {
        return maintenanceDAO.getMaintenancesByType(type).map { entities ->
            entities.toMaintenanceDomain()
        }
    }

    override fun getAllMaintenanceTypes(): Flow<List<String>> {
        return maintenanceDAO.getAllMaintenanceTypes()
    }

    override fun getMaintenancesByCostRange(
        minCost: BigDecimal,
        maxCost: BigDecimal
    ): Flow<List<Maintenance>> {
        return maintenanceDAO.getMaintenancesByCostRange(minCost, maxCost).map { entities ->
            entities.toMaintenanceDomain()
        }
    }

    override fun getMaintenancesByStatus(status: String): Flow<List<Maintenance>> {
        return maintenanceDAO.getMaintenancesByStatus(status).map { entities ->
            entities.toMaintenanceDomain()
        }
    }

    override fun getMaintenancesByPriority(priority: String): Flow<List<Maintenance>> {
        return maintenanceDAO.getMaintenancesByPriority(priority).map { entities ->
            entities.toMaintenanceDomain()
        }
    }

    override fun getUpcomingMaintenances(dueDate: LocalDateTime): Flow<List<Maintenance>> {
        return maintenanceDAO.getUpcomingMaintenances(dueDate).map { entities ->
            entities.toMaintenanceDomain()
        }
    }

    override fun getUpcomingMaintenancesForRecord(
        recordId: Long,
        dueDate: LocalDateTime
    ): Flow<List<Maintenance>> {
        return maintenanceDAO.getUpcomingMaintenancesForRecord(recordId, dueDate).map { entities ->
            entities.toMaintenanceDomain()
        }
    }

    override suspend fun getMaintenanceCountByRecord(recordId: Long): Result<Long> {
        return Result.safeCall {
            maintenanceDAO.getMaintenanceCountByRecord(recordId)
        }
    }

    override suspend fun getTotalMaintenanceCount(): Result<Long> {
        return Result.safeCall {
            maintenanceDAO.getTotalMaintenanceCount()
        }
    }

    override suspend fun getTotalCostByRecord(recordId: Long): Result<BigDecimal?> {
        return Result.safeCall {
            maintenanceDAO.getTotalCostByRecord(recordId)
        }
    }

    override suspend fun getTotalCostByDateRange(
        startDate: LocalDateTime,
        endDate: LocalDateTime
    ): Result<BigDecimal?> {
        return Result.safeCall {
            maintenanceDAO.getTotalCostByDateRange(startDate, endDate)
        }
    }

    override suspend fun getLatestMaintenanceForRecord(recordId: Long): Result<Maintenance?> {
        return Result.safeCall {
            maintenanceDAO.getLatestMaintenanceForRecord(recordId)?.toDomain()
        }
    }

    override suspend fun getAverageMaintenanceCost(): Result<BigDecimal?> {
        return Result.safeCall {
            maintenanceDAO.getAverageMaintenanceCost()
        }
    }

    override suspend fun createMaintenance(maintenance: Maintenance): Result<Long> {
        return Result.safeCall {
            val entity = maintenance.toEntity()
            maintenanceDAO.insertMaintenance(entity)
        }
    }

    override suspend fun updateMaintenance(maintenance: Maintenance): Result<Unit> {
        return Result.safeCall {
            val entity = maintenance.toEntity()
            maintenanceDAO.updateMaintenance(entity)
        }
    }

    override suspend fun deleteMaintenance(maintenance: Maintenance): Result<Unit> {
        return Result.safeCall {
            val entity = maintenance.toEntity()
            maintenanceDAO.deleteMaintenance(entity)
        }
    }

    override suspend fun deleteMaintenanceById(maintenanceId: Long): Result<Unit> {
        return Result.safeCall {
            maintenanceDAO.deleteMaintenanceById(maintenanceId)
        }
    }

    override suspend fun deleteMaintenancesByRecordId(recordId: Long): Result<Unit> {
        return Result.safeCall {
            maintenanceDAO.deleteMaintenancesByRecordId(recordId)
        }
    }
}
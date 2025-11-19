package com.maintenance.app.data.repository

import com.maintenance.app.data.database.dao.RecordDAO
import com.maintenance.app.data.mappers.toDomain
import com.maintenance.app.data.mappers.toEntity
import com.maintenance.app.domain.model.Record
import com.maintenance.app.domain.repository.RecordRepository
import com.maintenance.app.utils.Result
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import java.time.LocalDateTime
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Implementation of RecordRepository using Room database.
 */
@Singleton
class RecordRepositoryImpl @Inject constructor(
    private val recordDAO: RecordDAO
) : RecordRepository {

    override fun getAllActiveRecords(): Flow<List<Record>> {
        return recordDAO.getAllActiveRecords().map { entities ->
            entities.toDomain()
        }
    }

    override fun getAllRecords(): Flow<List<Record>> {
        return recordDAO.getAllRecords().map { entities ->
            entities.toDomain()
        }
    }

    override suspend fun getRecordById(recordId: Long): Result<Record?> {
        return Result.safeCall {
            recordDAO.getRecordById(recordId)?.toDomain()
        }
    }

    override fun getRecordByIdFlow(recordId: Long): Flow<Record?> {
        return recordDAO.getRecordByIdFlow(recordId).map { entity ->
            entity?.toDomain()
        }
    }

    override fun searchRecords(searchQuery: String): Flow<List<Record>> {
        val formattedQuery = "%$searchQuery%"
        return recordDAO.searchRecords(formattedQuery).map { entities ->
            entities.toDomain()
        }
    }

    override fun getRecordsByCategory(category: String): Flow<List<Record>> {
        return recordDAO.getRecordsByCategory(category).map { entities ->
            entities.toDomain()
        }
    }

    override fun getAllCategories(): Flow<List<String>> {
        return recordDAO.getAllCategories()
    }

    override fun getRecordsByDateRange(
        startDate: LocalDateTime,
        endDate: LocalDateTime
    ): Flow<List<Record>> {
        return recordDAO.getRecordsByDateRange(startDate, endDate).map { entities ->
            entities.toDomain()
        }
    }

    override fun getRecordsNeedingMaintenance(cutoffDate: LocalDateTime): Flow<List<Record>> {
        return recordDAO.getRecordsNeedingMaintenance(cutoffDate).map { entities ->
            entities.toDomain()
        }
    }

    override suspend fun getActiveRecordsCount(): Result<Long> {
        return Result.safeCall {
            recordDAO.getActiveRecordsCount()
        }
    }

    override suspend fun getTotalRecordsCount(): Result<Long> {
        return Result.safeCall {
            recordDAO.getTotalRecordsCount()
        }
    }

    override suspend fun createRecord(record: Record): Result<Long> {
        return Result.safeCall {
            val entity = record.toEntity()
            recordDAO.insertRecord(entity)
        }
    }

    override suspend fun updateRecord(record: Record): Result<Unit> {
        return Result.safeCall {
            val entity = record.toEntity()
            recordDAO.updateRecord(entity)
        }
    }

    override suspend fun deleteRecord(record: Record): Result<Unit> {
        return Result.safeCall {
            val entity = record.toEntity()
            recordDAO.deleteRecord(entity)
        }
    }

    override suspend fun deleteRecordById(recordId: Long): Result<Unit> {
        return Result.safeCall {
            recordDAO.deleteRecordById(recordId)
        }
    }

    override suspend fun softDeleteRecord(recordId: Long): Result<Unit> {
        return Result.safeCall {
            recordDAO.softDeleteRecord(recordId)
        }
    }

    override suspend fun updateLastMaintenanceDate(
        recordId: Long,
        maintenanceDate: LocalDateTime
    ): Result<Unit> {
        return Result.safeCall {
            recordDAO.updateLastMaintenanceDate(recordId, maintenanceDate)
        }
    }

    override fun getRecordsWithExpiringWarranty(
        startDate: LocalDateTime,
        endDate: LocalDateTime
    ): Flow<List<Record>> {
        return recordDAO.getRecordsWithExpiringWarranty(startDate, endDate).map { entities ->
            entities.toDomain()
        }
    }

    override fun getDeletedRecords(): Flow<List<Record>> {
        return recordDAO.getDeletedRecords().map { entities ->
            entities.toDomain()
        }
    }

    override suspend fun restoreRecord(recordId: Long): Result<Unit> {
        return Result.safeCall {
            recordDAO.restoreRecord(recordId)
        }
    }
}
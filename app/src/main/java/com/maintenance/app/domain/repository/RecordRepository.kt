package com.maintenance.app.domain.repository

import com.maintenance.app.domain.model.Record
import com.maintenance.app.utils.Result
import kotlinx.coroutines.flow.Flow
import java.time.LocalDateTime

/**
 * Repository interface for Record operations.
 * This interface defines the contract for record data operations
 * in the domain layer, following Clean Architecture principles.
 */
interface RecordRepository {

    /**
     * Gets all active records as a Flow.
     */
    fun getAllActiveRecords(): Flow<List<Record>>

    /**
     * Gets all records including inactive ones as a Flow.
     */
    fun getAllRecords(): Flow<List<Record>>

    /**
     * Gets a specific record by ID.
     */
    suspend fun getRecordById(recordId: Long): Result<Record?>

    /**
     * Gets a specific record by ID as a Flow.
     */
    fun getRecordByIdFlow(recordId: Long): Flow<Record?>

    /**
     * Searches records by name, description, category, etc.
     */
    fun searchRecords(searchQuery: String): Flow<List<Record>>

    /**
     * Gets records by category.
     */
    fun getRecordsByCategory(category: String): Flow<List<Record>>

    /**
     * Gets all available categories.
     */
    fun getAllCategories(): Flow<List<String>>

    /**
     * Gets records created within a date range.
     */
    fun getRecordsByDateRange(startDate: LocalDateTime, endDate: LocalDateTime): Flow<List<Record>>

    /**
     * Gets records that need maintenance (no maintenance or overdue).
     */
    fun getRecordsNeedingMaintenance(cutoffDate: LocalDateTime): Flow<List<Record>>

    /**
     * Gets the count of active records.
     */
    suspend fun getActiveRecordsCount(): Result<Long>

    /**
     * Gets the total count of all records.
     */
    suspend fun getTotalRecordsCount(): Result<Long>

    /**
     * Creates a new record.
     */
    suspend fun createRecord(record: Record): Result<Long>

    /**
     * Updates an existing record.
     */
    suspend fun updateRecord(record: Record): Result<Unit>

    /**
     * Deletes a record permanently.
     */
    suspend fun deleteRecord(record: Record): Result<Unit>

    /**
     * Deletes a record by ID.
     */
    suspend fun deleteRecordById(recordId: Long): Result<Unit>

    /**
     * Soft deletes a record (marks as inactive).
     */
    suspend fun softDeleteRecord(recordId: Long): Result<Unit>

    /**
     * Updates the last maintenance date for a record.
     */
    suspend fun updateLastMaintenanceDate(recordId: Long, maintenanceDate: LocalDateTime): Result<Unit>

    /**
     * Gets records with warranty expiring within a date range.
     */
    fun getRecordsWithExpiringWarranty(startDate: LocalDateTime, endDate: LocalDateTime): Flow<List<Record>>

    /**
     * Gets all deleted (inactive) records.
     */
    fun getDeletedRecords(): Flow<List<Record>>

    /**
     * Restores a deleted record (marks as active).
     */
    suspend fun restoreRecord(recordId: Long): Result<Unit>
}
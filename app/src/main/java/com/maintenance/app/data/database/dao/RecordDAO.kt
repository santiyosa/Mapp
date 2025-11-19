package com.maintenance.app.data.database.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.maintenance.app.data.database.entities.RecordEntity
import kotlinx.coroutines.flow.Flow
import java.time.LocalDateTime

/**
 * Data Access Object for Record operations.
 */
@Dao
interface RecordDAO {

    @Query("SELECT * FROM records WHERE is_active = 1 ORDER BY created_date DESC")
    fun getAllActiveRecords(): Flow<List<RecordEntity>>

    @Query("SELECT * FROM records ORDER BY created_date DESC")
    fun getAllRecords(): Flow<List<RecordEntity>>

    @Query("SELECT * FROM records WHERE id = :recordId")
    suspend fun getRecordById(recordId: Long): RecordEntity?

    @Query("SELECT * FROM records WHERE id = :recordId")
    fun getRecordByIdFlow(recordId: Long): Flow<RecordEntity?>

    @Query("""
        SELECT * FROM records 
        WHERE is_active = 1 AND (
            name LIKE :searchQuery OR 
            description LIKE :searchQuery OR 
            category LIKE :searchQuery OR 
            brand_model LIKE :searchQuery OR
            serial_number LIKE :searchQuery
        )
        ORDER BY created_date DESC
    """)
    fun searchRecords(searchQuery: String): Flow<List<RecordEntity>>

    @Query("SELECT * FROM records WHERE category = :category AND is_active = 1 ORDER BY created_date DESC")
    fun getRecordsByCategory(category: String): Flow<List<RecordEntity>>

    @Query("SELECT DISTINCT category FROM records WHERE category IS NOT NULL AND is_active = 1")
    fun getAllCategories(): Flow<List<String>>

    @Query("""
        SELECT * FROM records 
        WHERE created_date BETWEEN :startDate AND :endDate 
        AND is_active = 1 
        ORDER BY created_date DESC
    """)
    fun getRecordsByDateRange(startDate: LocalDateTime, endDate: LocalDateTime): Flow<List<RecordEntity>>

    @Query("""
        SELECT * FROM records 
        WHERE last_maintenance_date IS NULL OR 
        last_maintenance_date < :cutoffDate
        AND is_active = 1 
        ORDER BY last_maintenance_date ASC
    """)
    fun getRecordsNeedingMaintenance(cutoffDate: LocalDateTime): Flow<List<RecordEntity>>

    @Query("SELECT COUNT(*) FROM records WHERE is_active = 1")
    suspend fun getActiveRecordsCount(): Long

    @Query("SELECT COUNT(*) FROM records")
    suspend fun getTotalRecordsCount(): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertRecord(record: RecordEntity): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertRecords(records: List<RecordEntity>): List<Long>

    @Update
    suspend fun updateRecord(record: RecordEntity)

    @Update
    suspend fun updateRecords(records: List<RecordEntity>)

    @Delete
    suspend fun deleteRecord(record: RecordEntity)

    @Query("DELETE FROM records WHERE id = :recordId")
    suspend fun deleteRecordById(recordId: Long)

    @Query("UPDATE records SET is_active = 0 WHERE id = :recordId")
    suspend fun softDeleteRecord(recordId: Long)

    @Query("UPDATE records SET last_maintenance_date = :maintenanceDate WHERE id = :recordId")
    suspend fun updateLastMaintenanceDate(recordId: Long, maintenanceDate: LocalDateTime)

    @Query("DELETE FROM records WHERE is_active = 0")
    suspend fun deleteInactiveRecords()

    @Query("SELECT * FROM records WHERE warranty_expiry_date BETWEEN :startDate AND :endDate")
    fun getRecordsWithExpiringWarranty(startDate: LocalDateTime, endDate: LocalDateTime): Flow<List<RecordEntity>>

    @Query("SELECT * FROM records WHERE is_active = 0 ORDER BY updated_date DESC")
    fun getDeletedRecords(): Flow<List<RecordEntity>>

    @Query("UPDATE records SET is_active = 1 WHERE id = :recordId")
    suspend fun restoreRecord(recordId: Long)
}
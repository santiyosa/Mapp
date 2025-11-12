package com.maintenance.app.data.database.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.maintenance.app.data.database.entities.MaintenanceEntity
import kotlinx.coroutines.flow.Flow
import java.math.BigDecimal
import java.time.LocalDateTime

/**
 * Data Access Object for Maintenance operations.
 */
@Dao
interface MaintenanceDAO {

    @Query("SELECT * FROM maintenances WHERE record_id = :recordId ORDER BY maintenance_date DESC")
    fun getMaintenancesByRecordId(recordId: Long): Flow<List<MaintenanceEntity>>

    @Query("SELECT * FROM maintenances WHERE id = :maintenanceId")
    suspend fun getMaintenanceById(maintenanceId: Long): MaintenanceEntity?

    @Query("SELECT * FROM maintenances WHERE id = :maintenanceId")
    fun getMaintenanceByIdFlow(maintenanceId: Long): Flow<MaintenanceEntity?>

    @Query("SELECT * FROM maintenances ORDER BY maintenance_date DESC")
    fun getAllMaintenances(): Flow<List<MaintenanceEntity>>

    @Query("""
        SELECT * FROM maintenances 
        WHERE record_id = :recordId AND (
            description LIKE :searchQuery OR 
            type LIKE :searchQuery OR 
            performed_by LIKE :searchQuery OR 
            parts_replaced LIKE :searchQuery OR
            notes LIKE :searchQuery
        )
        ORDER BY maintenance_date DESC
    """)
    fun searchMaintenancesForRecord(recordId: Long, searchQuery: String): Flow<List<MaintenanceEntity>>

    @Query("""
        SELECT * FROM maintenances 
        WHERE description LIKE :searchQuery OR 
        type LIKE :searchQuery OR 
        performed_by LIKE :searchQuery OR 
        parts_replaced LIKE :searchQuery OR
        notes LIKE :searchQuery
        ORDER BY maintenance_date DESC
    """)
    fun searchAllMaintenances(searchQuery: String): Flow<List<MaintenanceEntity>>

    @Query("""
        SELECT * FROM maintenances 
        WHERE maintenance_date BETWEEN :startDate AND :endDate 
        ORDER BY maintenance_date DESC
    """)
    fun getMaintenancesByDateRange(startDate: LocalDateTime, endDate: LocalDateTime): Flow<List<MaintenanceEntity>>

    @Query("""
        SELECT * FROM maintenances 
        WHERE record_id = :recordId AND 
        maintenance_date BETWEEN :startDate AND :endDate 
        ORDER BY maintenance_date DESC
    """)
    fun getMaintenancesByRecordAndDateRange(
        recordId: Long, 
        startDate: LocalDateTime, 
        endDate: LocalDateTime
    ): Flow<List<MaintenanceEntity>>

    @Query("SELECT * FROM maintenances WHERE type = :type ORDER BY maintenance_date DESC")
    fun getMaintenancesByType(type: String): Flow<List<MaintenanceEntity>>

    @Query("SELECT DISTINCT type FROM maintenances ORDER BY type ASC")
    fun getAllMaintenanceTypes(): Flow<List<String>>

    @Query("""
        SELECT * FROM maintenances 
        WHERE cost BETWEEN :minCost AND :maxCost 
        ORDER BY cost DESC
    """)
    fun getMaintenancesByCostRange(minCost: BigDecimal, maxCost: BigDecimal): Flow<List<MaintenanceEntity>>

    @Query("SELECT * FROM maintenances WHERE status = :status ORDER BY maintenance_date DESC")
    fun getMaintenancesByStatus(status: String): Flow<List<MaintenanceEntity>>

    @Query("SELECT * FROM maintenances WHERE priority = :priority ORDER BY maintenance_date DESC")
    fun getMaintenancesByPriority(priority: String): Flow<List<MaintenanceEntity>>

    @Query("""
        SELECT * FROM maintenances 
        WHERE next_maintenance_due IS NOT NULL AND 
        next_maintenance_due <= :dueDate 
        ORDER BY next_maintenance_due ASC
    """)
    fun getUpcomingMaintenances(dueDate: LocalDateTime): Flow<List<MaintenanceEntity>>

    @Query("""
        SELECT * FROM maintenances 
        WHERE record_id = :recordId AND 
        next_maintenance_due IS NOT NULL AND 
        next_maintenance_due <= :dueDate 
        ORDER BY next_maintenance_due ASC
    """)
    fun getUpcomingMaintenancesForRecord(recordId: Long, dueDate: LocalDateTime): Flow<List<MaintenanceEntity>>

    @Query("SELECT COUNT(*) FROM maintenances WHERE record_id = :recordId")
    suspend fun getMaintenanceCountByRecord(recordId: Long): Long

    @Query("SELECT COUNT(*) FROM maintenances")
    suspend fun getTotalMaintenanceCount(): Long

    @Query("SELECT SUM(cost) FROM maintenances WHERE record_id = :recordId AND cost IS NOT NULL")
    suspend fun getTotalCostByRecord(recordId: Long): BigDecimal?

    @Query("""
        SELECT SUM(cost) FROM maintenances 
        WHERE cost IS NOT NULL AND 
        maintenance_date BETWEEN :startDate AND :endDate
    """)
    suspend fun getTotalCostByDateRange(startDate: LocalDateTime, endDate: LocalDateTime): BigDecimal?

    @Query("""
        SELECT * FROM maintenances 
        WHERE record_id = :recordId 
        ORDER BY maintenance_date DESC 
        LIMIT 1
    """)
    suspend fun getLatestMaintenanceForRecord(recordId: Long): MaintenanceEntity?

    @Query("SELECT AVG(cost) FROM maintenances WHERE cost IS NOT NULL")
    suspend fun getAverageMaintenanceCost(): BigDecimal?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertMaintenance(maintenance: MaintenanceEntity): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertMaintenances(maintenances: List<MaintenanceEntity>): List<Long>

    @Update
    suspend fun updateMaintenance(maintenance: MaintenanceEntity)

    @Update
    suspend fun updateMaintenances(maintenances: List<MaintenanceEntity>)

    @Delete
    suspend fun deleteMaintenance(maintenance: MaintenanceEntity)

    @Query("DELETE FROM maintenances WHERE id = :maintenanceId")
    suspend fun deleteMaintenanceById(maintenanceId: Long)

    @Query("DELETE FROM maintenances WHERE record_id = :recordId")
    suspend fun deleteMaintenancesByRecordId(recordId: Long)
}
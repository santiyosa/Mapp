package com.maintenance.app.data.database.dao

import androidx.room.Dao
import androidx.room.Query
import com.maintenance.app.data.database.entities.MaintenanceEntity
import com.maintenance.app.data.database.entities.RecordEntity
import com.maintenance.app.data.database.entities.SearchResultEntity
import kotlinx.coroutines.flow.Flow

/**
 * Data Access Object for search operations.
 * Provides complex queries for searching across records and maintenances.
 */
@Dao
interface SearchDAO {
    
    /**
     * Search records by name (case-insensitive, partial matches).
     */
    @Query("""
        SELECT * FROM records 
        WHERE name LIKE '%' || :query || '%' 
        ORDER BY 
            CASE WHEN name LIKE :query || '%' THEN 1 ELSE 2 END,
            name ASC
        LIMIT :limit
    """)
    suspend fun searchRecordsByName(query: String, limit: Int = 50): List<RecordEntity>
    
    /**
     * Search records by name as Flow for real-time updates.
     */
    @Query("""
        SELECT * FROM records 
        WHERE name LIKE '%' || :query || '%' 
        ORDER BY 
            CASE WHEN name LIKE :query || '%' THEN 1 ELSE 2 END,
            name ASC
        LIMIT :limit
    """)
    fun searchRecordsByNameFlow(query: String, limit: Int = 50): Flow<List<RecordEntity>>
    
    /**
     * Search maintenances by description (case-insensitive, partial matches).
     */
    @Query("""
        SELECT * FROM maintenances 
        WHERE description LIKE '%' || :query || '%' 
           OR type LIKE '%' || :query || '%'
           OR performed_by LIKE '%' || :query || '%'
           OR location LIKE '%' || :query || '%'
           OR parts_replaced LIKE '%' || :query || '%'
           OR notes LIKE '%' || :query || '%'
        ORDER BY 
            CASE WHEN description LIKE :query || '%' THEN 1 
                 WHEN type LIKE :query || '%' THEN 2 
                 ELSE 3 END,
            maintenance_date DESC
        LIMIT :limit
    """)
    suspend fun searchMaintenancesByContent(query: String, limit: Int = 50): List<MaintenanceEntity>
    
    /**
     * Search maintenances by content as Flow.
     */
    @Query("""
        SELECT * FROM maintenances 
        WHERE description LIKE '%' || :query || '%' 
           OR type LIKE '%' || :query || '%'
           OR performed_by LIKE '%' || :query || '%'
           OR location LIKE '%' || :query || '%'
           OR parts_replaced LIKE '%' || :query || '%'
           OR notes LIKE '%' || :query || '%'
        ORDER BY 
            CASE WHEN description LIKE :query || '%' THEN 1 
                 WHEN type LIKE :query || '%' THEN 2 
                 ELSE 3 END,
            maintenance_date DESC
        LIMIT :limit
    """)
    fun searchMaintenancesByContentFlow(query: String, limit: Int = 50): Flow<List<MaintenanceEntity>>
    
    /**
     * Advanced search with multiple criteria.
     */
    @Query("""
        SELECT DISTINCT r.* FROM records r
        LEFT JOIN maintenances m ON r.id = m.record_id
        WHERE 1=1
            AND (:nameQuery IS NULL OR r.name LIKE '%' || :nameQuery || '%')
            AND (:maintenanceQuery IS NULL OR 
                m.description LIKE '%' || :maintenanceQuery || '%' OR
                m.type LIKE '%' || :maintenanceQuery || '%' OR
                m.performed_by LIKE '%' || :maintenanceQuery || '%' OR
                m.location LIKE '%' || :maintenanceQuery || '%')
            AND (:minCost IS NULL OR m.cost >= :minCost)
            AND (:maxCost IS NULL OR m.cost <= :maxCost)
            AND (:startDate IS NULL OR m.maintenance_date >= :startDate)
            AND (:endDate IS NULL OR m.maintenance_date <= :endDate)
        ORDER BY r.name ASC
        LIMIT :limit
    """)
    suspend fun advancedSearch(
        nameQuery: String?,
        maintenanceQuery: String?,
        minCost: Double?,
        maxCost: Double?,
        startDate: Long?,
        endDate: Long?,
        limit: Int = 100
    ): List<RecordEntity>
    
    /**
     * Search maintenances within date range.
     */
    @Query("""
        SELECT * FROM maintenances 
        WHERE maintenance_date BETWEEN :startDate AND :endDate
        ORDER BY maintenance_date DESC
        LIMIT :limit
    """)
    suspend fun searchMaintenancesByDateRange(
        startDate: Long,
        endDate: Long,
        limit: Int = 100
    ): List<MaintenanceEntity>
    
    /**
     * Search maintenances within cost range.
     */
    @Query("""
        SELECT * FROM maintenances 
        WHERE cost BETWEEN :minCost AND :maxCost
        ORDER BY cost DESC
        LIMIT :limit
    """)
    suspend fun searchMaintenancesByCostRange(
        minCost: Double,
        maxCost: Double,
        limit: Int = 100
    ): List<MaintenanceEntity>
    
    /**
     * Search by maintenance type.
     */
    @Query("""
        SELECT * FROM maintenances 
        WHERE type LIKE '%' || :type || '%'
        ORDER BY maintenance_date DESC
        LIMIT :limit
    """)
    suspend fun searchMaintenancesByType(type: String, limit: Int = 50): List<MaintenanceEntity>
    
    /**
     * Get maintenance types for autocomplete/suggestions.
     */
    @Query("""
        SELECT DISTINCT type FROM maintenances 
        WHERE type IS NOT NULL AND type != ''
        ORDER BY type ASC
    """)
    suspend fun getMaintenanceTypes(): List<String>
    
    /**
     * Get performers for autocomplete/suggestions.
     */
    @Query("""
        SELECT DISTINCT performed_by FROM maintenances 
        WHERE performed_by IS NOT NULL AND performed_by != ''
        ORDER BY performed_by ASC
    """)
    suspend fun getPerformers(): List<String>
    
    /**
     * Get locations for autocomplete/suggestions.
     */
    @Query("""
        SELECT DISTINCT location FROM maintenances 
        WHERE location IS NOT NULL AND location != ''
        ORDER BY location ASC
    """)
    suspend fun getLocations(): List<String>
    
    /**
     * Full-text search across all text fields in both records and maintenances.
     */
    @Query("""
        SELECT DISTINCT 'record' as type, r.id as id, r.name as title, 
               COALESCE(r.brand_model, '') as subtitle, COALESCE(r.description, '') as description
        FROM records r
        WHERE r.name LIKE '%' || :query || '%' 
           OR r.brand_model LIKE '%' || :query || '%'
           OR r.serial_number LIKE '%' || :query || '%'
           OR r.location LIKE '%' || :query || '%'
           OR r.description LIKE '%' || :query || '%'
        
        UNION ALL
        
        SELECT DISTINCT 'maintenance' as type, m.id as id, m.type as title,
               'Record: ' || r.name as subtitle, m.description as description
        FROM maintenances m
        INNER JOIN records r ON m.record_id = r.id
        WHERE m.description LIKE '%' || :query || '%'
           OR m.type LIKE '%' || :query || '%'
           OR m.performed_by LIKE '%' || :query || '%'
           OR m.location LIKE '%' || :query || '%'
           OR m.parts_replaced LIKE '%' || :query || '%'
           OR m.notes LIKE '%' || :query || '%'
        
        ORDER BY type, id
        LIMIT :limit
    """)
    suspend fun fullTextSearch(query: String, limit: Int = 50): List<SearchResultEntity>
    
    /**
     * Get search suggestions based on partial query.
     */
    @Query("""
        SELECT DISTINCT name as suggestion FROM records 
        WHERE name LIKE :query || '%'
        UNION
        SELECT DISTINCT type as suggestion FROM maintenances 
        WHERE type LIKE :query || '%'
        ORDER BY suggestion ASC
        LIMIT 10
    """)
    suspend fun getSearchSuggestions(query: String): List<String>
}


package com.maintenance.app.data.database.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.maintenance.app.data.database.entities.SearchHistoryEntity
import kotlinx.coroutines.flow.Flow

/**
 * Data Access Object for search history operations.
 */
@Dao
interface SearchHistoryDAO {
    
    /**
     * Insert or update search history entry.
     */
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertOrUpdateHistory(entry: SearchHistoryEntity): Long
    
    /**
     * Get search history ordered by timestamp (most recent first).
     */
    @Query("SELECT * FROM search_history ORDER BY timestamp DESC LIMIT :limit")
    suspend fun getSearchHistory(limit: Int = 20): List<SearchHistoryEntity>
    
    /**
     * Get search history as Flow.
     */
    @Query("SELECT * FROM search_history ORDER BY timestamp DESC LIMIT :limit")
    fun getSearchHistoryFlow(limit: Int = 20): Flow<List<SearchHistoryEntity>>
    
    /**
     * Get recent unique queries for suggestions.
     */
    @Query("""
        SELECT DISTINCT query FROM search_history 
        WHERE query LIKE :queryPrefix || '%'
        ORDER BY timestamp DESC 
        LIMIT :limit
    """)
    suspend fun getRecentQueries(queryPrefix: String, limit: Int = 5): List<String>
    
    /**
     * Delete specific history entry.
     */
    @Query("DELETE FROM search_history WHERE id = :entryId")
    suspend fun deleteHistoryEntry(entryId: Long)
    
    /**
     * Clear all search history.
     */
    @Query("DELETE FROM search_history")
    suspend fun clearAllHistory()
    
    /**
     * Delete old history entries (keep only the most recent ones).
     */
    @Query("""
        DELETE FROM search_history 
        WHERE id NOT IN (
            SELECT id FROM search_history 
            ORDER BY timestamp DESC 
            LIMIT :keepCount
        )
    """)
    suspend fun deleteOldEntries(keepCount: Int = 100)
    
    /**
     * Get search history count.
     */
    @Query("SELECT COUNT(*) FROM search_history")
    suspend fun getHistoryCount(): Int
}
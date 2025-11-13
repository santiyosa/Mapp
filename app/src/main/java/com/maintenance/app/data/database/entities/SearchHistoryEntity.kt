package com.maintenance.app.data.database.entities

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import com.maintenance.app.domain.model.SearchCriteria
import com.maintenance.app.domain.model.SearchHistoryEntry

/**
 * Room entity for search history.
 */
@Entity(tableName = "search_history")
data class SearchHistoryEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    
    @ColumnInfo(name = "query")
    val query: String,
    
    @ColumnInfo(name = "search_criteria")
    val searchCriteria: String, // JSON representation of SearchCriteria
    
    @ColumnInfo(name = "result_count")
    val resultCount: Int,
    
    @ColumnInfo(name = "timestamp")
    val timestamp: Long
) {
    /**
     * Convert to domain model.
     */
    fun toDomain(criteria: SearchCriteria): SearchHistoryEntry {
        return SearchHistoryEntry(
            id = id,
            query = query,
            criteria = criteria,
            resultCount = resultCount,
            timestamp = timestamp
        )
    }
    
    companion object {
        /**
         * Create from domain model.
         */
        fun fromDomain(entry: SearchHistoryEntry, criteriaJson: String): SearchHistoryEntity {
            return SearchHistoryEntity(
                id = entry.id,
                query = entry.query,
                searchCriteria = criteriaJson,
                resultCount = entry.resultCount,
                timestamp = entry.timestamp
            )
        }
    }
}
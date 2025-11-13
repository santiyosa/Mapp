package com.maintenance.app.data.database.daos

import androidx.room.*
import com.maintenance.app.data.database.entities.MaintenanceDraftEntity
import kotlinx.coroutines.flow.Flow

/**
 * Data Access Object for maintenance drafts.
 */
@Dao
interface MaintenanceDraftDao {
    
    /**
     * Get draft by record ID.
     */
    @Query("SELECT * FROM maintenance_drafts WHERE record_id = :recordId LIMIT 1")
    suspend fun getDraftByRecordId(recordId: Long): MaintenanceDraftEntity?
    
    /**
     * Get draft by record ID as Flow.
     */
    @Query("SELECT * FROM maintenance_drafts WHERE record_id = :recordId LIMIT 1")
    fun getDraftByRecordIdFlow(recordId: Long): Flow<MaintenanceDraftEntity?>
    
    /**
     * Get all drafts.
     */
    @Query("SELECT * FROM maintenance_drafts ORDER BY updated_at DESC")
    suspend fun getAllDrafts(): List<MaintenanceDraftEntity>
    
    /**
     * Get all drafts as Flow.
     */
    @Query("SELECT * FROM maintenance_drafts ORDER BY updated_at DESC")
    fun getAllDraftsFlow(): Flow<List<MaintenanceDraftEntity>>
    
    /**
     * Insert or update a draft.
     */
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertOrUpdateDraft(draft: MaintenanceDraftEntity): Long
    
    /**
     * Update existing draft.
     */
    @Update
    suspend fun updateDraft(draft: MaintenanceDraftEntity)
    
    /**
     * Delete draft by record ID.
     */
    @Query("DELETE FROM maintenance_drafts WHERE record_id = :recordId")
    suspend fun deleteDraftByRecordId(recordId: Long)
    
    /**
     * Delete draft by ID.
     */
    @Query("DELETE FROM maintenance_drafts WHERE id = :draftId")
    suspend fun deleteDraftById(draftId: Long)
    
    /**
     * Delete old drafts (older than specified days).
     */
    @Query("DELETE FROM maintenance_drafts WHERE updated_at < datetime('now', '-' || :days || ' days')")
    suspend fun deleteOldDrafts(days: Int = 30)
    
    /**
     * Get drafts count by record ID.
     */
    @Query("SELECT COUNT(*) FROM maintenance_drafts WHERE record_id = :recordId")
    suspend fun getDraftCountByRecordId(recordId: Long): Int
    
    /**
     * Check if draft exists for record.
     */
    @Query("SELECT EXISTS(SELECT 1 FROM maintenance_drafts WHERE record_id = :recordId)")
    suspend fun hasDraftForRecord(recordId: Long): Boolean
}
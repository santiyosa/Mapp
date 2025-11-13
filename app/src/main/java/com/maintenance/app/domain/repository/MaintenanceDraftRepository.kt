package com.maintenance.app.domain.repository

import com.maintenance.app.domain.model.MaintenanceDraft
import kotlinx.coroutines.flow.Flow

/**
 * Repository interface for maintenance drafts.
 */
interface MaintenanceDraftRepository {
    
    /**
     * Get draft by record ID.
     */
    suspend fun getDraftByRecordId(recordId: Long): MaintenanceDraft?
    
    /**
     * Get draft by record ID as Flow.
     */
    fun getDraftByRecordIdFlow(recordId: Long): Flow<MaintenanceDraft?>
    
    /**
     * Get all drafts.
     */
    suspend fun getAllDrafts(): List<MaintenanceDraft>
    
    /**
     * Get all drafts as Flow.
     */
    fun getAllDraftsFlow(): Flow<List<MaintenanceDraft>>
    
    /**
     * Save or update a draft.
     */
    suspend fun saveDraft(draft: MaintenanceDraft): Long
    
    /**
     * Delete draft by record ID.
     */
    suspend fun deleteDraftByRecordId(recordId: Long)
    
    /**
     * Delete draft by ID.
     */
    suspend fun deleteDraftById(draftId: Long)
    
    /**
     * Check if draft exists for record.
     */
    suspend fun hasDraftForRecord(recordId: Long): Boolean
    
    /**
     * Delete old drafts.
     */
    suspend fun deleteOldDrafts(days: Int = 30)
}
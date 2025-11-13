package com.maintenance.app.data.repositories

import com.maintenance.app.data.database.daos.MaintenanceDraftDao
import com.maintenance.app.data.database.entities.MaintenanceDraftEntity
import com.maintenance.app.domain.model.MaintenanceDraft
import com.maintenance.app.domain.repository.MaintenanceDraftRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Repository implementation for maintenance drafts.
 */
@Singleton
class MaintenanceDraftRepositoryImpl @Inject constructor(
    private val draftDao: MaintenanceDraftDao
) : MaintenanceDraftRepository {
    
    override suspend fun getDraftByRecordId(recordId: Long): MaintenanceDraft? {
        return draftDao.getDraftByRecordId(recordId)?.toDomain()
    }
    
    override fun getDraftByRecordIdFlow(recordId: Long): Flow<MaintenanceDraft?> {
        return draftDao.getDraftByRecordIdFlow(recordId).map { it?.toDomain() }
    }
    
    override suspend fun getAllDrafts(): List<MaintenanceDraft> {
        return draftDao.getAllDrafts().map { it.toDomain() }
    }
    
    override fun getAllDraftsFlow(): Flow<List<MaintenanceDraft>> {
        return draftDao.getAllDraftsFlow().map { entities ->
            entities.map { it.toDomain() }
        }
    }
    
    override suspend fun saveDraft(draft: MaintenanceDraft): Long {
        return draftDao.insertOrUpdateDraft(
            MaintenanceDraftEntity.fromDomain(draft.copy(
                updatedAt = java.time.LocalDateTime.now()
            ))
        )
    }
    
    override suspend fun deleteDraftByRecordId(recordId: Long) {
        draftDao.deleteDraftByRecordId(recordId)
    }
    
    override suspend fun deleteDraftById(draftId: Long) {
        draftDao.deleteDraftById(draftId)
    }
    
    override suspend fun hasDraftForRecord(recordId: Long): Boolean {
        return draftDao.hasDraftForRecord(recordId)
    }
    
    override suspend fun deleteOldDrafts(days: Int) {
        draftDao.deleteOldDrafts(days)
    }
}
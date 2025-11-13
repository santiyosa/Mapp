package com.maintenance.app.domain.usecases.drafts

import com.maintenance.app.domain.model.MaintenanceDraft
import com.maintenance.app.domain.repository.MaintenanceDraftRepository
import com.maintenance.app.domain.usecases.base.UseCase
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

/**
 * Use case for saving maintenance drafts.
 */
class SaveMaintenanceDraftUseCase @Inject constructor(
    private val draftRepository: MaintenanceDraftRepository
) : UseCase<SaveMaintenanceDraftUseCase.Params, Long>() {
    
    override suspend fun execute(parameters: Params): Long {
        return draftRepository.saveDraft(parameters.draft)
    }
    
    data class Params(
        val draft: MaintenanceDraft
    )
}

/**
 * Use case for loading maintenance drafts.
 */
class LoadMaintenanceDraftUseCase @Inject constructor(
    private val draftRepository: MaintenanceDraftRepository
) : UseCase<LoadMaintenanceDraftUseCase.Params, MaintenanceDraft?>() {
    
    override suspend fun execute(parameters: Params): MaintenanceDraft? {
        return draftRepository.getDraftByRecordId(parameters.recordId)
    }
    
    /**
     * Get draft as Flow for real-time updates.
     */
    fun executeAsFlow(recordId: Long): Flow<MaintenanceDraft?> {
        return draftRepository.getDraftByRecordIdFlow(recordId)
    }
    
    data class Params(
        val recordId: Long
    )
}

/**
 * Use case for deleting maintenance drafts.
 */
class DeleteMaintenanceDraftUseCase @Inject constructor(
    private val draftRepository: MaintenanceDraftRepository
) : UseCase<DeleteMaintenanceDraftUseCase.Params, Unit>() {
    
    override suspend fun execute(parameters: Params) {
        when {
            parameters.recordId != null -> {
                draftRepository.deleteDraftByRecordId(parameters.recordId)
            }
            parameters.draftId != null -> {
                draftRepository.deleteDraftById(parameters.draftId)
            }
        }
    }
    
    data class Params(
        val recordId: Long? = null,
        val draftId: Long? = null
    )
}

/**
 * Use case for getting all drafts.
 */
class GetAllMaintenanceDraftsUseCase @Inject constructor(
    private val draftRepository: MaintenanceDraftRepository
) : UseCase<Unit, List<MaintenanceDraft>>() {
    
    override suspend fun execute(parameters: Unit): List<MaintenanceDraft> {
        return draftRepository.getAllDrafts()
    }
    
    /**
     * Get all drafts as Flow for real-time updates.
     */
    fun executeAsFlow(): Flow<List<MaintenanceDraft>> {
        return draftRepository.getAllDraftsFlow()
    }
}

/**
 * Use case for checking if draft exists.
 */
class HasMaintenanceDraftUseCase @Inject constructor(
    private val draftRepository: MaintenanceDraftRepository
) : UseCase<HasMaintenanceDraftUseCase.Params, Boolean>() {
    
    override suspend fun execute(parameters: Params): Boolean {
        return draftRepository.hasDraftForRecord(parameters.recordId)
    }
    
    data class Params(
        val recordId: Long
    )
}
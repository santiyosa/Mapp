package com.maintenance.app.domain.usecases.records

import com.maintenance.app.domain.model.Record
import com.maintenance.app.domain.repository.RecordRepository
import com.maintenance.app.domain.usecases.base.FlowUseCaseNoParams
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

/**
 * Use case for getting all deleted (inactive) records for trash view.
 */
class GetDeletedRecordsUseCase @Inject constructor(
    private val recordRepository: RecordRepository
) : FlowUseCaseNoParams<Flow<List<Record>>>() {

    override suspend fun execute(): Flow<List<Record>> {
        return recordRepository.getDeletedRecords()
    }
}

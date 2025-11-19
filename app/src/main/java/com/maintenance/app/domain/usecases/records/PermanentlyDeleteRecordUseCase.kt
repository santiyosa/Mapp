package com.maintenance.app.domain.usecases.records

import com.maintenance.app.domain.repository.RecordRepository
import com.maintenance.app.domain.usecases.base.UseCase
import com.maintenance.app.utils.Result
import javax.inject.Inject

/**
 * Use case for permanently deleting a record (hard delete from trash).
 */
class PermanentlyDeleteRecordUseCase @Inject constructor(
    private val recordRepository: RecordRepository
) : UseCase<PermanentlyDeleteRecordUseCase.Params, Unit>() {

    override suspend fun execute(parameters: Params): Unit {
        require(parameters.recordId > 0) { "Record ID must be valid" }
        
        when (val result = recordRepository.deleteRecordById(parameters.recordId)) {
            is Result.Success -> return
            is Result.Error -> throw result.exception ?: Exception(result.message)
            is Result.Loading -> throw Exception("Unexpected loading state")
        }
    }

    data class Params(
        val recordId: Long
    )
}

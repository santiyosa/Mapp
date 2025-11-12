package com.maintenance.app.domain.usecases.records

import com.maintenance.app.domain.model.Record
import com.maintenance.app.domain.repository.RecordRepository
import com.maintenance.app.domain.usecases.base.UseCase
import com.maintenance.app.utils.Result
import javax.inject.Inject

/**
 * Use case for getting a single record by ID.
 */
class GetRecordByIdUseCase @Inject constructor(
    private val recordRepository: RecordRepository
) : UseCase<GetRecordByIdUseCase.Params, Record?>() {

    override suspend fun execute(parameters: Params): Record? {
        require(parameters.recordId > 0) { "Record ID must be valid" }
        
        return when (val result = recordRepository.getRecordById(parameters.recordId)) {
            is Result.Success -> result.data
            is Result.Error -> throw result.exception ?: Exception(result.message)
            is Result.Loading -> throw Exception("Unexpected loading state")
        }
    }

    data class Params(
        val recordId: Long
    )
}
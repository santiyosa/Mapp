package com.maintenance.app.domain.usecases.records

import com.maintenance.app.domain.model.Record
import com.maintenance.app.domain.repository.RecordRepository
import com.maintenance.app.domain.usecases.base.UseCase
import com.maintenance.app.utils.Result
import java.time.LocalDateTime
import javax.inject.Inject

/**
 * Use case for updating an existing record.
 */
class UpdateRecordUseCase @Inject constructor(
    private val recordRepository: RecordRepository
) : UseCase<UpdateRecordUseCase.Params, Unit>() {

    override suspend fun execute(parameters: Params): Unit {
        // Validate input
        require(parameters.record.id > 0) { "Record ID must be valid" }
        require(parameters.record.name.isNotBlank()) { "Record name cannot be blank" }
        
        // Check if record exists
        when (val result = recordRepository.getRecordById(parameters.record.id)) {
            is Result.Success -> result.data
            is Result.Error -> throw result.exception ?: Exception(result.message)
            is Result.Loading -> throw Exception("Unexpected loading state")
        } ?: throw Exception("Record not found")

        // Update the record with current timestamp
        val updatedRecord = parameters.record.copy(
            name = parameters.record.name.trim(),
            description = parameters.record.description?.trim(),
            category = parameters.record.category?.trim(),
            location = parameters.record.location?.trim(),
            brandModel = parameters.record.brandModel?.trim(),
            serialNumber = parameters.record.serialNumber?.trim(),
            notes = parameters.record.notes?.trim(),
            updatedDate = LocalDateTime.now()
        )

        when (val result = recordRepository.updateRecord(updatedRecord)) {
            is Result.Success -> return
            is Result.Error -> throw result.exception ?: Exception(result.message)
            is Result.Loading -> throw Exception("Unexpected loading state")
        }
    }

    data class Params(
        val record: Record
    )
}
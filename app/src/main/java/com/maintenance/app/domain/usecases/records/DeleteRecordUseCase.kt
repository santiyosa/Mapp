package com.maintenance.app.domain.usecases.records

import com.maintenance.app.domain.repository.MaintenanceRepository
import com.maintenance.app.domain.repository.RecordRepository
import com.maintenance.app.domain.usecases.base.UseCase
import com.maintenance.app.utils.Result
import javax.inject.Inject

/**
 * Use case for deleting a record.
 * This will also delete all associated maintenances.
 */
class DeleteRecordUseCase @Inject constructor(
    private val recordRepository: RecordRepository,
    private val maintenanceRepository: MaintenanceRepository
) : UseCase<DeleteRecordUseCase.Params, Unit>() {

    override suspend fun execute(parameters: Params): Unit {
        // Validate input
        require(parameters.recordId > 0) { "Record ID must be valid" }
        
        // Check if record exists
        val existingRecord = when (val result = recordRepository.getRecordById(parameters.recordId)) {
            is Result.Success -> result.data
            is Result.Error -> throw result.exception ?: Exception(result.message)
            is Result.Loading -> throw Exception("Unexpected loading state")
        } ?: throw Exception("Record not found")

        // If soft delete is requested, just mark as inactive
        if (parameters.softDelete) {
            when (val result = recordRepository.softDeleteRecord(parameters.recordId)) {
                is Result.Success -> return
                is Result.Error -> throw result.exception ?: Exception(result.message)
                is Result.Loading -> throw Exception("Unexpected loading state")
            }
        } else {
            // Hard delete: first delete all maintenances, then delete record
            when (val maintenanceResult = maintenanceRepository.deleteMaintenancesByRecordId(parameters.recordId)) {
                is Result.Success -> {
                    // Now delete the record
                    when (val recordResult = recordRepository.deleteRecordById(parameters.recordId)) {
                        is Result.Success -> return
                        is Result.Error -> throw recordResult.exception ?: Exception(recordResult.message)
                        is Result.Loading -> throw Exception("Unexpected loading state")
                    }
                }
                is Result.Error -> throw maintenanceResult.exception ?: Exception(maintenanceResult.message)
                is Result.Loading -> throw Exception("Unexpected loading state")
            }
        }
    }

    data class Params(
        val recordId: Long,
        val softDelete: Boolean = true // Default to soft delete for safety
    )
}
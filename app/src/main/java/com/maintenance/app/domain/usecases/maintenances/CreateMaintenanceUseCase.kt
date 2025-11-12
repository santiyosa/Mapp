package com.maintenance.app.domain.usecases.maintenances

import com.maintenance.app.domain.model.Maintenance
import com.maintenance.app.domain.repository.MaintenanceRepository
import com.maintenance.app.domain.repository.RecordRepository
import com.maintenance.app.domain.usecases.base.UseCase
import com.maintenance.app.utils.Result
import java.time.LocalDateTime
import javax.inject.Inject

/**
 * Use case for creating a new maintenance entry.
 */
class CreateMaintenanceUseCase @Inject constructor(
    private val maintenanceRepository: MaintenanceRepository,
    private val recordRepository: RecordRepository
) : UseCase<CreateMaintenanceUseCase.Params, Long>() {

    override suspend fun execute(parameters: Params): Long {
        // Validate input
        require(parameters.recordId > 0) { "Record ID must be valid" }
        require(parameters.description.isNotBlank()) { "Maintenance description cannot be blank" }
        require(parameters.type.isNotBlank()) { "Maintenance type cannot be blank" }
        
        // Verify that the record exists
        val recordExists = when (val result = recordRepository.getRecordById(parameters.recordId)) {
            is Result.Success -> result.data != null
            is Result.Error -> throw result.exception ?: Exception(result.message)
            is Result.Loading -> throw Exception("Unexpected loading state")
        }
        
        if (!recordExists) {
            throw Exception("Record with ID ${parameters.recordId} does not exist")
        }

        val now = LocalDateTime.now()
        
        val maintenance = Maintenance(
            recordId = parameters.recordId,
            maintenanceDate = parameters.maintenanceDate,
            description = parameters.description.trim(),
            type = parameters.type.trim(),
            cost = parameters.cost,
            currency = parameters.currency,
            performedBy = parameters.performedBy?.trim(),
            location = parameters.location?.trim(),
            durationMinutes = parameters.durationMinutes,
            partsReplaced = parameters.partsReplaced?.trim(),
            nextMaintenanceDue = parameters.nextMaintenanceDue,
            priority = parameters.priority,
            status = parameters.status,
            imagesPaths = parameters.imagesPaths,
            createdDate = now,
            updatedDate = now,
            notes = parameters.notes?.trim(),
            isRecurring = parameters.isRecurring,
            recurrenceIntervalDays = parameters.recurrenceIntervalDays
        )

        // Create the maintenance
        val maintenanceId = when (val result = maintenanceRepository.createMaintenance(maintenance)) {
            is Result.Success -> result.data
            is Result.Error -> throw result.exception ?: Exception(result.message)
            is Result.Loading -> throw Exception("Unexpected loading state")
        }

        // Update the record's last maintenance date
        when (val result = recordRepository.updateLastMaintenanceDate(parameters.recordId, parameters.maintenanceDate)) {
            is Result.Success -> return maintenanceId
            is Result.Error -> {
                // Note: We could rollback the maintenance creation here, but for simplicity we'll just log
                throw result.exception ?: Exception("Failed to update record's last maintenance date: ${result.message}")
            }
            is Result.Loading -> throw Exception("Unexpected loading state")
        }
    }

    data class Params(
        val recordId: Long,
        val maintenanceDate: LocalDateTime,
        val description: String,
        val type: String,
        val cost: java.math.BigDecimal? = null,
        val currency: String = "USD",
        val performedBy: String? = null,
        val location: String? = null,
        val durationMinutes: Int? = null,
        val partsReplaced: String? = null,
        val nextMaintenanceDue: LocalDateTime? = null,
        val priority: Maintenance.Priority = Maintenance.Priority.MEDIUM,
        val status: Maintenance.Status = Maintenance.Status.COMPLETED,
        val imagesPaths: List<String> = emptyList(),
        val notes: String? = null,
        val isRecurring: Boolean = false,
        val recurrenceIntervalDays: Int? = null
    )
}
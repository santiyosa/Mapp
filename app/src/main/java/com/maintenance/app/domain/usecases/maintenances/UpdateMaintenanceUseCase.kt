package com.maintenance.app.domain.usecases.maintenances

import com.maintenance.app.domain.model.Maintenance
import com.maintenance.app.domain.repository.MaintenanceRepository
import com.maintenance.app.domain.usecases.base.UseCase
import com.maintenance.app.utils.Result
import java.time.LocalDateTime
import javax.inject.Inject

/**
 * Use case for updating an existing maintenance entry.
 */
class UpdateMaintenanceUseCase @Inject constructor(
    private val maintenanceRepository: MaintenanceRepository
) : UseCase<UpdateMaintenanceUseCase.Params, Unit>() {

    override suspend fun execute(parameters: Params): Unit {
        // Validate input
        require(parameters.maintenance.id > 0) { "Maintenance ID must be valid" }
        require(parameters.maintenance.description.isNotBlank()) { "Maintenance description cannot be blank" }
        require(parameters.maintenance.type.isNotBlank()) { "Maintenance type cannot be blank" }
        
        // Check if maintenance exists
        val existingMaintenance = when (val result = maintenanceRepository.getMaintenanceById(parameters.maintenance.id)) {
            is Result.Success -> result.data
            is Result.Error -> throw result.exception ?: Exception(result.message)
            is Result.Loading -> throw Exception("Unexpected loading state")
        } ?: throw Exception("Maintenance not found")

        // Update the maintenance with current timestamp
        val updatedMaintenance = parameters.maintenance.copy(
            description = parameters.maintenance.description.trim(),
            type = parameters.maintenance.type.trim(),
            performedBy = parameters.maintenance.performedBy?.trim(),
            location = parameters.maintenance.location?.trim(),
            partsReplaced = parameters.maintenance.partsReplaced?.trim(),
            notes = parameters.maintenance.notes?.trim(),
            updatedDate = LocalDateTime.now()
        )

        when (val result = maintenanceRepository.updateMaintenance(updatedMaintenance)) {
            is Result.Success -> return
            is Result.Error -> throw result.exception ?: Exception(result.message)
            is Result.Loading -> throw Exception("Unexpected loading state")
        }
    }

    data class Params(
        val maintenance: Maintenance
    )
}
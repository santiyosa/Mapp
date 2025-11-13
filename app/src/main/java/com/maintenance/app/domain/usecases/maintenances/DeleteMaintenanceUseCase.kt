package com.maintenance.app.domain.usecases.maintenances

import com.maintenance.app.domain.repository.MaintenanceRepository
import com.maintenance.app.domain.usecases.base.UseCase
import com.maintenance.app.utils.Result
import javax.inject.Inject

/**
 * Use case for deleting a maintenance entry.
 */
class DeleteMaintenanceUseCase @Inject constructor(
    private val maintenanceRepository: MaintenanceRepository
) : UseCase<DeleteMaintenanceUseCase.Params, Unit>() {

    override suspend fun execute(parameters: Params): Unit {
        // Validate input
        require(parameters.maintenanceId > 0) { "Maintenance ID must be valid" }
        
        // Check if maintenance exists
        when (val result = maintenanceRepository.getMaintenanceById(parameters.maintenanceId)) {
            is Result.Success -> result.data
            is Result.Error -> throw result.exception ?: Exception(result.message)
            is Result.Loading -> throw Exception("Unexpected loading state")
        } ?: throw Exception("Maintenance not found")

        // Delete the maintenance
        when (val result = maintenanceRepository.deleteMaintenanceById(parameters.maintenanceId)) {
            is Result.Success -> return
            is Result.Error -> throw result.exception ?: Exception(result.message)
            is Result.Loading -> throw Exception("Unexpected loading state")
        }
    }

    data class Params(
        val maintenanceId: Long
    )
}
package com.maintenance.app.domain.usecases.maintenances

import com.maintenance.app.domain.model.Maintenance
import com.maintenance.app.domain.repository.MaintenanceRepository
import com.maintenance.app.domain.usecases.base.UseCase
import com.maintenance.app.utils.Result
import javax.inject.Inject

/**
 * Use case for getting a single maintenance by ID.
 */
class GetMaintenanceByIdUseCase @Inject constructor(
    private val maintenanceRepository: MaintenanceRepository
) : UseCase<GetMaintenanceByIdUseCase.Params, Maintenance?>() {

    override suspend fun execute(parameters: Params): Maintenance? {
        require(parameters.maintenanceId > 0) { "Maintenance ID must be valid" }
        
        return when (val result = maintenanceRepository.getMaintenanceById(parameters.maintenanceId)) {
            is Result.Success -> result.data
            is Result.Error -> throw result.exception ?: Exception(result.message)
            is Result.Loading -> throw Exception("Unexpected loading state")
        }
    }

    data class Params(
        val maintenanceId: Long
    )
}
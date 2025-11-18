package com.maintenance.app.domain.usecases.maintenance

import com.maintenance.app.domain.model.Maintenance
import com.maintenance.app.domain.repository.MaintenanceRepository
import com.maintenance.app.domain.usecases.base.UseCase
import javax.inject.Inject

class GetMaintenanceUseCase @Inject constructor(
    private val repository: MaintenanceRepository
) : UseCase<GetMaintenanceUseCase.Params, Maintenance?>() {

    override suspend fun execute(parameters: Params): Maintenance? {
        return when (val result = repository.getMaintenanceById(parameters.maintenanceId)) {
            is com.maintenance.app.utils.Result.Success -> result.data
            is com.maintenance.app.utils.Result.Error -> throw Exception(result.message)
            is com.maintenance.app.utils.Result.Loading -> throw Exception("Loading")
        }
    }

    data class Params(val maintenanceId: Long)
}

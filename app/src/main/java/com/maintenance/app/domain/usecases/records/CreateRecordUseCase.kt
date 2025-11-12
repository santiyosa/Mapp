package com.maintenance.app.domain.usecases.records

import com.maintenance.app.domain.model.Record
import com.maintenance.app.domain.repository.RecordRepository
import com.maintenance.app.domain.usecases.base.UseCase
import com.maintenance.app.utils.Result
import java.time.LocalDateTime
import javax.inject.Inject

/**
 * Use case for creating a new record.
 */
class CreateRecordUseCase @Inject constructor(
    private val recordRepository: RecordRepository
) : UseCase<CreateRecordUseCase.Params, Long>() {

    override suspend fun execute(parameters: Params): Long {
        // Validate input
        require(parameters.name.isNotBlank()) { "Record name cannot be blank" }
        
        val now = LocalDateTime.now()
        
        val record = Record(
            name = parameters.name.trim(),
            description = parameters.description?.trim(),
            imagePath = parameters.imagePath,
            createdDate = now,
            lastMaintenanceDate = null,
            updatedDate = now,
            isActive = true,
            category = parameters.category?.trim(),
            location = parameters.location?.trim(),
            brandModel = parameters.brandModel?.trim(),
            serialNumber = parameters.serialNumber?.trim(),
            purchaseDate = parameters.purchaseDate,
            warrantyExpiryDate = parameters.warrantyExpiryDate,
            notes = parameters.notes?.trim()
        )

        return when (val result = recordRepository.createRecord(record)) {
            is Result.Success -> result.data
            is Result.Error -> throw result.exception ?: Exception(result.message)
            is Result.Loading -> throw Exception("Unexpected loading state")
        }
    }

    data class Params(
        val name: String,
        val description: String? = null,
        val imagePath: String? = null,
        val category: String? = null,
        val location: String? = null,
        val brandModel: String? = null,
        val serialNumber: String? = null,
        val purchaseDate: LocalDateTime? = null,
        val warrantyExpiryDate: LocalDateTime? = null,
        val notes: String? = null
    )
}
package com.maintenance.app.data.mappers

import com.maintenance.app.data.database.entities.MaintenanceEntity
import com.maintenance.app.data.database.entities.RecordEntity
import com.maintenance.app.domain.model.Maintenance
import com.maintenance.app.domain.model.Record

/**
 * Extension functions to map between data layer entities and domain layer entities.
 */

// Record Entity Mappers
fun RecordEntity.toDomain(): Record {
    return Record(
        id = id,
        name = name,
        description = description,
        imagePath = imagePath,
        createdDate = createdDate,
        lastMaintenanceDate = lastMaintenanceDate,
        updatedDate = updatedDate,
        isActive = isActive,
        category = category,
        location = location,
        brandModel = brandModel,
        serialNumber = serialNumber,
        purchaseDate = purchaseDate,
        warrantyExpiryDate = warrantyExpiryDate,
        notes = notes
    )
}

fun Record.toEntity(): RecordEntity {
    return RecordEntity(
        id = id,
        name = name,
        description = description,
        imagePath = imagePath,
        createdDate = createdDate,
        lastMaintenanceDate = lastMaintenanceDate,
        updatedDate = updatedDate,
        isActive = isActive,
        category = category,
        location = location,
        brandModel = brandModel,
        serialNumber = serialNumber,
        purchaseDate = purchaseDate,
        warrantyExpiryDate = warrantyExpiryDate,
        notes = notes
    )
}

// Maintenance Entity Mappers
fun MaintenanceEntity.toDomain(): Maintenance {
    return Maintenance(
        id = id,
        recordId = recordId,
        maintenanceDate = maintenanceDate,
        description = description,
        type = type,
        cost = cost,
        currency = currency,
        performedBy = performedBy,
        location = location,
        durationMinutes = durationMinutes,
        partsReplaced = partsReplaced,
        nextMaintenanceDue = nextMaintenanceDue,
        priority = when (priority) {
            "HIGH" -> Maintenance.Priority.HIGH
            "LOW" -> Maintenance.Priority.LOW
            else -> Maintenance.Priority.MEDIUM
        },
        status = when (status) {
            "PENDING" -> Maintenance.Status.PENDING
            "IN_PROGRESS" -> Maintenance.Status.IN_PROGRESS
            else -> Maintenance.Status.COMPLETED
        },
        imagesPaths = imagesPaths?.split(",")?.filter { it.isNotBlank() } ?: emptyList(),
        createdDate = createdDate,
        updatedDate = updatedDate,
        notes = notes,
        isRecurring = isRecurring,
        recurrenceIntervalDays = recurrenceIntervalDays
    )
}

fun Maintenance.toEntity(): MaintenanceEntity {
    return MaintenanceEntity(
        id = id,
        recordId = recordId,
        maintenanceDate = maintenanceDate,
        description = description,
        type = type,
        cost = cost,
        currency = currency,
        performedBy = performedBy,
        location = location,
        durationMinutes = durationMinutes,
        partsReplaced = partsReplaced,
        nextMaintenanceDue = nextMaintenanceDue,
        priority = priority.name,
        status = status.name,
        imagesPaths = if (imagesPaths.isNotEmpty()) imagesPaths.joinToString(",") else null,
        createdDate = createdDate,
        updatedDate = updatedDate,
        notes = notes,
        isRecurring = isRecurring,
        recurrenceIntervalDays = recurrenceIntervalDays
    )
}

// Extension functions for lists
fun List<RecordEntity>.toDomain(): List<Record> = map { it.toDomain() }
fun List<Record>.toEntity(): List<RecordEntity> = map { it.toEntity() }

fun List<MaintenanceEntity>.toMaintenanceDomain(): List<Maintenance> = map { it.toDomain() }
fun List<Maintenance>.toMaintenanceEntity(): List<MaintenanceEntity> = map { it.toEntity() }
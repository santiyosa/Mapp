package com.maintenance.app.domain.entities

import java.time.LocalDateTime

/**
 * Domain entity representing a maintenance record.
 * This is the clean representation used in the domain layer,
 * independent of data layer implementation details.
 */
data class Record(
    val id: Long = 0,
    val name: String,
    val description: String? = null,
    val imagePath: String? = null,
    val createdDate: LocalDateTime,
    val lastMaintenanceDate: LocalDateTime? = null,
    val updatedDate: LocalDateTime,
    val isActive: Boolean = true,
    val category: String? = null,
    val location: String? = null,
    val brandModel: String? = null,
    val serialNumber: String? = null,
    val purchaseDate: LocalDateTime? = null,
    val warrantyExpiryDate: LocalDateTime? = null,
    val notes: String? = null
) {
    /**
     * Checks if warranty is still valid.
     */
    fun isWarrantyValid(): Boolean {
        return warrantyExpiryDate?.let { it.isAfter(LocalDateTime.now()) } ?: false
    }

    /**
     * Gets the number of days since creation.
     */
    fun getDaysSinceCreation(): Long {
        return java.time.temporal.ChronoUnit.DAYS.between(createdDate, LocalDateTime.now())
    }

    /**
     * Gets the number of days since last maintenance.
     */
    fun getDaysSinceLastMaintenance(): Long? {
        return lastMaintenanceDate?.let {
            java.time.temporal.ChronoUnit.DAYS.between(it, LocalDateTime.now())
        }
    }

    /**
     * Checks if maintenance is overdue based on days threshold.
     */
    fun isMaintenanceOverdue(thresholdDays: Long): Boolean {
        return getDaysSinceLastMaintenance()?.let { it > thresholdDays } ?: true
    }

    /**
     * Gets display name including brand and model if available.
     */
    fun getDisplayName(): String {
        return if (brandModel.isNullOrBlank()) {
            name
        } else {
            "$name ($brandModel)"
        }
    }
}
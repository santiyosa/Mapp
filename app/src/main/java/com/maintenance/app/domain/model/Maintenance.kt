package com.maintenance.app.domain.model

import java.math.BigDecimal
import java.time.LocalDateTime

/**
 * Domain model representing a maintenance entry.
 */
data class Maintenance(
    val id: Long = 0,
    val recordId: Long,
    val maintenanceDate: LocalDateTime,
    val description: String,
    val type: String,
    val cost: BigDecimal? = null,
    val currency: String = "USD",
    val performedBy: String? = null,
    val location: String? = null,
    val durationMinutes: Int? = null,
    val partsReplaced: String? = null,
    val nextMaintenanceDue: LocalDateTime? = null,
    val priority: Priority = Priority.MEDIUM,
    val status: Status = Status.COMPLETED,
    val imagesPaths: List<String> = emptyList(),
    val createdDate: LocalDateTime,
    val updatedDate: LocalDateTime,
    val notes: String? = null,
    val isRecurring: Boolean = false,
    val recurrenceIntervalDays: Int? = null
) {

    enum class Priority(val displayName: String) {
        HIGH("Alta"),
        MEDIUM("Media"),
        LOW("Baja")
    }

    enum class Status(val displayName: String) {
        COMPLETED("Completado"),
        PENDING("Pendiente"),
        IN_PROGRESS("En Progreso")
    }

    /**
     * Gets the cost as a formatted string with currency.
     */
    fun getFormattedCost(): String? {
        return cost?.let { "${currency} ${it.setScale(2)}" }
    }

    /**
     * Gets the duration as a formatted string.
     */
    fun getFormattedDuration(): String? {
        return durationMinutes?.let {
            when {
                it < 60 -> "${it}m"
                it % 60 == 0 -> "${it / 60}h"
                else -> "${it / 60}h ${it % 60}m"
            }
        }
    }

    /**
     * Checks if maintenance is overdue.
     */
    fun isOverdue(): Boolean {
        return nextMaintenanceDue?.let { it.isBefore(LocalDateTime.now()) } ?: false
    }

    /**
     * Gets the number of days until next maintenance is due.
     */
    fun getDaysUntilDue(): Long? {
        return nextMaintenanceDue?.let {
            java.time.temporal.ChronoUnit.DAYS.between(LocalDateTime.now(), it)
        }
    }

    /**
     * Gets the number of days since this maintenance was performed.
     */
    fun getDaysSincePerformed(): Long {
        return java.time.temporal.ChronoUnit.DAYS.between(maintenanceDate, LocalDateTime.now())
    }

    /**
     * Checks if this maintenance has images.
     */
    fun hasImages(): Boolean = imagesPaths.isNotEmpty()

    /**
     * Gets the next maintenance due date based on recurrence.
     */
    fun calculateNextMaintenanceDue(): LocalDateTime? {
        return if (isRecurring && recurrenceIntervalDays != null) {
            maintenanceDate.plusDays(recurrenceIntervalDays.toLong())
        } else {
            nextMaintenanceDue
        }
    }
}
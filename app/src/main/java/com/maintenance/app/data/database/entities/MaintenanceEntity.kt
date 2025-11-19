package com.maintenance.app.data.database.entities

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey
import java.math.BigDecimal
import java.time.LocalDateTime

/**
 * Room entity representing a maintenance entry.
 * This entity stores information about maintenance activities performed on records.
 */
@Entity(
    tableName = "maintenances",
    foreignKeys = [
        ForeignKey(
            entity = RecordEntity::class,
            parentColumns = arrayOf("id"),
            childColumns = arrayOf("record_id"),
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [
        Index(value = ["record_id"], unique = false),
        Index(value = ["maintenance_date"], unique = false),
        Index(value = ["type"], unique = false),
        Index(value = ["cost"], unique = false)
    ]
)
data class MaintenanceEntity(
    @PrimaryKey(autoGenerate = true)
    @ColumnInfo(name = "id")
    val id: Long = 0,

    @ColumnInfo(name = "record_id")
    val recordId: Long,

    @ColumnInfo(name = "maintenance_date")
    val maintenanceDate: LocalDateTime,

    @ColumnInfo(name = "description")
    val description: String,

    @ColumnInfo(name = "type")
    val type: String,

    @ColumnInfo(name = "cost")
    val cost: BigDecimal? = null,

    @ColumnInfo(name = "currency")
    val currency: String = "COP",

    @ColumnInfo(name = "performed_by")
    val performedBy: String? = null,

    @ColumnInfo(name = "location")
    val location: String? = null,

    @ColumnInfo(name = "duration_minutes")
    val durationMinutes: Int? = null,

    @ColumnInfo(name = "parts_replaced")
    val partsReplaced: String? = null,

    @ColumnInfo(name = "next_maintenance_due")
    val nextMaintenanceDue: LocalDateTime? = null,

    @ColumnInfo(name = "priority")
    val priority: String = "MEDIUM", // HIGH, MEDIUM, LOW

    @ColumnInfo(name = "status")
    val status: String = "COMPLETED", // COMPLETED, PENDING, IN_PROGRESS

    @ColumnInfo(name = "images_paths")
    val imagesPaths: String? = null, // JSON array of image paths

    @ColumnInfo(name = "created_date")
    val createdDate: LocalDateTime,

    @ColumnInfo(name = "updated_date")
    val updatedDate: LocalDateTime,

    @ColumnInfo(name = "notes")
    val notes: String? = null,

    @ColumnInfo(name = "is_recurring")
    val isRecurring: Boolean = false,

    @ColumnInfo(name = "recurrence_interval_days")
    val recurrenceIntervalDays: Int? = null
)
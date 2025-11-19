package com.maintenance.app.data.database.entities

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey
import com.maintenance.app.domain.model.Maintenance
import com.maintenance.app.domain.model.MaintenanceDraft
import java.time.LocalDateTime

/**
 * Room entity for maintenance drafts.
 */
@Entity(
    tableName = "maintenance_drafts",
    foreignKeys = [
        ForeignKey(
            entity = RecordEntity::class,
            parentColumns = ["id"],
            childColumns = ["record_id"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [
        Index(value = ["record_id"])
    ]
)
data class MaintenanceDraftEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    
    @ColumnInfo(name = "record_id")
    val recordId: Long,
    
    @ColumnInfo(name = "description")
    val description: String = "",
    
    @ColumnInfo(name = "type")
    val type: String = "",
    
    @ColumnInfo(name = "cost")
    val cost: String = "",
    
    @ColumnInfo(name = "currency")
    val currency: String = "COP",
    
    @ColumnInfo(name = "performed_by")
    val performedBy: String = "",
    
    @ColumnInfo(name = "location")
    val location: String = "",
    
    @ColumnInfo(name = "duration_minutes")
    val durationMinutes: String = "",
    
    @ColumnInfo(name = "parts_replaced")
    val partsReplaced: String = "",
    
    @ColumnInfo(name = "notes")
    val notes: String = "",
    
    @ColumnInfo(name = "priority")
    val priority: String = Maintenance.Priority.MEDIUM.name,
    
    @ColumnInfo(name = "is_recurring")
    val isRecurring: Boolean = false,
    
    @ColumnInfo(name = "recurrence_interval_days")
    val recurrenceIntervalDays: String = "",
    
    @ColumnInfo(name = "selected_images")
    val selectedImages: String = "", // JSON string of image paths
    
    @ColumnInfo(name = "created_at")
    val createdAt: LocalDateTime = LocalDateTime.now(),
    
    @ColumnInfo(name = "updated_at")
    val updatedAt: LocalDateTime = LocalDateTime.now()
) {
    /**
     * Convert to domain model.
     */
    fun toDomain(): MaintenanceDraft {
        return MaintenanceDraft(
            id = id,
            recordId = recordId,
            description = description,
            type = type,
            cost = cost,
            currency = currency,
            performedBy = performedBy,
            location = location,
            durationMinutes = durationMinutes,
            partsReplaced = partsReplaced,
            notes = notes,
            priority = Maintenance.Priority.valueOf(priority),
            isRecurring = isRecurring,
            recurrenceIntervalDays = recurrenceIntervalDays,
            selectedImages = if (selectedImages.isNotBlank()) {
                selectedImages.split(",").filter { it.isNotBlank() }
            } else {
                emptyList()
            },
            createdAt = createdAt,
            updatedAt = updatedAt
        )
    }
    
    companion object {
        /**
         * Create from domain model.
         */
        fun fromDomain(draft: MaintenanceDraft): MaintenanceDraftEntity {
            return MaintenanceDraftEntity(
                id = draft.id,
                recordId = draft.recordId,
                description = draft.description,
                type = draft.type,
                cost = draft.cost,
                currency = draft.currency,
                performedBy = draft.performedBy,
                location = draft.location,
                durationMinutes = draft.durationMinutes,
                partsReplaced = draft.partsReplaced,
                notes = draft.notes,
                priority = draft.priority.name,
                isRecurring = draft.isRecurring,
                recurrenceIntervalDays = draft.recurrenceIntervalDays,
                selectedImages = draft.selectedImages.joinToString(","),
                createdAt = draft.createdAt,
                updatedAt = draft.updatedAt
            )
        }
    }
}
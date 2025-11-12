package com.maintenance.app.data.database.entities

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey
import java.time.LocalDateTime

/**
 * Room entity representing a maintenance record.
 * This entity stores information about items that require maintenance tracking.
 */
@Entity(
    tableName = "records",
    indices = [
        Index(value = ["name"], unique = false),
        Index(value = ["created_date"], unique = false),
        Index(value = ["last_maintenance_date"], unique = false)
    ]
)
data class RecordEntity(
    @PrimaryKey(autoGenerate = true)
    @ColumnInfo(name = "id")
    val id: Long = 0,

    @ColumnInfo(name = "name")
    val name: String,

    @ColumnInfo(name = "description")
    val description: String? = null,

    @ColumnInfo(name = "image_path")
    val imagePath: String? = null,

    @ColumnInfo(name = "created_date")
    val createdDate: LocalDateTime,

    @ColumnInfo(name = "last_maintenance_date")
    val lastMaintenanceDate: LocalDateTime? = null,

    @ColumnInfo(name = "updated_date")
    val updatedDate: LocalDateTime,

    @ColumnInfo(name = "is_active")
    val isActive: Boolean = true,

    @ColumnInfo(name = "category")
    val category: String? = null,

    @ColumnInfo(name = "location")
    val location: String? = null,

    @ColumnInfo(name = "brand_model")
    val brandModel: String? = null,

    @ColumnInfo(name = "serial_number")
    val serialNumber: String? = null,

    @ColumnInfo(name = "purchase_date")
    val purchaseDate: LocalDateTime? = null,

    @ColumnInfo(name = "warranty_expiry_date")
    val warrantyExpiryDate: LocalDateTime? = null,

    @ColumnInfo(name = "notes")
    val notes: String? = null
)
package com.maintenance.app.data.database.entities

/**
 * Entity class for unified search results.
 * This represents a search result that can be either a record or maintenance.
 */
data class SearchResultEntity(
    val type: String, // 'record' or 'maintenance'
    val id: Long,
    val title: String,
    val subtitle: String,
    val description: String,
    val recordId: Long? = null  // For 'maintenance' type, the associated record ID
)
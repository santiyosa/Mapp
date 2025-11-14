package com.maintenance.app.presentation.viewmodels.search

import java.math.BigDecimal
import java.time.LocalDate

/**
 * Filters para búsqueda avanzada
 */
data class AdvancedSearchFilters(
    val minCost: BigDecimal? = null,
    val maxCost: BigDecimal? = null,
    val startDate: LocalDate? = null,
    val endDate: LocalDate? = null,
    val categories: List<String> = emptyList(),
    val recordTypes: List<String> = emptyList(),
    val maintenanceTypes: List<String> = emptyList(),
    val sortBy: SortOption = SortOption.RELEVANCE,
    val isActive: Boolean = true
) {
    fun isEmpty(): Boolean {
        return minCost == null && maxCost == null && 
               startDate == null && endDate == null &&
               categories.isEmpty() && recordTypes.isEmpty() &&
               maintenanceTypes.isEmpty() && isActive
    }
}

enum class SortOption(val label: String) {
    RELEVANCE("Relevancia"),
    NAME("Nombre"),
    DATE_NEWEST("Más reciente"),
    DATE_OLDEST("Más antiguo"),
    COST_HIGH("Mayor costo"),
    COST_LOW("Menor costo")
}

data class FilterOptions(
    val categories: List<String> = emptyList(),
    val recordTypes: List<String> = emptyList(),
    val maintenanceTypes: List<String> = emptyList(),
    val minPrice: BigDecimal = BigDecimal.ZERO,
    val maxPrice: BigDecimal = BigDecimal("999999"),
    val dateRangeStart: LocalDate? = null,
    val dateRangeEnd: LocalDate? = null
)

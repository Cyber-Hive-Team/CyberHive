package org.example.data.dataholder

import org.example.domain.model.Warehouse

data class WarehouseMappingResult(
    val warehouse: Warehouse?,
    val warnings: List<String>
)


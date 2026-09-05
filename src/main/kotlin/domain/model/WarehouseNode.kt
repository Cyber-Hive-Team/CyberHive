package org.example.domain.model

data class WarehouseNode(
    val warehouse: Warehouse,
    val level: WarehouseLevel,
    val parent: WarehouseNode?,
    val children: MutableList<WarehouseNode> = mutableListOf()
)

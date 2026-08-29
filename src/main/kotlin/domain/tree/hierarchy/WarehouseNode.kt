package org.example.domain.tree.hierarchy

import org.example.domain.model.Warehouse

data class WarehouseNode(
    val warehouse: Warehouse,
    val level: WarehouseLevel,
    val parent: WarehouseNode?,
    val children: MutableList<WarehouseNode> = mutableListOf()
)
package org.example.domain.algorithm.search

import org.example.domain.model.Warehouse

interface WarehouseGraph {

    fun getNeighbors(warehouse: Warehouse): List<Warehouse>
    fun getDistance(currentWarehouse: Warehouse, neighborWarehouse: Warehouse): Double?
}
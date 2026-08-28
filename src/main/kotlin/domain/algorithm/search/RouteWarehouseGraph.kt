package org.example.domain.algorithm.search

import org.example.domain.model.Route
import org.example.domain.model.Warehouse

class RouteWarehouseGraph(
    private val routes: List<Route>
) : WarehouseGraph {


    override fun getNeighbors(
        warehouse: Warehouse
    ): List<Warehouse> {
        return routes
            .filter {
                it.originWarehouse == warehouse
            }
            .map {
                it.destinationWarehouse
            }
    }

    override fun getDistance(
        currentWarehouse: Warehouse,
        neighborWarehouse: Warehouse
    ): Double? {

        return routes
            .firstOrNull {
                it.originWarehouse == currentWarehouse &&
                        it.destinationWarehouse == neighborWarehouse
            }
            ?.distanceKm
    }
}

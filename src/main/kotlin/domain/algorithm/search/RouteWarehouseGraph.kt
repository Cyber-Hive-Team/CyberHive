package org.example.domain.algorithm.search

import org.example.domain.model.Route
import org.example.domain.model.Warehouse

class RouteWarehouseGraph(
    private val routes: List<Route>
) : WarehouseGraph {

    override fun getNeighbors(
        warehouse: Warehouse
    ): List<Warehouse> {

        return routes.mapNotNull { route ->
            when {
                route.originWarehouse == warehouse ->
                    route.destinationWarehouse

                route.destinationWarehouse == warehouse ->
                    route.originWarehouse

                else -> null
            }
        }
    }
}
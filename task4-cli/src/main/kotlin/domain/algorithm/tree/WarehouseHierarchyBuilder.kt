package org.example.domain.algorithm.tree

import org.example.domain.model.RegionalZone
import org.example.domain.model.Route
import org.example.domain.model.Warehouse
import org.example.domain.model.WarehouseLevel
import org.example.domain.model.WarehouseNode

class WarehouseHierarchyBuilder(
    private val warehouses: List<Warehouse>,
    private val routes: List<Route>
) {
    fun build(): WarehouseNode? {
        val globalWarehouse = findGlobalWarehouse() ?: return null
        val globalNode = WarehouseNode(
            warehouse = globalWarehouse,
            level = WarehouseLevel.GLOBAL,
            parent = null
        )
        val warehousesByRegion = warehouses
            .filter { warehouse ->
                warehouse != globalWarehouse
            }
            .groupBy { warehouse ->
                warehouse.regionalZone
            }
        warehousesByRegion.forEach { (_, warehousesInRegion) ->
            val regionalWarehouse = findRegionalWarehouse(warehousesInRegion) ?: return@forEach
            val regionalNode = WarehouseNode(
                warehouse = regionalWarehouse,
                level = WarehouseLevel.REGIONAL,
                parent = globalNode
            )
            globalNode.children.add(regionalNode)
            addLocalWarehouses(warehousesInRegion, regionalWarehouse, regionalNode)
        }
        return globalNode
    }

    private fun findGlobalWarehouse(): Warehouse? {

        val allRegions = RegionalZone.entries

        return warehouses
            .filter { warehouse ->

                val connectedRegions = routes
                    .filter { route ->
                        route.originWarehouse == warehouse
                    }
                    .map { route ->
                        route.destinationWarehouse.regionalZone
                    }
                    .toSet()

                connectedRegions.containsAll(allRegions)
            }
            .maxByOrNull { warehouse ->
                routes.count { route ->
                    route.originWarehouse == warehouse
                }
            }
    }

    private fun findRegionalWarehouse(
        warehousesInRegion: List<Warehouse>
    ): Warehouse? {

        return warehousesInRegion
            .maxByOrNull { warehouse ->

                routes.count { route ->
                    route.originWarehouse == warehouse &&
                            route.destinationWarehouse.regionalZone ==
                            warehouse.regionalZone
                }
            }
    }

    private fun addLocalWarehouses(
        warehousesInRegion: List<Warehouse>,
        regionalWarehouse: Warehouse,
        regionalNode: WarehouseNode
    ) {
        warehousesInRegion
            .filter { warehouse ->
                warehouse != regionalWarehouse
            }
            .forEach { localWarehouse ->
                val localNode = WarehouseNode(
                    warehouse = localWarehouse,
                    level = WarehouseLevel.LOCAL,
                    parent = regionalNode
                )

                regionalNode.children.add(localNode)
            }
    }
}

package org.example.domain.builder

import org.example.domain.model.Package
import org.example.domain.model.Route
import org.example.domain.model.Vehicle
import org.example.domain.model.Warehouse

class DomainGraphBuilder {

    fun buildConnectedDomainGraph(
        warehouses: List<Warehouse>,
        packages: List<Package>,
        vehicles: List<Vehicle>,
        routes: List<Route>
    ): BuildResult {
        linkRelationships(
            packages = packages,
            vehicles = vehicles,
            routes = routes
        )

        return BuildResult(
            success = warehouses,
            warnings = emptyList()
        )
    }

    private fun linkRelationships(
        packages: List<Package>,
        vehicles: List<Vehicle>,
        routes: List<Route>
    ) {
        packages
            .groupBy { it.originWarehouse }
            .forEach { (warehouse, items) ->
                warehouse.addPackages(items)
            }

        vehicles
            .groupBy { it.currentHub }
            .forEach { (warehouse, items) ->
                warehouse.addVehicles(items)
            }

        routes
            .groupBy { it.originWarehouse }
            .forEach { (warehouse, items) ->
                warehouse.addRoutes(items)
            }
    }
}
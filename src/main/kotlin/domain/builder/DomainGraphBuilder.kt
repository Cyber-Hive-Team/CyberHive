package org.example.domain.builder

import org.example.data.dataholder.PackageRaw
import org.example.data.dataholder.RouteRaw
import org.example.domain.model.Package
import org.example.domain.model.Route
import org.example.domain.model.Vehicle
import org.example.domain.model.Warehouse

class DomainGraphBuilder {

    fun buildConnectedDomainGraph(
        warehouses: List<Warehouse>,
        rawPackageList: List<PackageRaw>,
        vehicles: List<Vehicle>,
        rawRouteList: List<RouteRaw>
    ): BuildResult {
        val warehouseIndex = warehouses.associateBy { it.id }
        val warnings = mutableListOf<String>()

        val packages = buildPackages(
            rawPackageList, warehouseIndex, warnings
        )
        val routes = buildRoutes(
            rawRouteList, warehouseIndex, warnings
        )

        linkBidirectionalRelationships(
            packages = packages,
            vehicles = vehicles,
            routes = routes
        )

        return BuildResult(
            success = warehouses,
            warnings = warnings
        )
    }

    private fun buildPackages(
        rawPackages: List<PackageRaw>,
        warehouseIndex: Map<String, Warehouse>,
        warnings: MutableList<String>
    ): List<Package> =
        rawPackages.mapNotNull { raw ->
            val origin = warehouseIndex[normalizeId(raw.originHubId)]
            val destination = warehouseIndex[normalizeId(raw.destinationHubId)]

            if (origin == null || destination == null) {
                warnings.add("Missing hub for package '${raw.id}'")
                null
            } else {
                Package(
                    id = raw.id,
                    weight = raw.weight,
                    priority = raw.priority,
                    originWarehouse = origin,
                    destinationWarehouse = destination
                )
            }
        }

    private fun buildRoutes(
        rawRoutes: List<RouteRaw>,
        warehouseIndex: Map<String, Warehouse>,
        warnings: MutableList<String>
    ): List<Route> =
        rawRoutes.mapNotNull { raw ->
            val origin = warehouseIndex[normalizeId(raw.originHubId)]
            val destination = warehouseIndex[normalizeId(raw.destinationHubId)]

            if (origin == null || destination == null) {
                warnings.add("Missing hub for route '${raw.id}'")
                null
            } else {
                Route(
                    id = raw.id,
                    distanceKm = raw.distanceKm,
                    typicalDelayMin = raw.typicalDelayMin,
                    originWarehouse = origin,
                    destinationWarehouse = destination
                )
            }
        }

    private fun linkBidirectionalRelationships(
        packages: List<Package>,
        vehicles: List<Vehicle>,
        routes: List<Route>
    ) {
        packages.groupBy { it.originWarehouse }
            .forEach { (warehouse, items) -> warehouse.addPackages(items) }

        vehicles.groupBy { it.currentHub }
            .forEach { (warehouse, items) -> warehouse.addVehicles(items) }

        routes.groupBy { it.originWarehouse }
            .forEach { (warehouse, items) -> warehouse.addRoutes(items) }
    }

    private fun normalizeId(id: String) = id.trim().uppercase()
}
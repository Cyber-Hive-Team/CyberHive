package org.example.domain.builder

import org.example.data.dataholder.PackageRaw
import org.example.data.dataholder.RouteRaw
import org.example.data.dataholder.VehicleRaw
import org.example.data.dataholder.WareHouseRaw
import org.example.domain.model.Package
import org.example.domain.model.Route
import org.example.domain.model.Vehicle
import org.example.domain.model.Warehouse

class DomainGraphBuilder {

    fun buildConnectedDomainGraph(
        warehouses: List<Warehouse>, rawPackageList: List<PackageRaw>,
        rawVehicleList: List<VehicleRaw>, rawRouteList: List<RouteRaw>
    ): BuildResult {
        val warehouseIndex = warehouses.associateBy { warehouse ->
            warehouse.id
            }
        val warningMessages = mutableListOf<String>()
        val packageEntities = buildPackages(
            rawPackages = rawPackageList,
            warehouseIndex = warehouseIndex, warningMessages = warningMessages
        )
        val vehicleEntities = buildVehicles(
            rawVehicles = rawVehicleList,
            warehouseIndex = warehouseIndex, warningMessages = warningMessages
        )
        val routeEntities = buildRoutes(
            rawRoutes = rawRouteList,
            warehouseIndex = warehouseIndex, warningMessages = warningMessages
        )
        linkBidirectionalRelationships(
            packages = packageEntities,
            vehicles = vehicleEntities,
            routes = routeEntities
        )
        return BuildResult(
            success = warehouses,
            warnings = warningMessages
        )
    }
    private fun buildPackages(
        rawPackages: List<PackageRaw>,
        warehouseIndex: Map<String, Warehouse>,
        warningMessages: MutableList<String>
    ): List<Package> {
        val packages = mutableListOf<Package>()

        for (rawPackage in rawPackages) {
            val builtPackage = buildSinglePackage(
                rawPackage = rawPackage,
                warehouseIndex = warehouseIndex,
                warningMessages = warningMessages
            )

            if (builtPackage != null) {
                packages.add(builtPackage)
            }
        }

        return packages
    }

    private fun buildSinglePackage(
        rawPackage: PackageRaw,
        warehouseIndex: Map<String, Warehouse>,
        warningMessages: MutableList<String>
    ): Package? {
        val originWarehouse =
            warehouseIndex[normalizeId(rawPackage.originHubId)]

        if (originWarehouse == null) {
            warningMessages.add(
                "Missing origin hub '${rawPackage.originHubId}' " +
                        "for package '${rawPackage.id}'"
            )
            return null
        }

        val destinationWarehouse =
            warehouseIndex[normalizeId(rawPackage.destinationHubId)]

        if (destinationWarehouse == null) {
            warningMessages.add(
                "Missing destination hub '${rawPackage.destinationHubId}' " +
                        "for package '${rawPackage.id}'"
            )
            return null
        }

        return Package(
            id = rawPackage.id,
            weight = rawPackage.weight,
            priority = rawPackage.priority,
            originWarehouse = originWarehouse,
            destinationWarehouse = destinationWarehouse
        )
    }

    private fun buildVehicles(
        rawVehicles: List<VehicleRaw>,
        warehouseIndex: Map<String, Warehouse>,
        warningMessages: MutableList<String>
    ): List<Vehicle> {
        val vehicles = mutableListOf<Vehicle>()

        for (rawVehicle in rawVehicles) {
            val builtVehicle = buildSingleVehicle(
                rawVehicle = rawVehicle,
                warehouseIndex = warehouseIndex,
                warningMessages = warningMessages
            )

            if (builtVehicle != null) {
                vehicles.add(builtVehicle)
            }
        }

        return vehicles
    }

    private fun buildSingleVehicle(
        rawVehicle: VehicleRaw,
        warehouseIndex: Map<String, Warehouse>,
        warningMessages: MutableList<String>
    ): Vehicle? {
        val currentWarehouse =
            warehouseIndex[normalizeId(rawVehicle.currentHubId)]

        if (currentWarehouse == null) {
            warningMessages.add(
                "Missing hub '${rawVehicle.currentHubId}' " +
                        "for vehicle '${rawVehicle.id}'"
            )
            return null
        }

        return Vehicle(
            id = rawVehicle.id,
            maxCapacityKg = rawVehicle.maxCapacityKg,
            costPerKm = rawVehicle.costPerKm,
            currentHub = currentWarehouse
        )
    }

    private fun buildRoutes(
        rawRoutes: List<RouteRaw>,
        warehouseIndex: Map<String, Warehouse>,
        warningMessages: MutableList<String>
    ): List<Route> {
        val routes = mutableListOf<Route>()

        for (rawRoute in rawRoutes) {
            val builtRoute = buildSingleRoute(
                rawRoute = rawRoute,
                warehouseIndex = warehouseIndex,
                warningMessages = warningMessages
            )

            if (builtRoute != null) {
                routes.add(builtRoute)
            }
        }

        return routes
    }

    private fun buildSingleRoute(
        rawRoute: RouteRaw,
        warehouseIndex: Map<String, Warehouse>,
        warningMessages: MutableList<String>
    ): Route? {
        val originWarehouse =
            warehouseIndex[normalizeId(rawRoute.originHubId)]

        if (originWarehouse == null) {
            warningMessages.add(
                "Missing origin hub '${rawRoute.originHubId}' " +
                        "for route '${rawRoute.id}'"
            )
            return null
        }

        val destinationWarehouse =
            warehouseIndex[normalizeId(rawRoute.destinationHubId)]

        if (destinationWarehouse == null) {
            warningMessages.add(
                "Missing destination hub '${rawRoute.destinationHubId}' " +
                        "for route '${rawRoute.id}'"
            )
            return null
        }

        return Route(
            id = rawRoute.id,
            distanceKm = rawRoute.distanceKm,
            typicalDelayMin = rawRoute.typicalDelayMin,
            originWarehouse = originWarehouse,
            destinationWarehouse = destinationWarehouse
        )
    }

    private fun linkBidirectionalRelationships(
        packages: List<Package>,
        vehicles: List<Vehicle>,
        routes: List<Route>
    ) {
        packages
            .groupBy { it.originWarehouse }
            .forEach { (warehouse, groupedPackages) ->
                warehouse.addPackages(groupedPackages)
            }

        vehicles
            .groupBy { it.currentHub }
            .forEach { (warehouse, groupedVehicles) ->
                warehouse.addVehicles(groupedVehicles)
            }

        routes
            .groupBy { it.originWarehouse }
            .forEach { (warehouse, groupedRoutes) ->
                warehouse.addRoutes(groupedRoutes)
            }
    }

    private fun normalizeId(id: String): String {
        return id.trim().uppercase()
    }
}
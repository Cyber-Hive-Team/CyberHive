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
        rawWarehouseList: List<WareHouseRaw>,
        rawPackageList: List<PackageRaw>,
        rawVehicleList: List<VehicleRaw>,
        rawRouteList: List<RouteRaw>
    ): BuildResult {
        val warehouses = rawWarehouseList.map {
            Warehouse(
                id = it.id.trim().uppercase(),
                name = it.name,
                regionalZone = it.regionalZone,
                latitude = it.latitude,
                longitude = it.longitude
            )
        }
        val warehouseIndex = warehouses.associateBy { it.id }
        val errorMessages = mutableListOf<String>()

        val packageEntities = buildPackages(rawPackageList, warehouseIndex, errorMessages)
        val vehicleEntities = buildVehicles(rawVehicleList, warehouseIndex, errorMessages)
        val routeEntities = buildRoutes(rawRouteList, warehouseIndex, errorMessages)

        if (errorMessages.isNotEmpty()) {
            return BuildResult(emptyList(), errorMessages)
        }

        linkBidirectionalRelationships(packageEntities, vehicleEntities, routeEntities)
        return BuildResult(warehouses, emptyList())
    }

    private fun buildPackages(
        rawPackages: List<PackageRaw>,
        warehouseIndex: Map<String, Warehouse>,
        errorMessages: MutableList<String>
    ): List<Package> {
        val packages = mutableListOf<Package>()
        for (rawPackage in rawPackages) {
            var hasError = false

            val originWarehouse = warehouseIndex[rawPackage.originHubId]
            if (originWarehouse == null) {
                errorMessages.add(
                    "Missing origin hub '${rawPackage.originHubId}' " +
                            "for package '${rawPackage.id}'"
                )
                hasError = true
            }

            val destinationWarehouse = warehouseIndex[rawPackage.destinationHubId]
            if (destinationWarehouse == null) {
                errorMessages.add(
                    "Missing destination hub '${rawPackage.destinationHubId}' " +
                            "for package '${rawPackage.id}'"
                )
                hasError = true
            }

            if (!hasError) {
                packages.add(
                    Package(
                        id = rawPackage.id,
                        weight = rawPackage.weight,
                        priority = rawPackage.priority,
                        originWarehouse = originWarehouse!!,
                        destinationWarehouse = destinationWarehouse!!
                    )
                )
            }
        }
        return packages
    }

    private fun buildVehicles(
        rawVehicles: List<VehicleRaw>,
        warehouseIndex: Map<String, Warehouse>,
        errorMessages: MutableList<String>
    ): List<Vehicle> {
        val vehicles = mutableListOf<Vehicle>()
        for (rawVehicle in rawVehicles) {
            val hubWarehouse = warehouseIndex[rawVehicle.currentHubId]
            if (hubWarehouse != null) {
                vehicles.add(
                    Vehicle(
                        id = rawVehicle.id,
                        maxCapacityKg = rawVehicle.maxCapacityKg,
                        costPerKm = rawVehicle.costPerKm,
                        currentHub = hubWarehouse
                    )
                )
            } else {
                errorMessages.add(
                    "Missing hub '${rawVehicle.currentHubId}' " +
                            "for vehicle '${rawVehicle.id}'"
                )
            }
        }
        return vehicles
    }

    private fun buildRoutes(
        rawRoutes: List<RouteRaw>,
        warehouseIndex: Map<String, Warehouse>,
        errorMessages: MutableList<String>
    ): List<Route> {
        val routes = mutableListOf<Route>()
        for (rawRoute in rawRoutes) {
            var hasError = false

            val originWarehouse = warehouseIndex[rawRoute.originHubId]
            if (originWarehouse == null) {
                errorMessages.add(
                    "Missing origin hub '${rawRoute.originHubId}' " +
                            "for route '${rawRoute.id}'"
                )
                hasError = true
            }

            val destinationWarehouse = warehouseIndex[rawRoute.destinationHubId]
            if (destinationWarehouse == null) {
                errorMessages.add(
                    "Missing destination hub '${rawRoute.destinationHubId}' " +
                            "for route '${rawRoute.id}'"
                )
                hasError = true
            }

            if (!hasError) {
                routes.add(
                    Route(
                        id = rawRoute.id,
                        distanceKm = rawRoute.distanceKm,
                        typicalDelayMin = rawRoute.typicalDelayMin,
                        originWarehouse = originWarehouse!!,
                        destinationWarehouse = destinationWarehouse!!
                    )
                )
            }
        }
        return routes
    }

    private fun linkBidirectionalRelationships(
        packages: List<Package>,
        vehicles: List<Vehicle>,
        routes: List<Route>
    ) {
        packages.forEach { packageEntity ->
            packageEntity.originWarehouse.addPackage(packageEntity)
        }
        vehicles.forEach { vehicleEntity ->
            vehicleEntity.currentHub.addVehicle(vehicleEntity)
        }
        routes.forEach { routeEntity ->
            routeEntity.originWarehouse.addRoute(routeEntity)
        }
    }
}
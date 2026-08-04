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

        // بدل ما نوقف، نطبع تحذير ونكمل
        if (errorMessages.isNotEmpty()) {
            println("WARNING: Some items were skipped because of missing hubs:")
            errorMessages.forEach { println("  - $it") }
        }

        linkBidirectionalRelationships(packageEntities, vehicleEntities, routeEntities)
        return BuildResult(warehouses, errorMessages)
    }

    private fun findWarehouse(
        warehouseId: String,
        warehouseIndex: Map<String, Warehouse>
    ): Warehouse? = warehouseIndex[warehouseId.trim().uppercase()]


    private fun buildPackages(
        rawPackages: List<PackageRaw>,
        warehouseIndex: Map<String, Warehouse>,
        errorMessages: MutableList<String>
    ): List<Package> {
        val packages = mutableListOf<Package>()
        for (rawPackage in rawPackages) {
            val built = buildSinglePackage(rawPackage, warehouseIndex, errorMessages)
            if (built != null) {
                packages.add(built)
            }
        }
        return packages
    }

    private fun buildSinglePackage(
        rawPackage: PackageRaw,
        warehouseIndex: Map<String, Warehouse>,
        errorMessages: MutableList<String>
    ): Package? {
        val originWarehouse = findWarehouse(rawPackage.originHubId, warehouseIndex)
        if (originWarehouse == null) {
            errorMessages.add("Missing origin hub '${rawPackage.originHubId}' for package '${rawPackage.id}'")
            return null
        }

        val destinationWarehouse = findWarehouse(rawPackage.destinationHubId, warehouseIndex)
        if (destinationWarehouse == null) {
            errorMessages.add("Missing destination hub '${rawPackage.destinationHubId}' for package '${rawPackage.id}'")
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
        errorMessages: MutableList<String>
    ): List<Vehicle> {
        val vehicles = mutableListOf<Vehicle>()
        for (rawVehicle in rawVehicles) {
            val built = buildSingleVehicle(rawVehicle, warehouseIndex, errorMessages)
            if (built != null) {
                vehicles.add(built)
            }
        }
        return vehicles
    }

    private fun buildSingleVehicle(
        rawVehicle: VehicleRaw,
        warehouseIndex: Map<String, Warehouse>,
        errorMessages: MutableList<String>
    ): Vehicle? {
        val hubWarehouse = findWarehouse(rawVehicle.currentHubId, warehouseIndex)
        if (hubWarehouse == null) {
            errorMessages.add("Missing hub '${rawVehicle.currentHubId}' for vehicle '${rawVehicle.id}'")
            return null
        }

        return Vehicle(
            id = rawVehicle.id,
            maxCapacityKg = rawVehicle.maxCapacityKg,
            costPerKm = rawVehicle.costPerKm,
            currentHub = hubWarehouse
        )
    }

    private fun buildRoutes(
        rawRoutes: List<RouteRaw>,
        warehouseIndex: Map<String, Warehouse>,
        errorMessages: MutableList<String>
    ): List<Route> {
        val routes = mutableListOf<Route>()
        for (rawRoute in rawRoutes) {
            val built = buildSingleRoute(rawRoute, warehouseIndex, errorMessages)
            if (built != null) {
                routes.add(built)
            }
        }
        return routes
    }

    private fun buildSingleRoute(
        rawRoute: RouteRaw,
        warehouseIndex: Map<String, Warehouse>,
        errorMessages: MutableList<String>
    ): Route? {
        val originWarehouse = findWarehouse(rawRoute.originHubId, warehouseIndex)
        if (originWarehouse == null) {
            errorMessages.add("Missing origin hub '${rawRoute.originHubId}' for route '${rawRoute.id}'")
            return null
        }

        val destinationWarehouse = findWarehouse(rawRoute.destinationHubId, warehouseIndex)
        if (destinationWarehouse == null) {
            errorMessages.add("Missing destination hub '${rawRoute.destinationHubId}' for route '${rawRoute.id}'")
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
        packages.forEach { pkg -> pkg.originWarehouse.addPackage(pkg) }
        vehicles.forEach { vehicle -> vehicle.currentHub.addVehicle(vehicle) }
        routes.forEach { route -> route.originWarehouse.addRoute(route) }
    }
}
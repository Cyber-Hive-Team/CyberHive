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
        rawWarehouse: List<WareHouseRaw>,
        rawPackage: List<PackageRaw>,
        rawVehicle: List<VehicleRaw>,
        rawRoute: List<RouteRaw>
    ): BuildResult {
        val warehouses = rawWarehouse.map {
            Warehouse(it.id.trim().uppercase(), it.name, it.regionalZone, it.latitude, it.longitude)
        }
        val warehouseByIdLookup = warehouses.associateBy { it.id }
        val errors = mutableListOf<String>()

        val packageEntities = constructPackagesFromRaw(rawPackage, warehouseByIdLookup, errors)
        val vehicleEntities = constructVehiclesFromRaw(rawVehicle, warehouseByIdLookup, errors)
        val routeEntities = constructRoutesFromRaw(rawRoute, warehouseByIdLookup, errors)

        if (errors.isNotEmpty()) {
            return BuildResult(emptyList(), errors)
        }

        synchronizeBidirectionalLinks(packageEntities, vehicleEntities, routeEntities)
        return BuildResult(warehouses, emptyList())
    }

    private fun constructPackagesFromRaw(
        rawPackages: List<PackageRaw>,
        warehouseLookup: Map<String, Warehouse>,
        errors: MutableList<String>
    ): List<Package> {
        val packages = mutableListOf<Package>()
        for (raw in rawPackages) {
            val origin = warehouseLookup[raw.originHubId]
            if (origin == null) {
                errors.add("Missing origin hub '${raw.originHubId}' for package '${raw.id}'")
                continue
            }
            val destination = warehouseLookup[raw.destinationHubId]
            if (destination == null) {
                errors.add("Missing destination hub '${raw.destinationHubId}' for package '${raw.id}'")
                continue
            }
            packages.add(
                Package(raw.id, raw.weight, raw.priority, origin, destination)
            )
        }
        return packages
    }

    private fun constructVehiclesFromRaw(
        rawVehicles: List<VehicleRaw>,
        warehouseLookup: Map<String, Warehouse>,
        errors: MutableList<String>
    ): List<Vehicle> {
        val vehicles = mutableListOf<Vehicle>()
        for (raw in rawVehicles) {
            val hub = warehouseLookup[raw.currentHubId]
            if (hub == null) {
                errors.add("Missing hub '${raw.currentHubId}' for vehicle '${raw.id}'")
                continue
            }
            vehicles.add(
                Vehicle(raw.id, raw.maxCapacityKg, raw.costPerKm, hub)
            )
        }
        return vehicles
    }

    private fun constructRoutesFromRaw(
        rawRoutes: List<RouteRaw>,
        warehouseLookup: Map<String, Warehouse>,
        errors: MutableList<String>
    ): List<Route> {
        val routes = mutableListOf<Route>()
        for (raw in rawRoutes) {
            val origin = warehouseLookup[raw.originHubId]
            if (origin == null) {
                errors.add("Missing origin hub '${raw.originHubId}' for route '${raw.id}'")
                continue
            }
            val destination = warehouseLookup[raw.destinationHubId]
            if (destination == null) {
                errors.add("Missing destination hub '${raw.destinationHubId}' for route '${raw.id}'")
                continue
            }
            routes.add(
                Route(raw.id, raw.distanceKm, raw.typicalDelayMin, origin, destination)
            )
        }
        return routes
    }

    private fun synchronizeBidirectionalLinks(
        packages: List<Package>,
        vehicles: List<Vehicle>,
        routes: List<Route>
    ) {
        packages.forEach { it.originWarehouse.addPackage(it) }
        vehicles.forEach { it.currentHub.addVehicle(it) }
        routes.forEach { it.originWarehouse.addRoute(it) }
    }
}
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
    ): List<Warehouse> {
        val warehouses = rawWarehouse.map {
            Warehouse(it.id.trim().uppercase(), it.name, it.regionalZone, it.latitude, it.longitude)
        }
        val warehouseByIdLookup = warehouses.associateBy { it.id }

        val packageEntities = constructPackagesFromRaw(
            rawPackage,
            warehouseByIdLookup
        )
        val vehicleEntities = constructVehiclesFromRaw(
            rawVehicle,
            warehouseByIdLookup
        )
        val routeEntities = constructRoutesFromRaw(
            rawRoute,
            warehouseByIdLookup
        )

        synchronizeBidirectionalLinks(
            packageEntities,
            vehicleEntities,
            routeEntities
        )

        return warehouses
    }

    private fun constructPackagesFromRaw(
        rawPackages: List<PackageRaw>,
        warehouseLookup: Map<String, Warehouse>
    ): List<Package> {
        return rawPackages.mapNotNull { rawPackageDto ->
            val originWarehouse = warehouseLookup[rawPackageDto.originHubId]
            if (originWarehouse == null) {
                println(
                    "Warning: Skipping package ${rawPackageDto.id} - " +
                            "Origin hub '${rawPackageDto.originHubId}' not found"
                )
                return@mapNotNull null
            }

            val destinationWarehouse =
                warehouseLookup[rawPackageDto.destinationHubId]
            if (destinationWarehouse == null) {
                println(
                    "Warning: Skipping package ${rawPackageDto.id} - " +
                            "Destination hub '${rawPackageDto.destinationHubId}' not found"
                )
                return@mapNotNull null
            }

            Package(
                id = rawPackageDto.id,
                weight = rawPackageDto.weight,
                priority = rawPackageDto.priority,
                originWarehouse = originWarehouse,
                destinationWarehouse = destinationWarehouse
            )
        }
    }

    private fun constructVehiclesFromRaw(
        rawVehicles: List<VehicleRaw>,
        warehouseLookup: Map<String, Warehouse>
    ): List<Vehicle> {
        return rawVehicles.mapNotNull { rawVehicleDto ->
            val currentHubWarehouse =
                warehouseLookup[rawVehicleDto.currentHubId]
            if (currentHubWarehouse == null) {
                println(
                    "Warning: Skipping vehicle ${rawVehicleDto.id} - " +
                            "Hub '${rawVehicleDto.currentHubId}' not found"
                )
                return@mapNotNull null
            }

            Vehicle(
                id = rawVehicleDto.id,
                maxCapacityKg = rawVehicleDto.maxCapacityKg,
                costPerKm = rawVehicleDto.costPerKm,
                currentHub = currentHubWarehouse
            )
        }
    }

    private fun constructRoutesFromRaw(
        rawRoutes: List<RouteRaw>,
        warehouseLookup: Map<String, Warehouse>
    ): List<Route> {
        return rawRoutes.mapNotNull { rawRouteDto ->
            val originWarehouse = warehouseLookup[rawRouteDto.originHubId]
            if (originWarehouse == null) {
                println(
                    "Warning: Skipping route ${rawRouteDto.id} - " +
                            "Origin hub '${rawRouteDto.originHubId}' not found"
                )
                return@mapNotNull null
            }

            val destinationWarehouse =
                warehouseLookup[rawRouteDto.destinationHubId]
            if (destinationWarehouse == null) {
                println(
                    "Warning: Skipping route ${rawRouteDto.id} - " +
                            "Destination hub '${rawRouteDto.destinationHubId}' not found"
                )
                return@mapNotNull null
            }

            Route(
                id = rawRouteDto.id,
                distanceKm = rawRouteDto.distanceKm,
                typicalDelayMin = rawRouteDto.typicalDelayMin,
                originWarehouse = originWarehouse,
                destinationWarehouse = destinationWarehouse
            )
        }
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
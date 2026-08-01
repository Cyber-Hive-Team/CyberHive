package org.example.domain.builder

import org.example.data.dataholder.FleetRaw
import org.example.data.dataholder.PackageRaw
import org.example.data.dataholder.RouteRaw
import org.example.data.dataholder.WareHouseRaw
import org.example.domain.model.Package
import org.example.domain.model.Route
import org.example.domain.model.Vehicle
import org.example.domain.model.Warehouse

class DomainGraphBuilder {

    fun buildConnectedDomainGraph(
        rawWarehouseDtos: List<WareHouseRaw>,
        rawPackageDtos: List<PackageRaw>,
        rawFleetDtos: List<FleetRaw>,
        rawRouteDtos: List<RouteRaw>
    ): List<Warehouse> {
        val warehouses = rawWarehouseDtos.map {
            Warehouse(it.id.trim().uppercase(), it.name, it.regionalZone)
        }
        val warehouseByIdLookup = warehouses.associateBy { it.id }

        val packageEntities = constructPackagesFromRaw(
            rawPackageDtos,
            warehouseByIdLookup
        )
        val vehicleEntities = constructVehiclesFromRaw(
            rawFleetDtos,
            warehouseByIdLookup
        )
        val routeEntities = constructRoutesFromRaw(
            rawRouteDtos,
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
                origin = originWarehouse,
                destination = destinationWarehouse
            )
        }
    }

    private fun constructVehiclesFromRaw(
        rawVehicles: List<FleetRaw>,
        warehouseLookup: Map<String, Warehouse>
    ): List<Vehicle> {
        return rawVehicles.mapNotNull { rawVehicleDto ->
            val currentHubWarehouse =
                warehouseLookup[rawVehicleDto.currentHubId]
            if (currentHubWarehouse == null) {
                println(
                    "Warning: Skipping vehicle ${rawVehicleDto.vehicleId} - " +
                            "Hub '${rawVehicleDto.currentHubId}' not found"
                )
                return@mapNotNull null
            }

            Vehicle(
                vehicleId = rawVehicleDto.vehicleId,
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
                routeId = rawRouteDto.id,
                distanceKm = rawRouteDto.distanceKm,
                typicalDelayMin = rawRouteDto.typicalDelayMin,
                origin = originWarehouse,
                destination = destinationWarehouse
            )
        }
    }

    private fun synchronizeBidirectionalLinks(
        packages: List<Package>,
        vehicles: List<Vehicle>,
        routes: List<Route>
    ) {
        packages.forEach { it.origin.addPackage(it) }
        vehicles.forEach { it.currentHub.addVehicle(it) }
        routes.forEach { it.origin.addRoute(it) }
    }
}
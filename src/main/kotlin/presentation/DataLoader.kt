package org.example.presentation

import org.example.domain.model.Package
import org.example.domain.model.Route
import org.example.domain.model.Vehicle
import org.example.domain.model.Warehouse
import org.example.domain.repository.PackageRepository
import org.example.domain.repository.RouteRepository
import org.example.domain.repository.VehicleRepository
import org.example.domain.repository.WarehouseRepository
import kotlinx.coroutines.async
import kotlinx.coroutines.coroutineScope

data class LoadedData(
    val warehouses: List<Warehouse>,
    val packages: List<Package>,
    val vehicles: List<Vehicle>,
    val routes: List<Route>
)

class DataLoader(
    private val warehouseRepository: WarehouseRepository,
    private val packageRepository: PackageRepository,
    private val vehicleRepository: VehicleRepository,
    private val routeRepository: RouteRepository,
) {

    suspend fun load(): LoadedData = coroutineScope {

        val warehousesDeferred = async { warehouseRepository.getAllWarehouses().getOrThrow() }
        val packagesDeferred = async { packageRepository.getAllPackages().getOrThrow() }
        val vehiclesDeferred = async { vehicleRepository.getVehicles().getOrThrow() }
        val routesDeferred = async { routeRepository.getAllRoutes().getOrThrow() }

        val warehouses = warehousesDeferred.await()
        val packages = packagesDeferred.await()
        val vehicles = vehiclesDeferred.await()
        val routes = routesDeferred.await()

        printLoadingResult(
            warehouses = warehouses,
            packages = packages,
            vehicles = vehicles,
            routes = routes
        )
        LoadedData(
            warehouses = warehouses,
            packages = packages,
            vehicles = vehicles,
            routes = routes
        )
    }

    private fun printLoadingResult(
        warehouses: List<Warehouse>,
        packages: List<Package>,
        vehicles: List<Vehicle>,
        routes: List<Route>
    ) {
        println("Warehouses loaded: ${warehouses.size}")
        println("Packages loaded: ${packages.size}")
        println("Vehicles loaded: ${vehicles.size}")
        println("Routes loaded: ${routes.size}")
    }
}

package org.example.presentation

import org.example.domain.model.Package
import org.example.domain.model.Route
import org.example.domain.model.Vehicle
import org.example.domain.model.Warehouse
import org.example.domain.repository.PackageRepository
import org.example.domain.repository.RouteRepository
import org.example.domain.repository.VehicleRepository
import org.example.domain.repository.WarehouseRepository

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

    suspend fun load(): LoadedData {

        val warehouses = warehouseRepository.getAllWarehouses().getOrThrow()
        val packages = packageRepository.getAllPackages().getOrThrow()
        val vehicles = vehicleRepository.getVehicles().getOrThrow()
        val routes = routeRepository.getAllRoutes().getOrThrow()

        printLoadingResult(
            warehouses = warehouses,
            packages = packages,
            vehicles = vehicles,
            routes = routes
        )

        return LoadedData(
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

package org.example.presentation

import org.example.data.datasource.CsvPackageDataSource
import org.example.data.datasource.CsvRouteDataSource
import org.example.data.datasource.CsvVehicleDataSource
import org.example.data.datasource.CsvWarehouseDataSource
import org.example.data.mapper.PackageMapper
import org.example.data.mapper.RouteMapper
import org.example.data.mapper.VehicleMapper
import org.example.data.mapper.WarehouseMapper
import org.example.data.repository.CsvPackageRepository
import org.example.data.repository.CsvRouteRepository
import org.example.data.repository.CsvVehicleRepository
import org.example.data.repository.CsvWarehouseRepository
import org.example.domain.model.Package
import org.example.domain.model.Route
import org.example.domain.model.Vehicle
import org.example.domain.model.Warehouse

private const val WAREHOUSE_FILE = "src/main/resources/warehouses.csv"
private const val PACKAGE_FILE = "src/main/resources/packages.csv"
private const val VEHICLE_FILE = "src/main/resources/fleet.csv"
private const val ROUTE_FILE = "src/main/resources/routes.csv"

data class LoadedData(
    val warehouses: List<Warehouse>,
    val packages: List<Package>,
    val vehicles: List<Vehicle>,
    val routes: List<Route>
)

class DataLoader {

    fun load(): LoadedData {
        val warehouses = loadWarehouses()
        val map = warehouses.associateBy { it.id }

        val packages = loadPackages(map)
        val vehicles = loadVehicles(map)
        val routes = loadRoutes(map)

        println("\n=== Parsing Results ===")
        println("Warehouses: ${warehouses.size}")
        println("Packages: ${packages.size}")
        println("Vehicles: ${vehicles.size}")
        println("Routes: ${routes.size}")

        return LoadedData(warehouses, packages, vehicles, routes)
    }

    private fun loadWarehouses(): List<Warehouse> {
        val result = CsvWarehouseRepository(
            CsvWarehouseDataSource(WAREHOUSE_FILE),
            WarehouseMapper()
        ).getAllWarehouses()

        result.errorMessage?.let { println("WARNING: $it") }

        return result.data
    }

    private fun loadPackages(map: Map<String, Warehouse>): List<Package> {
        val result = CsvPackageRepository(
            CsvPackageDataSource(PACKAGE_FILE),
            PackageMapper(),
            map
        ).getAllPackages()

        result.errorMessage?.let { println("WARNING: $it") }

        return result.data
    }

    private fun loadVehicles(map: Map<String, Warehouse>): List<Vehicle> {
        val result = CsvVehicleRepository(
            CsvVehicleDataSource(VEHICLE_FILE),
            VehicleMapper(),
            map
        ).getVehicles()

        result.errorMessage?.let { println("WARNING: $it") }

        return result.data
    }

    private fun loadRoutes(map: Map<String, Warehouse>): List<Route> {
        val result = CsvRouteRepository(
            CsvRouteDataSource(ROUTE_FILE),
            RouteMapper(),
            map
        ).getAllRoutes()

        result.errorMessage?.let { println("WARNING: $it") }

        return result.data
    }
}

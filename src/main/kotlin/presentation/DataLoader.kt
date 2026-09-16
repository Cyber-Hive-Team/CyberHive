package org.example.presentation

import org.example.data.datasource.local.CsvPackageDataSource
import org.example.data.datasource.local.CsvRouteDataSource
import org.example.data.datasource.local.CsvVehicleDataSource
import org.example.data.datasource.local.CsvWarehouseDataSource
import org.example.data.datasource.remote.supabase.SupabaseWarehouseRemoteDatasource
import org.example.data.mapper.csv.PackageMapper
import org.example.data.mapper.csv.RouteMapper
import org.example.data.mapper.csv.VehicleMapper
import org.example.data.mapper.csv.WarehouseMapper
import org.example.data.mapper.remote.WarehouseRemoteMapper
import org.example.data.remote.client.SupabaseHttpClient
import org.example.data.remote.config.SupabaseConfig
import org.example.data.repository.CsvPackageRepository
import org.example.data.repository.CsvRouteRepository
import org.example.data.repository.CsvVehicleRepository
import org.example.data.repository.WarehouseRepositoryImpl
import org.example.data.validation.PackageValidator
import org.example.data.validation.RouteValidator
import org.example.data.validation.VehicleValidator
import org.example.data.validation.WarehouseValidator
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

class DataLoader(
    private val httpClient: SupabaseHttpClient,
    private val supabaseConfig: SupabaseConfig
) {

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
        val result = WarehouseRepositoryImpl(
            csvDataSource = CsvWarehouseDataSource(WAREHOUSE_FILE),
            csvMapper = WarehouseMapper(),
            validator = WarehouseValidator(),
            remoteDataSource = SupabaseWarehouseRemoteDatasource(
                client = httpClient.create(),
                baseUrl = "${supabaseConfig.url}/rest/v1"
            ),
            remoteMapper = WarehouseRemoteMapper()
        ).getAllWarehouses()
        result.errorMessage?.let { println("WARNING: $it") }

        return result.data
    }

    private fun loadPackages(map: Map<String, Warehouse>): List<Package> {
        val result = CsvPackageRepository(
            CsvPackageDataSource(PACKAGE_FILE),
            PackageMapper(),
            map,
            PackageValidator()
        ).getAllPackages()

        result.errorMessage?.let { println("WARNING: $it") }

        return result.data
    }

    private fun loadVehicles(map: Map<String, Warehouse>): List<Vehicle> {
        val result = CsvVehicleRepository(
            CsvVehicleDataSource(VEHICLE_FILE),
            VehicleMapper(),
            map,
            VehicleValidator()
        ).getVehicles()

        result.errorMessage?.let { println("WARNING: $it") }

        return result.data
    }

    private fun loadRoutes(map: Map<String, Warehouse>): List<Route> {
        val result = CsvRouteRepository(
            CsvRouteDataSource(ROUTE_FILE),
            RouteMapper(),
            map,
            RouteValidator()
        ).getAllRoutes()

        result.errorMessage?.let { println("WARNING: $it") }

        return result.data
    }
}

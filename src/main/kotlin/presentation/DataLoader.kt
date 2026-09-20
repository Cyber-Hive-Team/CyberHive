package org.example.presentation

import org.example.data.datasource.local.csv.CsvPackageLocalDataSource
import org.example.data.datasource.local.csv.CsvRouteLocalDataSource
import org.example.data.datasource.local.csv.CsvVehicleLocalDataSource
import org.example.data.datasource.local.csv.CsvWarehouseLocalDataSource
import org.example.data.datasource.remote.supabase.SupabasePackageRemoteDatasource
import org.example.data.datasource.remote.supabase.SupabaseRouteRemoteDatasource
import org.example.data.datasource.remote.supabase.SupabaseVehicleRemoteDatasource
import org.example.data.datasource.remote.supabase.SupabaseWarehouseRemoteDatasource
import org.example.data.mapper.csv.PackageMapper
import org.example.data.mapper.csv.RouteMapper
import org.example.data.mapper.csv.VehicleMapper
import org.example.data.mapper.csv.WarehouseMapper
import org.example.data.mapper.remote.PackageRemoteMapper
import org.example.data.mapper.remote.RouteDtoMapper
import org.example.data.mapper.remote.VehicleDtoMapper
import org.example.data.mapper.remote.WarehouseRemoteMapper
import org.example.data.remote.client.SupabaseHttpClient
import org.example.data.remote.config.SupabaseConfig
import org.example.data.repositoryImplementation.PackageRepositoryImpl
import org.example.data.repositoryImplementation.RouteRepositoryImpl
import org.example.data.repositoryImplementation.VehicleRepositoryImpl
import org.example.data.repositoryImplementation.WarehouseRepositoryImpl
import org.example.data.repositoryImplementation.dependencies.PackageRepositoryDependencies
import org.example.data.repositoryImplementation.dependencies.RouteRepositoryDependencies
import org.example.data.repositoryImplementation.dependencies.VehicleRepositoryDependencies
import org.example.data.repositoryImplementation.dependencies.WarehouseRepositoryDependencies
import org.example.data.validation.PackageRemoteValidator
import org.example.data.validation.PackageValidator
import org.example.data.validation.RouteRemoteValidator
import org.example.data.validation.RouteValidator
import org.example.data.validation.VehicleRemoteValidator
import org.example.data.validation.VehicleValidator
import org.example.data.validation.WarehouseRemoteValidator
import org.example.data.validation.WarehouseValidator
import org.example.domain.model.Package
import org.example.domain.model.Route
import org.example.domain.model.Vehicle
import org.example.domain.model.Warehouse
import org.example.domain.repository.VehicleRepository
import org.example.domain.repository.WarehouseRepository


private const val WAREHOUSE_FILE =
    "src/main/resources/warehouses.csv"

private const val PACKAGE_FILE =
    "src/main/resources/packages.csv"

private const val VEHICLE_FILE =
    "src/main/resources/fleet.csv"

private const val ROUTE_FILE =
    "src/main/resources/routes.csv"


data class LoadedData(
    val warehouses: List<Warehouse>,
    val packages: List<Package>,
    val vehicles: List<Vehicle>,
    val routes: List<Route>,
    val vehicleRepository: VehicleRepository
)


class DataLoader(
    private val httpClient: SupabaseHttpClient,
    private val supabaseConfig: SupabaseConfig
) {
    private val client by lazy {
        httpClient.create()
    }

    suspend fun load(): LoadedData {
        val warehouseRepository = createWarehouseRepository()
        val warehouses = loadWarehouses(warehouseRepository)
        val warehouseMap = warehouses.associateBy { it.id }
        val packages = loadPackages(warehouseMap, warehouseRepository)
        val vehicleRepository = createVehicleRepository(warehouseMap, warehouseRepository)
        val vehicles = vehicleRepository.getVehicles().getOrThrow()
        val routes = loadRoutes(warehouseMap, warehouseRepository)
        printLoadingResult(warehouses, packages, vehicles, routes)
        return LoadedData(
            warehouses = warehouses,
            packages = packages,
            vehicles = vehicles,
            routes = routes,
            vehicleRepository = vehicleRepository
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


    private fun createVehicleRepository(
        map: Map<String, Warehouse>,
        warehouseRepository: WarehouseRepository
    ): VehicleRepository {

        return VehicleRepositoryImpl(
            VehicleRepositoryDependencies(
                localDataSource = CsvVehicleLocalDataSource(VEHICLE_FILE),
                localMapper = VehicleMapper(),
                validator = VehicleValidator(),
                warehouseMap = map,
                remoteDataSource =
                    SupabaseVehicleRemoteDatasource(client, "${supabaseConfig.url}/rest/v1"),
                remoteMapper = VehicleDtoMapper(),
                remoteValidator = VehicleRemoteValidator(),
                warehouseRepository = warehouseRepository
            )
        )
    }


    private fun createWarehouseRepository(): WarehouseRepository {
        return WarehouseRepositoryImpl(
            WarehouseRepositoryDependencies(
                localDataSource = CsvWarehouseLocalDataSource(WAREHOUSE_FILE),
                localMapper = WarehouseMapper(),
                validator = WarehouseValidator(),
                remoteDataSource =
                    SupabaseWarehouseRemoteDatasource(client, "${supabaseConfig.url}/rest/v1"),
                remoteMapper = WarehouseRemoteMapper(),
                remoteValidator = WarehouseRemoteValidator()
            )
        )
    }


    private suspend fun loadWarehouses(
        warehouseRepository: WarehouseRepository
    ): List<Warehouse> {

        return warehouseRepository
            .getAllWarehouses()
            .getOrThrow()
    }


    private suspend fun loadPackages(
        map: Map<String, Warehouse>,
        warehouseRepository: WarehouseRepository
    ): List<Package> {
        return PackageRepositoryImpl(
            PackageRepositoryDependencies(
                localDataSource = CsvPackageLocalDataSource(PACKAGE_FILE),
                localMapper = PackageMapper(),
                validator = PackageValidator(),
                warehouseMap = map,
                remoteDataSource = SupabasePackageRemoteDatasource(client, "${supabaseConfig.url}/rest/v1"),
                remoteMapper = PackageRemoteMapper(),
                remoteValidator = PackageRemoteValidator(),
                warehouseRepository = warehouseRepository
            )
        )
            .getAllPackages()
            .getOrThrow()
    }


    private suspend fun loadRoutes(
        map: Map<String, Warehouse>,
        warehouseRepository: WarehouseRepository
    ): List<Route> {
        return RouteRepositoryImpl(
            RouteRepositoryDependencies(
                localDataSource = CsvRouteLocalDataSource(ROUTE_FILE),
                localMapper = RouteMapper(),
                validator = RouteValidator(),
                warehouseMap = map,
                remoteDataSource =
                    SupabaseRouteRemoteDatasource(client, "${supabaseConfig.url}/rest/v1"),
                remoteMapper = RouteDtoMapper(),
                remoteValidator = RouteRemoteValidator(),
                warehouseRepository = warehouseRepository
            )
        )
            .getAllRoutes()
            .getOrThrow()
    }
}

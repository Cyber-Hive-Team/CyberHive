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
        val packages = loadPackages(warehouseMap)
        val vehicleRepository = createVehicleRepository(warehouseMap)
        val vehicles = vehicleRepository.getVehicles().getOrThrow()
        val routes = loadRoutes(warehouseMap)
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
        map: Map<String, Warehouse>
    ): VehicleRepository {

        return VehicleRepositoryImpl(
            VehicleRepositoryDependencies(
                localDataSource = CsvVehicleLocalDataSource(VEHICLE_FILE),
                remoteDataSource = SupabaseVehicleRemoteDatasource(client, "${supabaseConfig.url}/rest/v1"),
                localMapper = VehicleMapper(),
                remoteMapper = VehicleDtoMapper(),
                warehouseMap = map
            )
        )
    }


    private fun createWarehouseRepository(): WarehouseRepository {
        return WarehouseRepositoryImpl(
            WarehouseRepositoryDependencies(
                localDataSource = CsvWarehouseLocalDataSource(WAREHOUSE_FILE),
                remoteDataSource = SupabaseWarehouseRemoteDatasource(client, "${supabaseConfig.url}/rest/v1"),
                localMapper = WarehouseMapper(),
                remoteMapper = WarehouseRemoteMapper()
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
        map: Map<String, Warehouse>
    ): List<Package> {
        return PackageRepositoryImpl(
            PackageRepositoryDependencies(
                localDataSource = CsvPackageLocalDataSource(PACKAGE_FILE),
                remoteDataSource = SupabasePackageRemoteDatasource(client, "${supabaseConfig.url}/rest/v1"),
                localMapper = PackageMapper(),
                remoteMapper = PackageRemoteMapper(),
                warehouseMap = map
            )
        ).getAllPackages().getOrThrow()
    }


    private suspend fun loadRoutes(
        map: Map<String, Warehouse>
    ): List<Route> {
        return RouteRepositoryImpl(
            RouteRepositoryDependencies(
                localDataSource = CsvRouteLocalDataSource(ROUTE_FILE),
                remoteDataSource = SupabaseRouteRemoteDatasource(client, "${supabaseConfig.url}/rest/v1"),
                localMapper = RouteMapper(),
                remoteMapper = RouteDtoMapper(),
                warehouseMap = map
            )
        ).getAllRoutes().getOrThrow()
    }
}

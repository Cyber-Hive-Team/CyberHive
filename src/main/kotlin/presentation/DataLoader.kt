package org.example.presentation

import org.example.data.datasource.local.csv.CsvWarehouseStatusDataSource
import org.example.data.datasource.remote.supabase.SupabasePackageRemoteDataSource
import org.example.data.datasource.remote.supabase.SupabaseRouteRemoteDataSource
import org.example.data.datasource.remote.supabase.SupabaseVehicleRemoteDataSource
import org.example.data.datasource.remote.supabase.SupabaseWarehouseRemoteDataSource
import org.example.data.remote.client.SupabaseHttpClient
import org.example.data.remote.config.SupabaseConfig
import org.example.data.repositoryImplementation.PackageRepositoryImpl
import org.example.data.repositoryImplementation.RouteRepositoryImpl
import org.example.data.repositoryImplementation.VehicleRepositoryImpl
import org.example.data.repositoryImplementation.WarehouseRepositoryImpl
import org.example.domain.model.Package
import org.example.domain.model.Route
import org.example.domain.model.Vehicle
import org.example.domain.model.Warehouse
import org.example.domain.repository.VehicleRepository
import org.example.domain.repository.WarehouseRepository



private const val WAREHOUSE_STATUS_FILE =
    "src/main/resources/warehouse-status.csv"

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

            remoteDataSource = SupabaseVehicleRemoteDataSource(
                client,
                "${supabaseConfig.url}/rest/v1"
            ),
            warehouseMap = map
        )
    }


    private fun createWarehouseRepository(): WarehouseRepository {

        return WarehouseRepositoryImpl(
            remoteDataSource = SupabaseWarehouseRemoteDataSource(
                client,
                "${supabaseConfig.url}/rest/v1"
            ),
            statusDataSource = CsvWarehouseStatusDataSource(
                WAREHOUSE_STATUS_FILE
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
            remoteDataSource = SupabasePackageRemoteDataSource(client, "${supabaseConfig.url}/rest/v1"),
            warehouseMap = map
        ).getAllPackages().getOrThrow()
    }


    private suspend fun loadRoutes(
        map: Map<String, Warehouse>
    ): List<Route> {
        return RouteRepositoryImpl(
            remoteDataSource = SupabaseRouteRemoteDataSource(
                client,
                "${supabaseConfig.url}/rest/v1"
            ),
            warehouseMap = map
        ).getAllRoutes().getOrThrow()
    }
}

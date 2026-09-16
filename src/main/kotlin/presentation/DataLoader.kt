package org.example.presentation

import org.example.data.datasource.local.CsvPackageDataSource
import org.example.data.datasource.local.CsvRouteDataSource
import org.example.data.datasource.local.CsvVehicleDataSource
import org.example.data.datasource.local.CsvWarehouseDataSource
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
import org.example.data.validation.PackageValidator
import org.example.data.validation.RouteValidator
import org.example.data.validation.VehicleValidator
import org.example.data.validation.WarehouseValidator
import org.example.domain.model.Package
import org.example.domain.model.Route
import org.example.domain.model.Vehicle
import org.example.domain.model.Warehouse
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
    val routes: List<Route>
)


class DataLoader(
    private val httpClient: SupabaseHttpClient,
    private val supabaseConfig: SupabaseConfig
) {


    private val client by lazy {
        httpClient.create()
    }


    fun load(): LoadedData {

        val warehouses =
            loadWarehouses()

        val warehouseMap =
            warehouses.associateBy { it.id }


        val warehouseRepository =
            createWarehouseRepository()


        val packages =
            loadPackages(
                warehouseMap,
                warehouseRepository
            )


        val vehicles =
            loadVehicles(
                warehouseMap,
                warehouseRepository
            )


        val routes =
            loadRoutes(
                warehouseMap,
                warehouseRepository
            )


        return LoadedData(
            warehouses = warehouses,
            packages = packages,
            vehicles = vehicles,
            routes = routes
        )
    }


    private fun createWarehouseRepository():
            WarehouseRepository {

        return WarehouseRepositoryImpl(

            WarehouseRepositoryDependencies(

                localDataSource =
                    CsvWarehouseDataSource(
                        WAREHOUSE_FILE
                    ),

                localMapper =
                    WarehouseMapper(),

                validator =
                    WarehouseValidator(),

                remoteDataSource =
                    SupabaseWarehouseRemoteDatasource(
                        client,
                        "${supabaseConfig.url}/rest/v1"
                    ),

                remoteMapper =
                    WarehouseRemoteMapper()
            )
        )
    }




    private fun loadWarehouses(): List<Warehouse> {

        return createWarehouseRepository()
            .getAllWarehouses()
            .data
    }


    private fun loadPackages(
        map: Map<String, Warehouse>,
        warehouseRepository: WarehouseRepository
    ): List<Package> {


        return PackageRepositoryImpl(

            PackageRepositoryDependencies(

                localDataSource =
                    CsvPackageDataSource(
                        PACKAGE_FILE
                    ),

                localMapper =
                    PackageMapper(),

                validator =
                    PackageValidator(),

                warehouseMap =
                    map,

                remoteDataSource =
                    SupabasePackageRemoteDatasource(
                        client,
                        "${supabaseConfig.url}/rest/v1"
                    ),

                remoteMapper =
                    PackageRemoteMapper(),

                warehouseRepository =
                    warehouseRepository
            )

        )
            .getAllPackages()
            .data
    }


    private fun loadVehicles(
        map: Map<String, Warehouse>,
        warehouseRepository: WarehouseRepository
    ): List<Vehicle> {


        return VehicleRepositoryImpl(

            VehicleRepositoryDependencies(

                localDataSource =
                    CsvVehicleDataSource(
                        VEHICLE_FILE
                    ),

                localMapper =
                    VehicleMapper(),

                validator =
                    VehicleValidator(),

                warehouseMap =
                    map,

                remoteDataSource =
                    SupabaseVehicleRemoteDatasource(
                        client,
                        "${supabaseConfig.url}/rest/v1"
                    ),

                remoteMapper =
                    VehicleDtoMapper(),

                warehouseRepository =
                    warehouseRepository
            )

        )
            .getVehicles()
            .data
    }


    private fun loadRoutes(
        map: Map<String, Warehouse>,
        warehouseRepository: WarehouseRepository
    ): List<Route> {


        return RouteRepositoryImpl(

            RouteRepositoryDependencies(

                localDataSource =
                    CsvRouteDataSource(
                        ROUTE_FILE
                    ),

                localMapper =
                    RouteMapper(),

                validator =
                    RouteValidator(),

                warehouseMap =
                    map,

                remoteDataSource =
                    SupabaseRouteRemoteDatasource(
                        client,
                        "${supabaseConfig.url}/rest/v1"
                    ),

                remoteMapper =
                    RouteDtoMapper(),

                warehouseRepository =
                    warehouseRepository
            )

        )
            .getAllRoutes()
            .data
    }
}

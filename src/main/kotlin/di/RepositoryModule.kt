package org.example.di

import org.example.data.datasource.PackageDataSource
import org.example.data.datasource.RouteDataSource
import org.example.data.datasource.VehicleDataSource
import org.example.data.datasource.WarehouseDataSource
import org.example.data.datasource.local.csv.CsvPackageLocalDataSource
import org.example.data.datasource.local.csv.CsvRouteLocalDataSource
import org.example.data.datasource.local.csv.CsvVehicleLocalDataSource
import org.example.data.datasource.local.csv.CsvWarehouseLocalDataSource
import org.example.data.datasource.local.csv.CsvWarehouseStatusDataSource
import org.example.data.repositoryImplementation.PackageRepositoryImpl
import org.example.data.repositoryImplementation.RouteRepositoryImpl
import org.example.data.repositoryImplementation.VehicleRepositoryImpl
import org.example.data.repositoryImplementation.WarehouseRepositoryImpl
import org.example.domain.model.Warehouse
import org.example.domain.repository.PackageRepository
import org.example.domain.repository.RouteRepository
import org.example.domain.repository.VehicleRepository
import org.example.domain.repository.WarehouseRepository
import org.koin.dsl.module

private const val WAREHOUSE_STATUS_FILE =
    "src/main/resources/warehouse-status.csv"

private const val WAREHOUSE_FILE =
    "src/main/resources/warehouses.csv"

private const val PACKAGE_FILE =
    "src/main/resources/packages.csv"

private const val VEHICLE_FILE =
    "src/main/resources/fleet.csv"

private const val ROUTE_FILE =
    "src/main/resources/routes.csv"

val repositoryModule = module {

    single<PackageDataSource> {
        CsvPackageLocalDataSource(
            filePath = PACKAGE_FILE
        )
    }

    single<RouteDataSource> {
        CsvRouteLocalDataSource(
            filePath = ROUTE_FILE
        )
    }

    single<VehicleDataSource> {
        CsvVehicleLocalDataSource(
            filePath = VEHICLE_FILE
        )
    }

    single<WarehouseDataSource> {
        CsvWarehouseLocalDataSource(
            filePath = WAREHOUSE_FILE
        )
    }

    single {
        CsvWarehouseStatusDataSource(
            filePath = WAREHOUSE_STATUS_FILE
        )
    }

    single<WarehouseRepository> {
        WarehouseRepositoryImpl(
            remoteDataSource = get(),
            localDataSource = get(),
            statusDataSource = get()
        )
    }

    single<PackageRepository> { params ->

        val warehouseMap:
                Map<String, Warehouse> = params.get()

        PackageRepositoryImpl(
            remoteDataSource = get(),
            localDataSource = get(),
            warehouseMap = warehouseMap
        )
    }

    single<RouteRepository> { params ->

        val warehouseMap:
                Map<String, Warehouse> = params.get()

        RouteRepositoryImpl(
            localDataSource = get(),
            remoteDataSource = get(),
            warehouseMap = warehouseMap
        )
    }

    single<VehicleRepository> { params ->

        val warehouseMap:
                Map<String, Warehouse> = params.get()

        VehicleRepositoryImpl(
            remoteDataSource = get(),
            localDataSource = get(),
            warehouseMap = warehouseMap
        )
    }
}

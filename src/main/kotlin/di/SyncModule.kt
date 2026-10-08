package org.example.di

import org.example.data.sync.DataSynchronizer
import org.example.data.sync.PackageSynchronizer
import org.example.data.sync.RouteSynchronizer
import org.example.data.sync.SyncCoordinator
import org.example.data.sync.VehicleSynchronizer
import org.example.data.sync.WarehouseSynchronizer
import org.koin.dsl.bind
import org.koin.dsl.module

val syncModule = module {

    single {
        WarehouseSynchronizer(
            remoteDataSource = get(),
            localDataSource = get()
        )
    } bind DataSynchronizer::class

    single {
        RouteSynchronizer(
            remoteDataSource = get(),
            localDataSource = get()
        )
    } bind DataSynchronizer::class

    single {
        VehicleSynchronizer(
            remoteDataSource = get(),
            localDataSource = get()
        )
    } bind DataSynchronizer::class

    single {
        PackageSynchronizer(
            remoteDataSource = get(),
            localDataSource = get()
        )
    } bind DataSynchronizer::class

    single {
        SyncCoordinator(
            synchronizers = getAll<DataSynchronizer>()
        )
    }
}

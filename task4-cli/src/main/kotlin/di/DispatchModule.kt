package org.example.di

import org.example.domain.algorithm.dynamicprogramming.KnapsackCargoOptimizer
import org.example.domain.dispatch.BaseDispatchProcessor
import org.example.domain.dispatch.DispatchNotifier
import org.example.domain.dispatch.StandardDispatchProcessor
import org.example.domain.dispatch.VehicleCapacityReservations
import org.example.presentation.ConsoleDispatchNotifier
import org.koin.dsl.module

val dispatchModule = module {

    single {
        KnapsackCargoOptimizer()
    }

    single {
        VehicleCapacityReservations()
    }

    single<DispatchNotifier> {
        ConsoleDispatchNotifier()
    }

    factory<BaseDispatchProcessor> {
        StandardDispatchProcessor(
            capacityReservations = get(),
            notifier = get()
        )
    }
}

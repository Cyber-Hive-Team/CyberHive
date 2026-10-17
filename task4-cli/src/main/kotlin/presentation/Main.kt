package org.example.presentation

import kotlinx.coroutines.CancellationException
import org.example.presentation.cli.launchApplicationShell
import org.example.presentation.cli.launchDemoShell

import org.example.di.dispatchModule
import org.example.di.networkModule
import org.example.di.repositoryModule
import org.example.di.useCaseModule
import org.example.di.validatorModule
import org.example.domain.algorithm.dynamicprogramming.KnapsackCargoOptimizer
import org.example.domain.dispatch.BaseDispatchProcessor
import org.example.domain.usecase.AnalyzeTreePerformanceUseCase
import org.example.domain.usecase.DispatchFleetGreedyUseCase
import org.example.domain.usecase.TraceHubLineageUseCase
import org.koin.core.Koin
import org.koin.core.context.startKoin
import org.koin.core.parameter.parametersOf
import org.example.di.syncModule
import org.example.data.sync.SyncCoordinator

suspend fun main(args: Array<String>) {
    if ("--demo" in args) {
        launchDemoShell()
        return
    }
    println("=== Cyber Hive ===")

    val koin = startKoin {
        modules(
            networkModule,
            repositoryModule,
            validatorModule,
            useCaseModule,
            dispatchModule,
            syncModule

        )
    }.koin

    try {
        val syncCoordinator = koin.get<SyncCoordinator>()
        try {
            syncCoordinator.syncAll()
        } catch (cancelled: CancellationException) {
            throw cancelled
        } catch (exception: Exception) {
            println("Warning: Sync failed, using local CSV data. Reason: ${exception.message}")
        }

        val data = koin.get<DataLoader>().load()
        if (data.warehouses.isEmpty()) {
            println("ERROR: No warehouses found.")
            return
        }
        if ("--demos" in args) runDemos(koin, data)
        else launchApplicationShell(koin)
    } finally {
        koin.get<io.ktor.client.HttpClient>().close()
        org.koin.core.context.stopKoin()
    }
}

private suspend fun runDemos(
    koin: Koin,
    data: LoadedData
) {
    runPatternDemos(koin, data)
    runCommandAndFleetDemos(koin, data)
    runEndToEndDispatch(koin, data)
}

private fun runPatternDemos(
    koin: Koin,
    data: LoadedData
) {
    val analyzeTreePerformanceUseCase =
        koin.get<AnalyzeTreePerformanceUseCase>()

    PricingDemoRunner(data.warehouses).run()
    DecoratorDemoRunner(data.warehouses).run()
    SortingDemoRunner(data.warehouses).run()
    ConsistentHashRoutingRunner(data.warehouses).run()
    RoutingComparisonRunner(data.warehouses, data.routes).run()
    TreePerformanceDemoRunner(analyzeTreePerformanceUseCase).run()

    TraceHubLineageDemoRunner(
        warehouses = data.warehouses,
        routes = data.routes,
        traceHubLineageUseCaseFactory = { tree ->
            koin.get<TraceHubLineageUseCase> {
                parametersOf(tree)
            }
        }
    ).run("WH-028")
}

private suspend fun runCommandAndFleetDemos(
    koin: Koin,
    data: LoadedData
) {
    val dispatchFleetGreedyUseCase = koin.get<DispatchFleetGreedyUseCase>()
    CommandInvokerDemoRunner(data.warehouses).run()
    GreedyFleetDispatcherRunner(dispatchFleetGreedyUseCase).run()
}

private fun runEndToEndDispatch(
    koin: Koin,
    data: LoadedData
) {
    val optimizer = koin.get<KnapsackCargoOptimizer>()

    val dispatchProcessor = koin.get<BaseDispatchProcessor>()

    EndToEndDispatchRunner(
        optimizer = optimizer, dispatchProcessor = dispatchProcessor
    ).run(vehicles = data.vehicles, packages = data.packages)
}

package org.example.presentation

import kotlinx.coroutines.CancellationException
import org.example.data.sync.SyncCoordinator
import org.example.di.dispatchModule
import org.example.di.networkModule
import org.example.di.repositoryModule
import org.example.di.syncModule
import org.example.di.useCaseModule
import org.example.di.validatorModule
import org.example.domain.algorithm.dynamicprogramming.KnapsackCargoOptimizer
import org.example.domain.dispatch.BaseDispatchProcessor
import org.example.domain.usecase.AnalyzeTreePerformanceUseCase
import org.example.domain.usecase.DispatchFleetGreedyUseCase
import org.example.domain.usecase.TraceHubLineageUseCase
import org.example.domain.usecase.TrackActiveFleetUseCase
import org.example.presentation.cli.CliShell
import org.example.presentation.cli.ConsoleInput
import org.koin.core.Koin
import org.koin.core.context.startKoin
import org.koin.core.parameter.parametersOf

suspend fun main() {
    println("=== Cyber Hive ===")

    val application = startKoin {
        modules(
            networkModule,
            repositoryModule,
            validatorModule,
            useCaseModule,
            dispatchModule,
            syncModule

        )
    }
    try {
        runApplication(application.koin)
    } finally {
        application.close()
    }
}

private suspend fun runApplication(koin: Koin) {

    val syncCoordinator = koin.get<SyncCoordinator>()

    runCatching {
        syncCoordinator.syncAll()
    }.onFailure { exception ->
        if (exception is CancellationException) throw exception
        println(
            "Warning: Sync failed, using local CSV data. " +
                    "Reason: ${exception.message}"
        )
    }

    val data = koin.get<DataLoader>().load()

    if (data.warehouses.isEmpty()) {
        println("ERROR: No warehouses found.")
        return
    }
    runDemos(koin, data)
    runCli(koin, data)
}

private suspend fun runDemos(
    koin: Koin,
    data: LoadedData
) {
    runPatternDemos(koin, data)
    runCommandAndFleetDemos(koin, data)
    runEndToEndDispatch(koin, data)
    runFleetTelemetryDemo(koin, data)

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
private suspend fun runFleetTelemetryDemo(
    koin: Koin,
    data: LoadedData
) {
    val vehicleIds = data.vehicles
        .take(2)
        .map { it.id }

    val useCase = koin.get<TrackActiveFleetUseCase>()

    FleetTelemetryDemoRunner(useCase).run(vehicleIds)
}
private suspend fun runCli(
    koin: Koin,
    data: LoadedData
) {
    CliShell(
        input = ConsoleInput(),
        findOptimalPath = koin.get(),
        classifyPackageSector = koin.get(),
        trackActiveFleet = koin.get(),
        vehicleIds = data.vehicles.map { it.id }
    ).run()
}

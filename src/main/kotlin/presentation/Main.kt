package org.example.presentation

import org.example.di.networkModule
import org.example.di.repositoryModule
import org.example.di.useCaseModule
import org.example.di.validatorModule
import org.example.domain.usecase.AnalyzeTreePerformanceUseCase
import org.example.domain.usecase.DispatchFleetGreedyUseCase
import org.example.domain.usecase.TraceHubLineageUseCase
import org.koin.core.context.startKoin
import org.koin.core.parameter.parametersOf

suspend fun main() {

    println("=== Cyber Hive ===")

    val koin = startKoin {
        modules(networkModule, repositoryModule, validatorModule, useCaseModule)
    }.koin

    val dataLoader = koin.get<DataLoader>()
    val data = dataLoader.load()

    if (data.warehouses.isEmpty()) {
        println("ERROR: No warehouses found.")
        return
    }
    val dispatchFleetGreedyUseCase = koin.get<DispatchFleetGreedyUseCase>()
    val analyzeTreePerformanceUseCase = koin.get<AnalyzeTreePerformanceUseCase>()

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
            koin.get<TraceHubLineageUseCase> { parametersOf(tree) }
        }
    ).run("WH-028")

    CommandInvokerDemoRunner(data.warehouses).run()
    GreedyFleetDispatcherRunner(dispatchFleetGreedyUseCase).run()
}

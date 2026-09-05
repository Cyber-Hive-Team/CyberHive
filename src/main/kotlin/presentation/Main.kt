package org.example.presentation

import org.example.domain.usecase.AnalyzeTreePerformanceUseCase


fun main() {
    println("=== Cyber Hive ===")
    val data = DataLoader().load()

    if (data.warehouses.isEmpty()) {
        println("ERROR: No warehouses found.")
        return
    }

    PricingDemoRunner(data.warehouses).run()
    DecoratorDemoRunner(data.warehouses).run()
    SortingDemoRunner(data.warehouses).run()
    ConsistentHashRoutingRunner(data.warehouses).run()
    RoutingComparisonRunner(data.warehouses, data.routes).run()
    TreePerformanceDemoRunner(AnalyzeTreePerformanceUseCase()).run()
    TraceHubLineageDemoRunner().run("WH-028")

}

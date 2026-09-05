package org.example.presentation



import org.example.domain.builder.DomainGraphBuilder
import org.example.domain.usecase.AnalyzeTreePerformanceUseCase




fun main() {
    println("=== Cyber Hive ===")
    val data = DataLoader().load()

    if (data.warehouses.isEmpty()) {
        println("ERROR: No warehouses found.")
        return
    }

    val result = DomainGraphBuilder()
        .buildConnectedDomainGraph(
            data.warehouses,
            data.packages,
            data.vehicles,
            data.routes
        )

    println("\n=== Building Domain Graph ===\nConnected hubs: ${result.success.size}")

    result.warnings.forEach {
        println("WARNING: $it")
    }

    if (result.success.isEmpty()) {
        println("ERROR: Domain graph building failed.")
        return
    }

    PricingDemoRunner(result.success).run()
    DecoratorDemoRunner(result.success).run()
    SortingDemoRunner(result.success).run()
    ConsistentHashRoutingRunner(result.success).run()
    RoutingComparisonRunner(result.success, data.routes).run()

    AnalyzeTreePerformanceUseCase()
    CommandInvokerDemoRunner(result.success).run()
}

package org.example.presentation

import data.remote.config.SupabaseConfig
import org.example.domain.usecase.AnalyzeTreePerformanceUseCase


fun main() {
    val supabaseConfig = SupabaseConfig(
        url = requireNotNull(
            System.getenv("SUPABASE_URL")
        ),
        publishableKey = requireNotNull(
            System.getenv("SUPABASE_PUBLISHABLE_KEY")
        )
    )


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
    CommandInvokerDemoRunner(data.warehouses).run()


}

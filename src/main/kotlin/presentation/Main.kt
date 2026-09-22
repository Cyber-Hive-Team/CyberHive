package org.example.presentation

import org.example.data.remote.client.SupabaseHttpClient
import org.example.data.remote.config.SupabaseConfig
import org.example.domain.algorithm.greedy.GreedyFleetDispatcher
import org.example.domain.usecase.AnalyzeTreePerformanceUseCase
import org.example.domain.usecase.DispatchFleetGreedyUseCase
import java.io.IOException
import org.example.data.retry.retryWithBackoff
import org.example.domain.model.exception.InvalidDataException


suspend fun main() {
    println("=== Cyber Hive ===")
    println(System.getenv("SUPABASE_URL"))
    println(System.getenv("SUPABASE_PUBLISHABLE_KEY"))
    val supabaseConfig = SupabaseConfig(
        url = requireNotNull(System.getenv("SUPABASE_URL")),
        publishableKey = requireNotNull(
            System.getenv("SUPABASE_PUBLISHABLE_KEY")
        )
    )
    val httpClient = SupabaseHttpClient(supabaseConfig)
    val dataLoader = DataLoader(httpClient = httpClient, supabaseConfig = supabaseConfig)
    val data = dataLoader.load()
    if (data.warehouses.isEmpty()) {
        println("ERROR: No warehouses found.")
        return
    }
    val dispatchFleetGreedyUseCase =
        DispatchFleetGreedyUseCase(vehicleRepository = data.vehicleRepository, dispatcher = GreedyFleetDispatcher())
    PricingDemoRunner(data.warehouses).run()
    DecoratorDemoRunner(data.warehouses).run()
    SortingDemoRunner(data.warehouses).run()
    ConsistentHashRoutingRunner(data.warehouses).run()
    RoutingComparisonRunner(data.warehouses, data.routes).run()
    TreePerformanceDemoRunner(AnalyzeTreePerformanceUseCase()).run()
    TraceHubLineageDemoRunner(dataLoader).run("WH-028")
    CommandInvokerDemoRunner(data.warehouses).run()
    GreedyFleetDispatcherRunner(dispatchFleetGreedyUseCase).run()



        var calls = 0
        val result = retryWithBackoff {
            calls++
            if (calls < 3) throw IOException("timeout")
            "OK"
        }
        println(result)
    }


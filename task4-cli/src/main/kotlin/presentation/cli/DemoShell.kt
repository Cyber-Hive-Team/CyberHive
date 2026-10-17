package org.example.presentation.cli

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.currentCoroutineContext
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.isActive
import kotlinx.coroutines.withContext
import org.example.domain.algorithm.search.DijkstraRouter
import org.example.domain.algorithm.search.RouteWarehouseGraph
import org.example.domain.model.RegionalZone
import org.example.domain.model.Route
import org.example.domain.model.Warehouse
import kotlin.math.sqrt

/** Explicit fixtures for demonstrating Member 4 before Members 2 and 3 are merged. */
fun demoShell(terminal: Terminal): InteractiveShell {
    terminal.printLine("DEMO MODE: synthetic KNN training cargo and simulated telemetry.")
    terminal.printLine("Route sample: WH-001 -> WH-003. Classification sample: 31.9 35.2 5")
    val warehouses = listOf(
        Warehouse("WH-001", "North", RegionalZone.NORTH, 32.5, 35.3),
        Warehouse("WH-002", "Central", RegionalZone.CENTRAL, 31.9, 35.2),
        Warehouse("WH-003", "South", RegionalZone.SOUTH, 31.3, 34.8),
    )
    val routes = listOf(
        Route("RT-00001", 75.0, 0, warehouses[0], warehouses[1]),
        Route("RT-00002", 80.0, 0, warehouses[1], warehouses[2]),
        Route("RT-00003", 200.0, 0, warehouses[0], warehouses[2]),
    )
    return InteractiveShell(
        terminal,
        RouteCalculator { start, end ->
            withContext(Dispatchers.Default) {
                val origin = warehouses.firstOrNull { it.id == start }
                    ?: error("Unknown start warehouse: $start")
                val destination = warehouses.firstOrNull { it.id == end }
                    ?: error("Unknown destination warehouse: $end")
                val result = DijkstraRouter(RouteWarehouseGraph(routes), warehouses).findPath(origin, destination)
                if (result.path.isEmpty()) "No route found."
                else "${result.path.joinToString(" -> ") { it.id }} | ${result.distanceKm} km"
            }
        },
        PackageSectorClassifier { features ->
            withContext(Dispatchers.Default) { demoClassification(features, warehouses) }
        },
        FleetTelemetry {
            flow {
                var tick = 0
                while (currentCoroutineContext().isActive) {
                    val speed = if (tick % 3 == 0) 0 else 45
                    emit("[DEMO TRK-0001] GPS=${31.9 + tick * 0.0001},35.2 " +
                        "speed=$speed km/h fuel=${(100 - tick).coerceAtLeast(0)}%" +
                        if (speed == 0) " | DELAY: vehicle stopped" else "")
                    tick++
                    delay(1000)
                }
            }
        },
    )
}

private fun demoClassification(input: PackageFeatures, warehouses: List<Warehouse>): String {
    val training = warehouses.flatMap { warehouse ->
        (1..5).map { index ->
            PackageFeatures(warehouse.latitude + index * 0.001, warehouse.longitude, index * 2.0) to
                warehouse.regionalZone.name
        }
    }
    val columns = listOf<(PackageFeatures) -> Double>({ it.latitude }, { it.longitude }, { it.weightKg })
    val bounds = columns.map { feature ->
        training.minOf { feature(it.first) } to training.maxOf { feature(it.first) }
    }
    fun normalized(features: PackageFeatures): List<Double> = columns.mapIndexed { index, feature ->
        val (min, max) = bounds[index]
        if (max == min) 0.0 else ((feature(features) - min) / (max - min)).coerceIn(0.0, 1.0)
    }
    val target = normalized(input)
    return training.sortedBy { (features, _) ->
        sqrt(normalized(features).zip(target).sumOf { (a, b) -> (a - b) * (a - b) })
    }.take(5).groupingBy { it.second }.eachCount().entries
        .sortedWith(compareByDescending<Map.Entry<String, Int>> { it.value }.thenBy { it.key })
        .first().key + " (DEMO synthetic cargo, k=5)"
}

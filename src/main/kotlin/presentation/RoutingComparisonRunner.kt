package org.example.presentation

import org.example.domain.algorithm.benchmark.BenchmarkReporter
import org.example.domain.algorithm.benchmark.RoutingBenchmark
import org.example.domain.algorithm.search.BreadthFirstSearchRouter
import org.example.domain.algorithm.search.DijkstraRouter
import org.example.domain.algorithm.search.RouteWarehouseGraph
import org.example.domain.model.Route
import org.example.domain.model.result.RoutingResult
import org.example.domain.model.Warehouse

class RoutingComparisonRunner(
    private val warehouses: List<Warehouse>,
    private val routes: List<Route>
) {

    fun run() {
        if (warehouses.size < 2) {
            println("Not enough warehouses.")
            return
        }

        val start = warehouses.first()
        val destination = warehouses.last()
        val graph = RouteWarehouseGraph(routes)

        val bfs = BreadthFirstSearchRouter(graph).findPath(start, destination)
        val dijkstra = DijkstraRouter(graph, warehouses).findPath(start, destination)

        printComparison(start, destination, bfs, dijkstra)
        runBenchmark(graph, start, destination)
    }

    private fun printComparison(
        start: Warehouse,
        destination: Warehouse,
        bfs: RoutingResult,
        dijkstra: RoutingResult
    ) {
        println("\n=== Routing Algorithms Comparison ===")
        println("Start: ${start.id}")
        println("Destination: ${destination.id}")

        printPathResult("BFS", bfs)
        printPathResult("Dijkstra", dijkstra)
    }

    private fun printPathResult(name: String, result: RoutingResult) {
        println("\n--- $name ---")

        if (result.path.isEmpty()) {
            println("No path found.")
            return
        }

        println("Path: " + result.path.joinToString(" -> ") { it.id })
        println("Hops: ${result.path.size - 1}")
        println("Distance: ${result.distanceKm} km")
    }

    private fun runBenchmark(
        graph: RouteWarehouseGraph,
        start: Warehouse,
        destination: Warehouse
    ) {
        println("\n=== BFS vs Bidirectional BFS Benchmark ===")

        val benchmark = RoutingBenchmark(graph)
        val result = benchmark.compare(start, destination)

        val reporter = BenchmarkReporter()
        reporter.printResults(result)
        reporter.printValidation(
            benchmark.validate(result, start, destination)
        )
    }
}

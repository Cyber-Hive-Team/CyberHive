package org.example

import org.example.data.datasource.CsvPackageDataSource
import org.example.data.datasource.CsvRouteDataSource
import org.example.data.datasource.CsvVehicleDataSource
import org.example.data.datasource.CsvWarehouseDataSource
import org.example.data.mapper.PackageMapper
import org.example.data.mapper.RouteMapper
import org.example.data.mapper.VehicleMapper
import org.example.data.mapper.WarehouseMapper
import org.example.data.repository.CsvPackageRepository
import org.example.data.repository.CsvRouteRepository
import org.example.data.repository.CsvVehicleRepository
import org.example.data.repository.CsvWarehouseRepository
import org.example.domain.algorithm.benchmark.BenchmarkReporter
import org.example.domain.algorithm.benchmark.RoutingBenchmark
import org.example.domain.algorithm.search.BreadthFirstSearchRouter
import org.example.domain.algorithm.search.DijkstraRouter
import org.example.domain.algorithm.search.RouteWarehouseGraph
import org.example.domain.builder.DomainGraphBuilder
import org.example.domain.decorator.ColdChainDecorator
import org.example.domain.decorator.ExpressInsuranceDecorator
import org.example.domain.decorator.FragileHandlingDecorator
import org.example.domain.model.*
import org.example.domain.pricing.EcoStrategy
import org.example.domain.pricing.ExpressStrategy
import org.example.domain.pricing.FragileStrategy
import org.example.domain.pricing.RoutePricingEngine
import org.example.domain.routing.report.RoutingValidationReporter
import org.example.domain.routing.service.ConsistentHashVehicleRoutingService

private const val WAREHOUSE_FILE = "src/main/resources/warehouses.csv"
private const val PACKAGE_FILE = "src/main/resources/packages.csv"
private const val VEHICLE_FILE = "src/main/resources/fleet.csv"
private const val ROUTE_FILE = "src/main/resources/routes.csv"
private const val MIN_VEHICLES = 4
private const val FAILURE_SLOT = 40
private const val MIN_WAREHOUSES_FOR_ROUTING = 2

private data class LoadedData(
    val warehouses: List<Warehouse>,
    val packages: List<Package>,
    val vehicles: List<Vehicle>,
    val routes: List<Route>
)

fun main() {
    println("=== Cyber Hive ===")
    val data = loadData()

    if (data.warehouses.isEmpty()) {
        println("ERROR: No warehouses found.")
        return
    }

    val result = buildDomainGraph(data)
    printGraphResult(result)

    if (result.success.isEmpty()) {
        println("ERROR: Domain graph building failed.")
        return
    }

    testPricing(result.success)
    testDecorator(result.success)
    testSorting(result.success)
    runRouting(result.success)
    compareRoutingAlgorithms(result.success, data.routes)
}

private fun loadData(): LoadedData {
    val warehouses = loadWarehouses()
    val map = warehouses.associateBy { it.id }
    val packages = loadPackages(map)
    val vehicles = loadVehicles(map)
    val routes = loadRoutes(map)

    println("\n=== Parsing Results ===")
    println("Warehouses: ${warehouses.size}")
    println("Packages: ${packages.size}")
    println("Vehicles: ${vehicles.size}")
    println("Routes: ${routes.size}")

    return LoadedData(warehouses, packages, vehicles, routes)
}

private fun buildDomainGraph(
    data: LoadedData
) = DomainGraphBuilder().buildConnectedDomainGraph(
    warehouses = data.warehouses,
    packages = data.packages,
    vehicles = data.vehicles,
    routes = data.routes
)

private fun printGraphResult(result: Any) {
    println("\n=== Building Domain Graph ===")
}

private fun loadWarehouses(): List<Warehouse> {
    val result = CsvWarehouseRepository(
        CsvWarehouseDataSource(WAREHOUSE_FILE),
        WarehouseMapper()
    ).getAllWarehouses()

    result.errorMessage?.let { println("WARNING: $it") }
    return result.data.orEmpty()
}

private fun loadPackages(
    map: Map<String, Warehouse>
): List<Package> {
    val result = CsvPackageRepository(
        CsvPackageDataSource(PACKAGE_FILE),
        PackageMapper(),
        map
    ).getAllPackages()

    result.errorMessage?.let { println("WARNING: $it") }
    return result.data.orEmpty()
}

private fun loadVehicles(
    map: Map<String, Warehouse>
): List<Vehicle> {
    val result = CsvVehicleRepository(
        CsvVehicleDataSource(VEHICLE_FILE),
        VehicleMapper(),
        map
    ).getVehicles()

    result.errorMessage?.let { println("WARNING: $it") }
    return result.data.orEmpty()
}

private fun loadRoutes(
    map: Map<String, Warehouse>
): List<Route> {
    val result = CsvRouteRepository(
        CsvRouteDataSource(ROUTE_FILE),
        RouteMapper(),
        map
    ).getAllRoutes()

    result.errorMessage?.let { println("WARNING: $it") }
    return result.data.orEmpty()
}

private fun testPricing(
    warehouses: List<Warehouse>
) {
    println("\n=== Strategy Pattern Pricing ===")

    val warehouse = warehouses.firstOrNull()
    val item = warehouse?.getCargoQueue()?.firstOrNull()
    val route = warehouse?.getOutgoingRoutes()?.firstOrNull()

    if (item == null || route == null) {
        println("No package or route available.")
        return
    }

    val engine = RoutePricingEngine(EcoStrategy())
    val strategies = listOf(
        "EcoStrategy" to EcoStrategy(),
        "ExpressStrategy" to ExpressStrategy(),
        "FragileStrategy" to FragileStrategy()
    )

    strategies.forEach { (name, strategy) ->
        engine.setStrategy(strategy)
        println("$name price: $${engine.calculatePrice(item, route)}")
    }
}

private fun testSorting(
    warehouses: List<Warehouse>
) {
    println("\n=== Quick Sort (Weight Descending) ===")

    val warehouse = warehouses.firstOrNull()
        ?: return println("No hub available for sorting.")

    printCargo("Before Sorting", warehouse)

    warehouses
        .filter { it.getCargoQueue().isNotEmpty() }
        .forEach { it.sortCargoQueue() }

    printCargo("After Sorting", warehouse)
}

private fun printCargo(
    title: String,
    warehouse: Warehouse
) {
    println("--- $title (${warehouse.id}) ---")

    warehouse.getCargoQueue().forEach {
        println(
            "${it.id} (Priority: ${it.priority}, Weight: ${it.weight}kg)"
        )
    }
}

private fun runRouting(
    warehouses: List<Warehouse>
) {
    println("\n=== Consistent Hash Routing ===")

    val warehouse = warehouses.firstOrNull {
        it.getStationedVehicles().size >= MIN_VEHICLES
    }

    if (warehouse == null) {
        println("Cannot test routing: no warehouse has enough vehicles.")
        return
    }

    val service = ConsistentHashVehicleRoutingService()
    val before = service.assignPackagesToVehicles(
        warehouse.getCargoQueue(),
        warehouse.getStationedVehicles()
    )

    val failed = service.getVehiclesBySlot()[FAILURE_SLOT]

    if (failed == null) {
        println("No vehicle found at slot $FAILURE_SLOT.")
        return
    }

    val after = service.handleVehicleFailure(
        before,
        failed.id,
        FAILURE_SLOT
    )

    printRoutingResult(service, before, after, failed.id)
}

private fun printRoutingResult(
    service: ConsistentHashVehicleRoutingService,
    before: Map<Vehicle, List<Package>>,
    after: Map<Vehicle, List<Package>>,
    failedId: String
) {
    println("=== Package allocation before failure ===")
    printAllocation(service, before)

    println("=== Package allocation after failure ===")
    printAllocation(service, after)

    val report = RoutingValidationReporter().createReport(
        before,
        after,
        failedId
    )

    println("=== Routing validation report ===")
    report.messages.forEach(::println)
    println("Stable packages: ${report.stablePackageCount}")
    println("Rerouted packages: ${report.reroutedPackageCount}")
    println("All validations passed: ${report.allPassed}")
}

private fun printAllocation(
    service: ConsistentHashVehicleRoutingService,
    allocation: Map<Vehicle, List<Package>>
) {
    service.getVehiclesBySlot()
        .toSortedMap()
        .forEach { (slot, vehicle) ->
            val packages = allocation[vehicle]
                .orEmpty()
                .joinToString { it.id }

            println("Slot $slot -> ${vehicle.id} -> $packages")
        }
}

private fun testDecorator(
    warehouses: List<Warehouse>
) {
    println("\n=== Decorator Pattern ===")

    val item = warehouses.firstOrNull()
        ?.getCargoQueue()
        ?.firstOrNull()

    if (item == null) {
        println("No package available for decorator test.")
        return
    }

    var decorated: PackageComponent = item
    printDecorator("Original", decorated)

    decorated = FragileHandlingDecorator(decorated)
    printDecorator("After Fragile Handling", decorated)

    decorated = ColdChainDecorator(decorated)
    printDecorator("After Cold Chain", decorated)

    decorated = ExpressInsuranceDecorator(decorated)
    printDecorator("After Express Insurance", decorated)
}

private fun printDecorator(
    title: String,
    component: PackageComponent
) {
    println("\n$title:")
    println("Description: ${component.getDescription()}")
    println("Transit Rate: ${component.calculateTransitRate()}")
}

private fun compareRoutingAlgorithms(
    warehouses: List<Warehouse>,
    routes: List<Route>
) {
    println("\n=== Routing Algorithms Comparison ===")

    if (warehouses.size < MIN_WAREHOUSES_FOR_ROUTING) {
        println("Not enough warehouses to test routing.")
        return
    }

    val start = warehouses.first()
    val destination = warehouses.last()
    val graph = RouteWarehouseGraph(routes)

    println("Start: ${start.id}")
    println("Destination: ${destination.id}")

    runBfs(graph, start, destination)
    runDijkstra(warehouses, start, destination)
    runBidirectionalBenchmark(graph, start, destination)
}

private fun runBfs(
    graph: RouteWarehouseGraph,
    start: Warehouse,
    destination: Warehouse
) {
    val router = BreadthFirstSearchRouter(graph)
    val startTime = System.nanoTime()
    val result = router.findPath(start, destination)
    val time = System.nanoTime() - startTime

    printBfsResult(result, time)
}

private fun runDijkstra(
    warehouses: List<Warehouse>,
    start: Warehouse,
    destination: Warehouse
) {
    val router = DijkstraRouter(warehouses)
    val startTime = System.nanoTime()
    val result = router.findPath(start, destination)
    val time = System.nanoTime() - startTime

    printDijkstraResult(result, time)
}

private fun printBfsResult(
    result: RoutingResult,
    time: Long
) {
    println("\n--- BFS ---")

    if (result.path.isEmpty()) {
        println("No path found.")
        return
    }

    printPathResult(result)
    printTime(time)
}

private fun printDijkstraResult(
    result: RoutingResult,
    time: Long
) {
    println("\n--- Dijkstra ---")

    if (result.path.isEmpty()) {
        println("No path found.")
        return
    }

    printPathResult(result)
    printTime(time)
}

private fun printPathResult(
    result: RoutingResult
) {
    println("Path: ${result.path.joinToString(" -> ") { it.id }}")
    println("Hops: ${result.path.size - 1}")
    println("Distance: ${result.distanceKm} km")
}

private fun printTime(
    time: Long
) {
    println("Execution time: $time ns")
    println("Execution time: ${time / 1_000_000.0} ms")
}

private fun runBidirectionalBenchmark(
    graph: RouteWarehouseGraph,
    start: Warehouse,
    destination: Warehouse
) {
    println("\n=== BFS vs Bidirectional BFS Benchmark ===")

    val benchmark = RoutingBenchmark(graph)
    val reporter = BenchmarkReporter()
    val result = benchmark.compare(start, destination)

    printBenchmarkResult(result)
    reporter.printResults(result)

    val valid = benchmark.validate(result, start, destination)
    reporter.printValidation(valid)
}

private fun printBenchmarkResult(
    result: org.example.domain.algorithm.benchmark.BenchmarkResult
) {
    println("\n--- Benchmark Result ---")
    printBenchmarkPath("BFS", result.bfsPath)
    println("BFS Evaluated: ${result.bfsEvaluated}")
    println("BFS Distance: ${calculatePathDistance(result.bfsPath)} km")
    println("BFS Time: ${result.bfsTime} ns")

    printBenchmarkPath(
        "Bidirectional BFS",
        result.bidirectionalPath
    )
    println("Bidirectional BFS Evaluated: ${result.bidirectionalEvaluated}")
    println(
        "Bidirectional BFS Distance: " +
                "${calculatePathDistance(result.bidirectionalPath)} km"
    )
    println("Bidirectional BFS Time: ${result.bidirectionalTime} ns")
}

private fun printBenchmarkPath(
    name: String,
    path: List<Warehouse>
) {
    println(
        "$name Path: " +
                path.joinToString(" -> ") { it.id }
    )
    println(
        "$name Hops: " +
                if (path.isEmpty()) 0 else path.size - 1
    )
}

private fun calculatePathDistance(
    path: List<Warehouse>
): Double =
    path.zipWithNext().sumOf { (current, next) ->
        current.getOutgoingRoutes()
            .firstOrNull {
                it.destinationWarehouse == next
            }
            ?.distanceKm
            ?: 0.0
    }
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

private const val WAREHOUSE_FILE =
    "src/main/resources/warehouses.csv"
private const val PACKAGE_FILE =
    "src/main/resources/packages.csv"
private const val VEHICLE_FILE =
    "src/main/resources/fleet.csv"
private const val ROUTE_FILE =
    "src/main/resources/routes.csv"

private const val MIN_VEHICLES = 4
private const val FAILURE_SLOT = 40

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

    val result = DomainGraphBuilder()
        .buildConnectedDomainGraph(
            data.warehouses,
            data.packages,
            data.vehicles,
            data.routes
        )

    println("\n=== Building Domain Graph ===")
    println("Connected hubs: ${result.success.size}")

    result.warnings.forEach {
        println("WARNING: $it")
    }

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

    return LoadedData(
        warehouses,
        packages,
        vehicles,
        routes
    )
}

private fun loadWarehouses(): List<Warehouse> {
    val result = CsvWarehouseRepository(
        CsvWarehouseDataSource(WAREHOUSE_FILE),
        WarehouseMapper()
    ).getAllWarehouses()

    result.errorMessage?.let {
        println("WARNING: $it")
    }

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

    result.errorMessage?.let {
        println("WARNING: $it")
    }

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

    result.errorMessage?.let {
        println("WARNING: $it")
    }

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

    result.errorMessage?.let {
        println("WARNING: $it")
    }

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

    listOf(
        "EcoStrategy" to EcoStrategy(),
        "ExpressStrategy" to ExpressStrategy(),
        "FragileStrategy" to FragileStrategy()
    ).forEach { (name, strategy) ->
        engine.setStrategy(strategy)

        println(
            "$name price: $" +
                    engine.calculatePrice(item, route)
        )
    }
}

private fun testSorting(
    warehouses: List<Warehouse>
) {
    println("\n=== Quick Sort (Weight Descending) ===")

    val warehouse = warehouses.firstOrNull()
        ?: return println("No hub available.")

    printCargo("Before Sorting", warehouse)

    warehouses
        .filter {
            it.getCargoQueue().isNotEmpty()
        }
        .forEach {
            it.sortCargoQueue()
        }

    printCargo("After Sorting", warehouse)
}

private fun printCargo(
    title: String,
    warehouse: Warehouse
) {
    println("--- $title (${warehouse.id}) ---")

    warehouse.getCargoQueue().forEach {
        println(
            "${it.id} " +
                    "(Priority: ${it.priority}, " +
                    "Weight: ${it.weight}kg)"
        )
    }
}

private fun runRouting(
    warehouses: List<Warehouse>
) {
    println("\n=== Consistent Hash Routing ===")

    val warehouse = warehouses.firstOrNull {
        it.getStationedVehicles().size >= MIN_VEHICLES
    } ?: return println("No warehouse has enough vehicles.")

    val service = ConsistentHashVehicleRoutingService()

    val before = service.assignPackagesToVehicles(
        warehouse.getCargoQueue(),
        warehouse.getStationedVehicles()
    )

    val failed = service.getVehiclesBySlot()[FAILURE_SLOT]
        ?: return println("No vehicle at slot $FAILURE_SLOT.")

    val after = service.handleVehicleFailure(
        before,
        failed.id,
        FAILURE_SLOT
    )

    printRoutingResult(
        service,
        before,
        after,
        failed.id
    )
}

private fun printRoutingResult(
    service: ConsistentHashVehicleRoutingService,
    before: Map<Vehicle, List<Package>>,
    after: Map<Vehicle, List<Package>>,
    failedId: String
) {
    println("=== Allocation Before Failure ===")
    printAllocation(service, before)

    println("=== Allocation After Failure ===")
    printAllocation(service, after)

    val report = RoutingValidationReporter()
        .createReport(
            before,
            after,
            failedId
        )

    report.messages.forEach(::println)

    println(
        "Stable packages: " +
                report.stablePackageCount
    )

    println(
        "Rerouted packages: " +
                report.reroutedPackageCount
    )

    println(
        "All validations passed: " +
                report.allPassed
    )
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

            println(
                "Slot $slot -> " +
                        "${vehicle.id} -> $packages"
            )
        }
}

private fun testDecorator(
    warehouses: List<Warehouse>
) {
    println("\n=== Decorator Pattern ===")

    val item = warehouses
        .firstOrNull()
        ?.getCargoQueue()
        ?.firstOrNull()
        ?: return println("No package available.")

    var decorated: PackageComponent = item

    printDecorator("Original", decorated)

    decorated =
        FragileHandlingDecorator(decorated)
    printDecorator(
        "After Fragile Handling",
        decorated
    )

    decorated =
        ColdChainDecorator(decorated)
    printDecorator(
        "After Cold Chain",
        decorated
    )

    decorated =
        ExpressInsuranceDecorator(decorated)
    printDecorator(
        "After Express Insurance",
        decorated
    )
}

private fun printDecorator(
    title: String,
    item: PackageComponent
) {
    println("\n$title:")
    println("Description: ${item.getDescription()}")
    println("Transit Rate: ${item.calculateTransitRate()}")
}

private fun compareRoutingAlgorithms(
    warehouses: List<Warehouse>,
    routes: List<Route>
) {
    if (warehouses.size < 2) {
        println("Not enough warehouses.")
        return
    }

    val start = warehouses.first()
    val destination = warehouses.last()
    val graph = RouteWarehouseGraph(routes)

    val bfs = BreadthFirstSearchRouter(graph)
        .findPath(start, destination)

    val dijkstra = DijkstraRouter(graph, warehouses).findPath(start, destination)

    printComparison(
        start,
        destination,
        bfs,
        dijkstra
    )

    runBenchmark(
        graph,
        start,
        destination
    )
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

private fun printPathResult(
    name: String,
    result: RoutingResult
) {
    println("\n--- $name ---")

    if (result.path.isEmpty()) {
        println("No path found.")
        return
    }

    println(
        "Path: " +
                result.path.joinToString(" -> ") {
                    it.id
                }
    )

    println(
        "Hops: ${result.path.size - 1}"
    )

    println(
        "Distance: ${result.distanceKm} km"
    )
}

private fun runBenchmark(
    graph: RouteWarehouseGraph,
    start: Warehouse,
    destination: Warehouse
) {
    println(
        "\n=== BFS vs Bidirectional BFS Benchmark ==="
    )

    val benchmark = RoutingBenchmark(graph)
    val result = benchmark.compare(
        start,
        destination
    )

    val reporter = BenchmarkReporter()

    reporter.printResults(result)

    reporter.printValidation(
        benchmark.validate(
            result,
            start,
            destination
        )
    )
}
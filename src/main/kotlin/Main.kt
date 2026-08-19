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
import org.example.domain.builder.DomainGraphBuilder
import org.example.domain.model.Package
import org.example.domain.model.Route
import org.example.domain.model.Vehicle
import org.example.domain.model.Warehouse
import org.example.domain.pricing.EcoStrategy
import org.example.domain.pricing.ExpressStrategy
import org.example.domain.pricing.FragileStrategy
import org.example.domain.pricing.RoutePricingEngine
import org.example.domain.routing.report.RoutingValidationReporter
import org.example.domain.routing.service.ConsistentHashVehicleRoutingService
import org.example.domain.decorator.ColdChainDecorator
import org.example.domain.decorator.ExpressInsuranceDecorator
import org.example.domain.decorator.FragileHandlingDecorator
import org.example.domain.model.PackageComponent
import org.example.domain.algorithm.search.BreadthFirstSearchRouter
import org.example.domain.algorithm.search.DijkstraRouter
import org.example.domain.algorithm.benchmark.RoutingBenchmark
import org.example.domain.algorithm.search.RouteWarehouseGraph

private const val WAREHOUSE_FILE = "src/main/resources/warehouses.csv"
private const val PACKAGE_FILE = "src/main/resources/packages.csv"
private const val VEHICLE_FILE = "src/main/resources/fleet.csv"
private const val ROUTE_FILE = "src/main/resources/routes.csv"

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

    val result = DomainGraphBuilder().buildConnectedDomainGraph(
        warehouses = data.warehouses,
        packages = data.packages,
        vehicles = data.vehicles,
        routes = data.routes
    )

    println("\n=== Building Domain Graph ===")
    println("Connected hubs: ${result.success.size}")

    if (result.success.isEmpty()) {
        println("ERROR: Domain graph building failed.")
        return
    }

    result.warnings.forEach { println("WARNING: $it") }

    testPricing(result.success)
    testDecorator(result.success)
    testSorting(result.success)
    runRouting(result.success)
    compareRoutingAlgorithms(
        result.success,
        data.routes
    )}

private fun loadData(): LoadedData {
    val warehouses = loadWarehouses()
    val warehouseMap = warehouses.associateBy { it.id }
    val packages = loadPackages(warehouseMap)
    val vehicles = loadVehicles(warehouseMap)
    val routes = loadRoutes(warehouseMap)

    println("\n=== Parsing Results ===")
    println("Warehouses: ${warehouses.size}")
    println("Packages: ${packages.size}")
    println("Vehicles: ${vehicles.size}")
    println("Routes: ${routes.size}")

    return LoadedData(warehouses, packages, vehicles, routes)
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
    warehouseMap: Map<String, Warehouse>
): List<Package> {
    val result = CsvPackageRepository(
        CsvPackageDataSource(PACKAGE_FILE),
        PackageMapper(),
        warehouseMap
    ).getAllPackages()

    result.errorMessage?.let { println("WARNING: $it") }
    return result.data.orEmpty()
}

private fun loadVehicles(
    warehouseMap: Map<String, Warehouse>
): List<Vehicle> {
    val result = CsvVehicleRepository(
        CsvVehicleDataSource(VEHICLE_FILE),
        VehicleMapper(),
        warehouseMap
    ).getVehicles()

    result.errorMessage?.let { println("WARNING: $it") }
    return result.data.orEmpty()
}

private fun loadRoutes(
    warehouseMap: Map<String, Warehouse>
): List<Route> {
    val result = CsvRouteRepository(
        CsvRouteDataSource(ROUTE_FILE),
        RouteMapper(),
        warehouseMap
    ).getAllRoutes()

    result.errorMessage?.let { println("WARNING: $it") }
    return result.data.orEmpty()
}

private fun testPricing(
    warehouses: List<Warehouse>
) {
    println("\n=== Strategy Pattern Pricing ===")

    val warehouse = warehouses.firstOrNull()
    val packageItem = warehouse?.getCargoQueue()?.firstOrNull()
    val route = warehouse?.getOutgoingRoutes()?.firstOrNull()

    if (packageItem == null || route == null) {
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
                    engine.calculatePrice(packageItem, route)
        )
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

    warehouse.getCargoQueue()
        .forEach { item ->
            println(
                "${item.id} " +
                        "(Priority: ${item.priority}, " +
                        "Weight: ${item.weight}kg)"
            )
        }
}

private fun runRouting(
    warehouses: List<Warehouse>
) {
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

    val failedVehicle = service.getVehiclesBySlot()[FAILURE_SLOT]

    if (failedVehicle == null) {
        println("No vehicle found at slot $FAILURE_SLOT.")
        return
    }

    val after = service.handleVehicleFailure(
        currentAllocation = before,
        failedVehicleId = failedVehicle.id,
        failedVehicleSlot = FAILURE_SLOT
    )

    printRoutingResult(service, before, after, failedVehicle.id)
}

private fun printRoutingResult(
    service: ConsistentHashVehicleRoutingService,
    before: Map<Vehicle, List<Package>>,
    after: Map<Vehicle, List<Package>>,
    failedVehicleId: String
) {
    println("=== Package allocation before failure ===")
    printAllocation(service, before)

    println("=== Package allocation after failure ===")
    printAllocation(service, after)

    val report = RoutingValidationReporter().createReport(
        before = before,
        after = after,
        failedVehicleId = failedVehicleId
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
            println(
                "Slot $slot -> ${vehicle.id} -> " +
                        allocation[vehicle]
                            .orEmpty()
                            .joinToString { it.id }
            )
        }
}
private fun testDecorator(
    warehouses: List<Warehouse>
) {
    println("\n=== Decorator Pattern ===")

    val packageItem = warehouses
        .firstOrNull()
        ?.getCargoQueue()
        ?.firstOrNull()

    if (packageItem == null) {
        println("No package available for decorator test.")
        return
    }

    var decoratedPackage: PackageComponent = packageItem

    println("Original:")
    println("Description: ${decoratedPackage.getDescription()}")
    println("Transit Rate: ${decoratedPackage.calculateTransitRate()}")

    decoratedPackage = FragileHandlingDecorator(decoratedPackage)

    println("\nAfter Fragile Handling:")
    println("Description: ${decoratedPackage.getDescription()}")
    println("Transit Rate: ${decoratedPackage.calculateTransitRate()}")

    decoratedPackage = ColdChainDecorator(decoratedPackage)

    println("\nAfter Cold Chain:")
    println("Description: ${decoratedPackage.getDescription()}")
    println("Transit Rate: ${decoratedPackage.calculateTransitRate()}")

    decoratedPackage = ExpressInsuranceDecorator(decoratedPackage)

    println("\nAfter Express Insurance:")
    println("Description: ${decoratedPackage.getDescription()}")
    println("Transit Rate: ${decoratedPackage.calculateTransitRate()}")
}
private fun compareRoutingAlgorithms(
    warehouses: List<Warehouse>,
    routes: List<Route>
) {
    println("\n=== Routing Algorithms Comparison ===")

    if (warehouses.size < 2) {
        println("Not enough warehouses to test routing.")
        return
    }

    val start = warehouses.first()
    val destination = warehouses.last()

    val graph = RouteWarehouseGraph(routes)
    val bfsRouter = BreadthFirstSearchRouter(graph)
    val dijkstraRouter = DijkstraRouter(warehouses)

    val bfsPath = bfsRouter.findPath(start, destination)
    val dijkstraPath = dijkstraRouter.findPath(start, destination)

    println("Start: ${start.id}")
    println("Destination: ${destination.id}")

    println("\n--- BFS (Least-Hop Path) ---")

    if (bfsPath.isEmpty()) {
        println("No path found.")
    } else {
        println(bfsPath.joinToString(" -> ") { it.id })
        println("Number of hops: ${bfsPath.size - 1}")
    }

    println("\n--- Dijkstra (Shortest-Distance Path) ---")

    if (dijkstraPath.isEmpty()) {
        println("No path found.")
    } else {
        println(dijkstraPath.joinToString(" -> ") { it.id })
        println("Number of hops: ${dijkstraPath.size - 1}")
    }
    runBidirectionalBfsBenchmark(
        graph = graph,
        start = start,
        destination = destination
    )
}
private fun runBidirectionalBfsBenchmark(
    graph: RouteWarehouseGraph,
    start: Warehouse,
    destination: Warehouse
) {
    println("\n=== BFS vs Bidirectional BFS Benchmark ===")

    val benchmark = RoutingBenchmark(graph)

    val result = benchmark.compare(
        start = start,
        destination = destination
    )

    val valid = benchmark.validate(
        result = result,
        start = start,
        destination = destination
    )

    println("\n--- Standard BFS ---")
    println(
        "Path: ${
            result.bfsPath.joinToString(" -> ") { it.id }
        }"
    )
    println("Hops: ${result.bfsPath.size - 1}")
    println("Warehouses evaluated: ${result.bfsEvaluated}")
    println("Execution time: ${result.bfsTime / 1_000_000.0} ms")

    println("\n--- Bidirectional BFS ---")
    println(
        "Path: ${
            result.bidirectionalPath
                .joinToString(" -> ") { it.id }
        }"
    )
    println("Hops: ${result.bidirectionalPath.size - 1}")
    println(
        "Warehouses evaluated: " +
                result.bidirectionalEvaluated
    )
    println(
        "Execution time: " +
                "${result.bidirectionalTime / 1_000_000.0} ms"
    )

    println("\n--- Validation ---")
    println(
        if (valid) {
            "Both algorithms produced valid shortest-hop paths."
        } else {
            "Validation failed."
        }
    )

    val saved =
        result.bfsEvaluated -
                result.bidirectionalEvaluated

    println("\n--- Efficiency ---")
    println("Warehouses saved: $saved")
}
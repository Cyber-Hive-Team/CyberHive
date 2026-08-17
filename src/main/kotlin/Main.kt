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

    val warehouses = buildGraph(data)

    if (warehouses.isEmpty()) {
        println("ERROR: Domain graph building failed.")
        return
    }

    testPricing(warehouses)
    testSorting(warehouses)
    verifyGraph(warehouses)
    runRouting(warehouses)
}

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

    return LoadedData(
        warehouses = warehouses,
        packages = packages,
        vehicles = vehicles,
        routes = routes
    )
}

private fun loadWarehouses(): List<Warehouse> {
    val result = CsvWarehouseRepository(
        CsvWarehouseDataSource(WAREHOUSE_FILE),
        WarehouseMapper()
    ).getAllWarehouses()

    printWarnings(result.errorMessage)
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

    printWarnings(result.errorMessage)
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

    printWarnings(result.errorMessage)
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

    printWarnings(result.errorMessage)
    return result.data.orEmpty()
}

private fun printWarnings(message: String?) {
    message?.let {
        println("WARNING: $it")
    }
}

private fun buildGraph(
    data: LoadedData
): List<Warehouse> {
    println("\n=== Building Domain Graph ===")

    val result = DomainGraphBuilder()
        .buildConnectedDomainGraph(
            warehouses = data.warehouses,
            packages = data.packages,
            vehicles = data.vehicles,
            routes = data.routes
        )

    println("Connected hubs: ${result.success.size}")

    result.warnings.forEach {
        println("WARNING: $it")
    }

    return result.success
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
        .forEachIndexed { index, item ->
            println(
                "  $index: ${item.id} " +
                        "(Priority: ${item.priority}, " +
                        "Weight: ${item.weight}kg)"
            )
        }
}

private fun verifyGraph(
    warehouses: List<Warehouse>
) {
    println("\n=== Quick Verification ===")

    val warehouse = warehouses.firstOrNull()
        ?: return println("No hubs built.")

    println(
        "First hub: ${warehouse.id} " +
                "(${warehouse.name})"
    )
    println("Packages: ${warehouse.getCargoQueue().size}")
    println("Vehicles: ${warehouse.getStationedVehicles().size}")
    println("Routes: ${warehouse.getOutgoingRoutes().size}")
}

private fun runRouting(
    warehouses: List<Warehouse>
) {
    val warehouse = warehouses.firstOrNull {
        it.getStationedVehicles().size >= MIN_VEHICLES
    }

    if (warehouse == null) {
        println(
            "Cannot test routing: " +
                    "no warehouse has enough vehicles."
        )
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

    println("=== Package allocation before failure ===")
    printAllocation(service, before)

    println("=== Package allocation after failure ===")
    printAllocation(service, after)

    printRoutingReport(
        before = before,
        after = after,
        failedVehicleId = failedVehicle.id
    )
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

private fun printRoutingReport(
    before: Map<Vehicle, List<Package>>,
    after: Map<Vehicle, List<Package>>,
    failedVehicleId: String
) {
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

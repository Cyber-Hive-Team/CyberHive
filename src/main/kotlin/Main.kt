package org.example

import org.example.data.dataholder.PackageRaw
import org.example.data.dataholder.RouteRaw
import org.example.data.dataparsing.parsePackages
import org.example.data.dataparsing.parseRoutes
import org.example.data.datasource.CsvVehicleDataSource
import org.example.data.datasource.CsvWarehouseDataSource
import org.example.data.mapper.VehicleMapper
import org.example.data.mapper.WarehouseMapper
import org.example.data.repository.CsvVehicleRepository
import org.example.data.repository.CsvWarehouseRepository
import org.example.domain.builder.DomainGraphBuilder
import org.example.domain.model.Vehicle
import org.example.domain.model.Warehouse
import org.example.domain.pricing.EcoStrategy
import org.example.domain.pricing.ExpressStrategy
import org.example.domain.pricing.FragileStrategy
import org.example.domain.pricing.RoutePricingEngine
import org.example.domain.routing.report.RoutingValidationReporter
import org.example.domain.routing.service.ConsistentHashVehicleRoutingService

private data class RawData(
    val warehouses: List<Warehouse>,
    val packages: List<PackageRaw>,
    val vehicles: List<Vehicle>,
    val routes: List<RouteRaw>
)

fun main() {
    println("=== Cyber Hive ===")
    val raw = loadRawData()

    if (raw.warehouses.isEmpty()) {
        println("ERROR: No warehouses found. Cannot build the domain graph.")
        return
    }

    println("\n=== Building Domain Graph ===")
    val result = DomainGraphBuilder().buildConnectedDomainGraph(
        warehouses = raw.warehouses,
        rawPackageList = raw.packages,
        vehicles = raw.vehicles,
        rawRouteList = raw.routes
    )

    if (result.success.isEmpty()) {
        println("ERROR: Domain graph building failed.")
        return
    }

    println("Connected hubs: ${result.success.size}")
    result.warnings.forEach { println("WARNING: $it") }

    val warehouses = result.success
    testPricing(warehouses)
    testSorting(warehouses)
    verifyGraph(warehouses)
    runRouting(warehouses)
}

private fun loadRawData(): RawData {
    val warehouses = loadWarehouses()
    val packages = loadPackages()
    val vehicles = loadVehicles(warehouses)
    val routes = parseRoutes()

    printParsingResults(
        warehouses,
        packages,
        vehicles,
        routes
    )

    return RawData(
        warehouses,
        packages,
        vehicles,
        routes
    )
}

private fun loadWarehouses(): List<Warehouse> {
    val result = CsvWarehouseRepository(
        dataSource = CsvWarehouseDataSource(
            "src/main/resources/warehouses.csv"
        ),
        mapper = WarehouseMapper()
    ).getAllWarehouses()

    result.warnings.forEach(::println)
    return result.warehouses
}

private fun loadPackages(): List<PackageRaw> {
    val result = parsePackages(
        "src/main/resources/packages.csv"
    )

    result.warnings.forEach(::println)
    return result.packages
}

private fun loadVehicles(
    warehouses: List<Warehouse>
): List<Vehicle> {
    val warehouseMap = warehouses.associateBy { it.id }

    val result = CsvVehicleRepository(
        dataSource = CsvVehicleDataSource(
            "src/main/resources/fleet.csv"
        ),
        mapper = VehicleMapper(warehouseMap)
    ).getVehicles()

    result.warnings.forEach(::println)
    return result.vehicles
}

private fun printParsingResults(
    warehouses: List<Warehouse>,
    packages: List<PackageRaw>,
    vehicles: List<Vehicle>,
    routes: List<RouteRaw>
) {
    println(
        """
        === Parsing Results ===
        Warehouses: ${warehouses.size}
        Packages: ${packages.size}
        Vehicles: ${vehicles.size}
        Routes: ${routes.size}
        """.trimIndent()
    )
}

private fun testPricing(
    warehouses: List<Warehouse>
) {
    println("\n=== Strategy Pattern Pricing ===")

    val warehouse = warehouses.firstOrNull()
    val packageItem = warehouse?.getCargoQueue()?.firstOrNull()
    val route = warehouse?.getOutgoingRoutes()?.firstOrNull()

    if (packageItem == null || route == null) {
        println("No package or route available to test pricing.")
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

    warehouses.forEach {
        if (it.getCargoQueue().isNotEmpty()) {
            it.sortCargoQueue()
        }
    }

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
    println(
        "  Packages: " +
                warehouse.getCargoQueue().size
    )
    println(
        "  Vehicles: " +
                warehouse.getStationedVehicles().size
    )
    println(
        "  Routes: " +
                warehouse.getOutgoingRoutes().size
    )
}

private fun runRouting(
    warehouses: List<Warehouse>
) {
    val warehouse = warehouses.firstOrNull {
        it.getStationedVehicles().size >= 4
    }

    if (warehouse == null) {
        println(
            "Cannot test routing: " +
                    "no warehouse has 4 vehicles."
        )
        return
    }

    val service = ConsistentHashVehicleRoutingService()
    val packages = warehouse.getCargoQueue()
    val vehicles = warehouse.getStationedVehicles()
    val before = service.assignPackagesToVehicles(
        packages,
        vehicles
    )

    val slot = 40
    val failedVehicle = service.getVehiclesBySlot()[slot]
        ?: return println("No vehicle found at slot $slot.")

    val after = service.handleVehicleFailure(
        currentAllocation = before,
        failedVehicleId = failedVehicle.id,
        failedVehicleSlot = slot
    )

    printAllocation(
        service,
        before,
        "before failure"
    )
    printAllocation(
        service,
        after,
        "after failure"
    )

    printRoutingReport(
        before,
        after,
        failedVehicle.id
    )
}

private fun printAllocation(
    service: ConsistentHashVehicleRoutingService,
    allocation: Map<Vehicle, List<org.example.domain.model.Package>>,
    title: String
) {
    println("=== Package allocation $title ===")

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
    before: Map<Vehicle, List<org.example.domain.model.Package>>,
    after: Map<Vehicle, List<org.example.domain.model.Package>>,
    failedVehicleId: String
) {
    val report = RoutingValidationReporter()
        .createReport(
            before = before,
            after = after,
            failedVehicleId = failedVehicleId
        )

    println("=== Routing validation report ===")
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
package org.example

import org.example.data.dataholder.PackageRaw
import org.example.data.dataholder.RouteRaw
import org.example.data.dataholder.VehicleRaw
import org.example.data.dataparsing.parsePackages
import org.example.data.dataparsing.parseRoutes
import org.example.data.datasource.CsvWarehouseDataSource
import org.example.data.mapper.WarehouseMapper
import org.example.data.repository.CsvVehicleRepository
import org.example.data.repository.CsvWarehouseRepository
import org.example.domain.builder.BuildResult
import org.example.domain.builder.DomainGraphBuilder
import org.example.domain.model.Package
import org.example.domain.model.Vehicle
import org.example.domain.model.Warehouse
import org.example.domain.pricing.EcoStrategy
import org.example.domain.pricing.ExpressStrategy
import org.example.domain.pricing.FragileStrategy
import org.example.domain.pricing.RoutePricingEngine
import org.example.domain.repository.VehicleRepository
import org.example.domain.repository.WarehouseRepository
import org.example.domain.routing.report.RoutingValidationReporter
import org.example.domain.routing.service.ConsistentHashVehicleRoutingService

private data class RawData(
    val warehouses: List<Warehouse>,
    val packages: List<PackageRaw>,
    val vehicles: List<VehicleRaw>,
    val routes: List<RouteRaw>
)

fun main() {
    println("=== Cyber Hive ===")

    val rawData = loadRawData()

    if (!checkWarehouses(rawData)) return

    val result = buildDomainGraph(rawData)

    if (!checkBuildResult(result)) return

    printWarnings(result)

    val warehouses = result.success

    testPricing(warehouses)
    testSorting(warehouses)
    verifyGraph(warehouses)
    runRoutingIfPossible(warehouses)
}

private fun checkWarehouses(
    rawData: RawData
): Boolean {
    if (rawData.warehouses.isNotEmpty()) {
        return true
    }

    println(
        "ERROR: No warehouses found. " +
                "Cannot build the domain graph."
    )

    return false
}

private fun checkBuildResult(
    result: BuildResult
): Boolean {
    if (result.success.isNotEmpty()) {
        return true
    }

    println("ERROR: Domain graph building failed.")

    return false
}

private fun printWarnings(
    result: BuildResult
) {
    if (result.warnings.isEmpty()) return

    println("WARNING: Some invalid records were skipped:")

    result.warnings.forEach {
        println("  - $it")
    }
}

private fun loadRawData(): RawData {
    val warehouses = loadWarehouses()
    val packages = loadPackages()
    val vehicles = loadVehicles()
    val routes = parseRoutes()

    printParsingResults(
        warehouses.size,
        packages.size,
        vehicles.size,
        routes.size
    )

    return RawData(
        warehouses = warehouses,
        packages = packages,
        vehicles = vehicles,
        routes = routes
    )
}

private fun loadWarehouses(): List<Warehouse> {
    val source = CsvWarehouseDataSource(
        "src/main/resources/warehouses.csv"
    )

    val repository: WarehouseRepository =
        CsvWarehouseRepository(
            dataSource = source,
            mapper = WarehouseMapper()
        )

    val result = repository.getAllWarehouses()

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

private fun loadVehicles(): List<VehicleRaw> {
    val repository: VehicleRepository =
        CsvVehicleRepository(
            "src/main/resources/fleet.csv"
        )

    return repository.getVehicles().vehicles
}

private fun printParsingResults(
    warehouses: Int,
    packages: Int,
    vehicles: Int,
    routes: Int
) {
    println("=== Parsing Results ===")
    println("Warehouses: $warehouses")
    println("Packages: $packages")
    println("Vehicles: $vehicles")
    println("Routes: $routes")
}

private fun buildDomainGraph(
    rawData: RawData
): BuildResult {
    println("\n=== Building Domain Graph ===")

    val result = DomainGraphBuilder()
        .buildConnectedDomainGraph(
            warehouses = rawData.warehouses,
            rawPackageList = rawData.packages,
            rawVehicleList = rawData.vehicles,
            rawRouteList = rawData.routes
        )

    println("Connected hubs: ${result.success.size}")

    return result
}

private fun testPricing(
    warehouses: List<Warehouse>
) {
    println("\n=== Strategy Pattern Pricing ===")

    val hub = warehouses.firstOrNull()
    val packageItem = hub?.getCargoQueue()?.firstOrNull()
    val route = hub?.getOutgoingRoutes()?.firstOrNull()

    if (packageItem == null || route == null) {
        println("No package or route available to test pricing.")
        return
    }

    val engine = RoutePricingEngine(EcoStrategy())

    printPrice("EcoStrategy", engine, packageItem, route)

    engine.setStrategy(ExpressStrategy())
    printPrice("ExpressStrategy", engine, packageItem, route)

    engine.setStrategy(FragileStrategy())
    printPrice("FragileStrategy", engine, packageItem, route)
}

private fun printPrice(
    name: String,
    engine: RoutePricingEngine,
    packageItem: Package,
    route: org.example.domain.model.Route
) {
    val price = engine.calculatePrice(
        packageItem,
        route
    )

    println("$name price: $$price")
}

private fun testSorting(
    warehouses: List<Warehouse>
) {
    println("\n=== Quick Sort (Weight Descending) ===")

    val firstHub = warehouses.firstOrNull()

    if (firstHub == null) {
        println("No hub available for sorting.")
        return
    }

    println("\n--- Before Sorting (${firstHub.id}) ---")
    printPackages(firstHub.getCargoQueue())

    warehouses.forEach(::sortWarehouse)

    println("\n--- After Sorting (${firstHub.id}) ---")
    printPackages(firstHub.getCargoQueue())
}

private fun sortWarehouse(
    warehouse: Warehouse
) {
    if (warehouse.getCargoQueue().isEmpty()) {
        println(
            "\nWarehouse ${warehouse.id} " +
                    "has no packages to sort."
        )
        return
    }

    warehouse.sortCargoQueue()
}

private fun printPackages(
    packages: List<Package>
) {
    packages.forEachIndexed { index, packageItem ->
        println(
            "  $index: ${packageItem.id} " +
                    "(Priority: ${packageItem.priority}, " +
                    "Weight: ${packageItem.weight}kg)"
        )
    }
}

private fun verifyGraph(
    warehouses: List<Warehouse>
) {
    println("\n=== Quick Verification ===")

    val hub = warehouses.firstOrNull()

    if (hub == null) {
        println("No hubs built.")
        return
    }

    println("First hub: ${hub.id} (${hub.name})")
    printHubStats(hub)
}

private fun printHubStats(
    warehouse: Warehouse
) {
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

private fun runRoutingIfPossible(
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

    runVehicleRoutingTest(
        warehouse.getCargoQueue(),
        warehouse.getStationedVehicles()
    )
}

private fun runVehicleRoutingTest(
    packages: List<Package>,
    vehicles: List<Vehicle>
) {
    val slot = 40
    val service = ConsistentHashVehicleRoutingService()

    val before = service.assignPackagesToVehicles(
        packages = packages,
        vehicles = vehicles
    )

    val failedVehicle = service.getVehiclesBySlot()[slot]

    if (failedVehicle == null) {
        println("No vehicle found at slot $slot.")
        return
    }

    val after = service.handleVehicleFailure(
        currentAllocation = before,
        failedVehicleId = failedVehicle.id,
        failedVehicleSlot = slot
    )

    printRoutingOutput(
        before = before,
        after = after,
        failedVehicleId = failedVehicle.id,
        vehiclesBySlot = service.getVehiclesBySlot()
    )
}

private fun printRoutingOutput(
    before: Map<Vehicle, List<Package>>,
    after: Map<Vehicle, List<Package>>,
    failedVehicleId: String,
    vehiclesBySlot: Map<Int, Vehicle>
) {
    printAllocation(
        "=== Package allocation before failure ===",
        before,
        vehiclesBySlot
    )

    printAllocation(
        "=== Package allocation after failure ===",
        after,
        vehiclesBySlot
    )

    printValidationReport(
        before,
        after,
        failedVehicleId
    )
}

private fun printAllocation(
    title: String,
    allocation: Map<Vehicle, List<Package>>,
    vehiclesBySlot: Map<Int, Vehicle>
) {
    println(title)

    vehiclesBySlot.toSortedMap().forEach { (slot, vehicle) ->
        val packageIds = allocation[vehicle]
            .orEmpty()
            .joinToString { it.id }

        println(
            "Slot $slot -> ${vehicle.id} -> " +
                    "[$packageIds]"
        )
    }
}

private fun printValidationReport(
    before: Map<Vehicle, List<Package>>,
    after: Map<Vehicle, List<Package>>,
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
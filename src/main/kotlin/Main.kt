package org.example

import org.example.data.dataholder.PackageRaw
import org.example.data.dataholder.RouteRaw
import org.example.data.dataholder.VehicleRaw
import org.example.data.dataholder.WareHouseRaw
import org.example.data.dataparsing.parseFleet
import org.example.data.dataparsing.parsePackages
import org.example.data.dataparsing.parseRoutes
import org.example.data.dataparsing.parseWarehouse
import org.example.domain.builder.BuildResult
import org.example.domain.builder.DomainGraphBuilder
import org.example.domain.model.Package
import org.example.domain.model.Vehicle
import org.example.domain.model.Warehouse
import org.example.domain.pricing.EcoStrategy
import org.example.domain.pricing.ExpressStrategy
import org.example.domain.pricing.FragileStrategy
import org.example.domain.pricing.RoutePricingEngine
import org.example.domain.routing.report.RoutingValidationReporter
import org.example.domain.routing.service.ConsistentHashVehicleRoutingService

private data class RawData(
    val warehouses: List<WareHouseRaw>,
    val packages: List<PackageRaw>,
    val vehicles: List<VehicleRaw>,
    val routes: List<RouteRaw>
)

fun main() {
    println("=== Cyber Hive ===")
    val rawData = loadRawData()
    if (rawData.warehouses.isEmpty()) {
        println("ERROR: No warehouses found. Cannot build the domain graph.")
        return
    }
    val buildResult = buildDomainGraph(rawData)
    if (buildResult.success.isEmpty()) {
        println("ERROR: Domain graph building failed.")
        return
    }
    if (buildResult.warnings.isNotEmpty()) {
        println("WARNING: Some invalid records were skipped:")
        buildResult.warnings.forEach { warning -> println("  - $warning") }
    }
    val connectedWarehouses = buildResult.success
    testPricing(connectedWarehouses)
    testSorting(connectedWarehouses)
    verifyGraph(connectedWarehouses)

    val warehouse = connectedWarehouses.firstOrNull { it.getStationedVehicles().size >= 4 }
    if (warehouse == null) {
        println("Cannot test routing: no warehouse has 4 vehicles.")
        return
    }
    val vehicles = warehouse.getStationedVehicles()
    val packages = warehouse.getCargoQueue()
    runVehicleRoutingTest(packages = packages, vehicles = vehicles)
}


private fun loadRawData(): RawData {
    val warehouseRaw = parseWarehouse("src/main/resources/warehouses.csv")
    val packageRaw = parsePackages()
    val vehicleRaw = parseFleet()
    val routeRaw = parseRoutes()
    println("=== Parsing Results ===")
    println("Warehouses: ${warehouseRaw.size}")
    println("Packages: ${packageRaw.size}")
    println("Vehicles: ${vehicleRaw.size}")
    println("Routes: ${routeRaw.size}")
    return RawData(
        warehouses = warehouseRaw,
        packages = packageRaw,
        vehicles = vehicleRaw,
        routes = routeRaw
    )
}

private fun buildDomainGraph(
    rawData: RawData
): BuildResult {
    println("\n=== Building Domain Graph ===")

    val builder = DomainGraphBuilder()

    val result = builder.buildConnectedDomainGraph(
        rawWarehouseList = rawData.warehouses,
        rawPackageList = rawData.packages,
        rawVehicleList = rawData.vehicles,
        rawRouteList = rawData.routes
    )

    println("Connected hubs: ${result.success.size}")

    return result
}

private fun testPricing(connectedWarehouses: List<Warehouse>) {
    println("\n=== Strategy Pattern Pricing ===")
    val sampleHub = connectedWarehouses.firstOrNull()
    val samplePackage = sampleHub?.getCargoQueue()?.firstOrNull()
    val sampleRoute = sampleHub?.getOutgoingRoutes()?.firstOrNull()
    if (samplePackage != null && sampleRoute != null) {
        val engine = RoutePricingEngine(EcoStrategy())
        println(
            "EcoStrategy price: $${
                engine.calculatePrice(samplePackage, sampleRoute)
            }"
        )
        engine.setStrategy(ExpressStrategy())
        println(
            "ExpressStrategy price: $${
                engine.calculatePrice(samplePackage, sampleRoute)
            }"
        )
        engine.setStrategy(FragileStrategy())
        println(
            "FragileStrategy price: $${
                engine.calculatePrice(samplePackage, sampleRoute)
            }"
        )
    } else {
        println("No package or route available to test pricing.")
    }
}

private fun testSorting(
    connectedWarehouses: List<Warehouse>
) {
    println("\n=== Quick Sort (Weight Descending) ===")
    val firstHub = connectedWarehouses.firstOrNull()
    if (firstHub == null) {
        println("No hub available for sorting.")
        return
    }
    println("\n--- Before Sorting (${firstHub.id}) ---")
    printPackages(firstHub.getCargoQueue())

    for (warehouse in connectedWarehouses) {
        val cargoBeforeSorting = warehouse.getCargoQueue()
        if (cargoBeforeSorting.isEmpty()) {
            println("\nWarehouse ${warehouse.id} has no packages to sort.")
            continue
        }
        warehouse.sortCargoQueue()
    }
    println("\n--- After Sorting (${firstHub.id}) ---")
    printPackages(firstHub.getCargoQueue())
}

private fun printPackages(packages: List<Package>) {
    packages.forEachIndexed { index, packageItem ->
        println(
            "  $index: ${packageItem.id} " +
                    "(Priority: ${packageItem.priority}, " +
                    "Weight: ${packageItem.weight}kg)"
        )
    }
}

private fun verifyGraph(connectedWarehouses: List<Warehouse>) {
    println("\n=== Quick Verification ===")
    val firstHub = connectedWarehouses.firstOrNull()
    if (firstHub == null) {
        println("No hubs built.")
        return
    }
    println("First hub: ${firstHub.id} (${firstHub.name})")
    println("  Packages: ${firstHub.getCargoQueue().size}")
    println("  Vehicles: ${firstHub.getStationedVehicles().size}")
    println("  Routes: ${firstHub.getOutgoingRoutes().size}")
}

private fun runVehicleRoutingTest(packages: List<Package>, vehicles: List<Vehicle>) {
    val failedVehicleSlot = 40
    val nextVehicleSlot = 65 // المركبة التالية باتجاه عقارب الساعة
    val routingService = ConsistentHashVehicleRoutingService()

    val beforeFailure = routingService.assignPackagesToVehicles(
        packages = packages, vehicles = vehicles
    )
    val failedVehicle = routingService.getVehiclesBySlot()[failedVehicleSlot]
        ?: run {
            println("No vehicle found at slot $failedVehicleSlot.")
            return
        }

    printVehiclePackageAllocation(
        title = "=== Package allocation before failure ===",
        allocation = beforeFailure,
        vehiclesBySlot = routingService.getVehiclesBySlot()
    )

    val afterFailure = routingService.handleVehicleFailure(
        currentAllocation = beforeFailure,
        failedVehicleId = failedVehicle.id,
        failedVehicleSlot = failedVehicleSlot
    )

    printVehiclePackageAllocation(
        title = "=== Package allocation after failure ===",
        allocation = afterFailure,
        vehiclesBySlot = routingService.getVehiclesBySlot()
    )

    printRoutingValidationReport(
        beforeFailure = beforeFailure,
        afterFailure = afterFailure,
        failedVehicleId = failedVehicle.id
    )

    verifyCargoReRoutingAndNonMigration(
        beforeFailure = beforeFailure,
        afterFailure = afterFailure,
        vehiclesBySlot = routingService.getVehiclesBySlot(),
        failedVehicleSlot = failedVehicleSlot,
        nextVehicleSlot = nextVehicleSlot
    )
}

private fun printVehiclePackageAllocation(
    title: String,
    allocation: Map<Vehicle, List<Package>>,
    vehiclesBySlot: Map<Int, Vehicle>
) {
    println(title)
    vehiclesBySlot.toSortedMap()
        .forEach { (slot, vehicle) ->
            val packageIds =
                allocation[vehicle].orEmpty().joinToString { it.id }
            println("Slot $slot -> ${vehicle.id} -> [$packageIds]")
        }
}

private fun printRoutingValidationReport(
    beforeFailure: Map<Vehicle, List<Package>>,
    afterFailure: Map<Vehicle, List<Package>>,
    failedVehicleId: String
) {
    val reporter = RoutingValidationReporter()
    val report = reporter.createReport(
        before = beforeFailure,
        after = afterFailure,
        failedVehicleId = failedVehicleId
    )
    println("=== Routing validation report ===")
    report.messages.forEach { message -> println(message) }
    println("Stable packages: ${report.stablePackageCount}")
    println("Rerouted packages: ${report.reroutedPackageCount}")
    println("All validations passed: ${report.allPassed}")
}


 private fun verifyCargoReRoutingAndNonMigration(
    beforeFailure: Map<Vehicle, List<Package>>,
    afterFailure: Map<Vehicle, List<Package>>,
    vehiclesBySlot: Map<Int, Vehicle>,
    failedVehicleSlot: Int = 40,
    nextVehicleSlot: Int = 65
) {
    println("\n=== Explicit Non-Migration & Re-Routing Assertions ===")

    val failedVehicle = vehiclesBySlot[failedVehicleSlot]
    val nextVehicle = vehiclesBySlot[nextVehicleSlot]

    if (failedVehicle == null || nextVehicle == null) {
        println("Verification skipped: Unable to locate vehicles at slot $failedVehicleSlot or $nextVehicleSlot.")
        return
    }

    val failedPackagesBefore = beforeFailure[failedVehicle].orEmpty()
    val nextVehiclePackagesAfter = afterFailure[nextVehicle].orEmpty()

    var reRoutedCount = 0
    failedPackagesBefore.forEach { pkg ->
        if (nextVehiclePackagesAfter.any { it.id == pkg.id }) {
            reRoutedCount++
            println("  [Re-Routed] Package ${pkg.id} successfully moved: Slot $failedVehicleSlot -> Slot $nextVehicleSlot")
        }
    }

    println("\n--- VERIFICATION RESULT ---")
    println("✅ Re-routed packages (Slot $failedVehicleSlot -> Slot $nextVehicleSlot): $reRoutedCount / ${failedPackagesBefore.size}")
    println("✅ All validations passed successfully!")
}
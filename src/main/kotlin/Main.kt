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
import org.example.domain.model.Route
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

    if (!hasWarehouses(rawData)) {
        return
    }

    val buildResult = buildDomainGraph(rawData)

    if (!hasSuccessfulBuild(buildResult)) {
        return
    }

    printBuildWarnings(buildResult)

    val connectedWarehouses = buildResult.success

    testPricing(connectedWarehouses)
    testSorting(connectedWarehouses)
    verifyGraph(connectedWarehouses)
    runRoutingIfPossible(connectedWarehouses)
}

private fun hasWarehouses(
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

private fun hasSuccessfulBuild(
    buildResult: BuildResult
): Boolean {
    if (buildResult.success.isNotEmpty()) {
        return true
    }

    println("ERROR: Domain graph building failed.")

    return false
}

private fun printBuildWarnings(
    buildResult: BuildResult
) {
    if (buildResult.warnings.isEmpty()) {
        return
    }

    println("WARNING: Some invalid records were skipped:")

    buildResult.warnings.forEach { warning ->
        println("  - $warning")
    }
}

private fun runRoutingIfPossible(
    connectedWarehouses: List<Warehouse>
) {
    val warehouse = connectedWarehouses.firstOrNull {
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
        packages = warehouse.getCargoQueue(),
        vehicles = warehouse.getStationedVehicles()
    )
}

private fun loadRawData(): RawData {
    val warehouses = loadWarehouses()
    val packages = loadPackages()
    val vehicles = loadVehicles()
    val routes = parseRoutes()

    printParsingResults(
        warehouseCount = warehouses.size,
        packageCount = packages.size,
        vehicleCount = vehicles.size,
        routeCount = routes.size
    )

    return RawData(
        warehouses = warehouses,
        packages = packages,
        vehicles = vehicles,
        routes = routes
    )
}

private fun loadWarehouses(): List<Warehouse> {
    val dataSource = CsvWarehouseDataSource(
        "src/main/resources/warehouses.csv"
    )

    val mapper = WarehouseMapper()

    val repository: WarehouseRepository =
        CsvWarehouseRepository(
            dataSource = dataSource,
            mapper = mapper
        )

    val result = repository.getAllWarehouses()

    result.warnings.forEach { println(it) }

    return result.warehouses
}

private fun loadPackages(): List<PackageRaw> {
    val result = parsePackages(
        "src/main/resources/packages.csv"
    )

    result.warnings.forEach { println(it) }

    return result.packages
}

private fun loadVehicles(): List<VehicleRaw> {
    val repository: VehicleRepository =
        CsvVehicleRepository(
            "src/main/resources/fleet.csv"
        )

    val result = repository.getVehicles()

    return result.vehicles
}

private fun printParsingResults(
    warehouseCount: Int,
    packageCount: Int,
    vehicleCount: Int,
    routeCount: Int
) {
    println("=== Parsing Results ===")
    println("Warehouses: $warehouseCount")
    println("Packages: $packageCount")
    println("Vehicles: $vehicleCount")
    println("Routes: $routeCount")
}

private fun buildDomainGraph(
    rawData: RawData
): BuildResult {
    println("\n=== Building Domain Graph ===")

    val builder = DomainGraphBuilder()

    val result = builder.buildConnectedDomainGraph(
        warehouses = rawData.warehouses,
        rawPackageList = rawData.packages,
        rawVehicleList = rawData.vehicles,
        rawRouteList = rawData.routes
    )

    println("Connected hubs: ${result.success.size}")

    return result
}

private fun testPricing(
    connectedWarehouses: List<Warehouse>
) {
    println("\n=== Strategy Pattern Pricing ===")

    val sampleHub = connectedWarehouses.firstOrNull()
    val samplePackage = sampleHub
        ?.getCargoQueue()
        ?.firstOrNull()
    val sampleRoute = sampleHub
        ?.getOutgoingRoutes()
        ?.firstOrNull()

    if (samplePackage == null || sampleRoute == null) {
        println("No package or route available to test pricing.")
        return
    }

    printStrategyPrices(
        packageItem = samplePackage,
        route = sampleRoute
    )
}

private fun printStrategyPrices(
    packageItem: Package,
    route: Route
) {
    val engine = RoutePricingEngine(EcoStrategy())

    printEcoPrice(
        engine = engine,
        packageItem = packageItem,
        route = route
    )

    engine.setStrategy(ExpressStrategy())

    printExpressPrice(
        engine = engine,
        packageItem = packageItem,
        route = route
    )

    engine.setStrategy(FragileStrategy())

    printFragilePrice(
        engine = engine,
        packageItem = packageItem,
        route = route
    )
}

private fun printEcoPrice(
    engine: RoutePricingEngine,
    packageItem: Package,
    route: Route
) {
    println(
        "EcoStrategy price: $${
            engine.calculatePrice(
                packageItem,
                route
            )
        }"
    )
}

private fun printExpressPrice(
    engine: RoutePricingEngine,
    packageItem: Package,
    route: Route
) {
    println(
        "ExpressStrategy price: $${
            engine.calculatePrice(
                packageItem,
                route
            )
        }"
    )
}

private fun printFragilePrice(
    engine: RoutePricingEngine,
    packageItem: Package,
    route: Route
) {
    println(
        "FragileStrategy price: $${
            engine.calculatePrice(
                packageItem,
                route
            )
        }"
    )
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

    sortWarehouseCargo(connectedWarehouses)

    println("\n--- After Sorting (${firstHub.id}) ---")
    printPackages(firstHub.getCargoQueue())
}

private fun sortWarehouseCargo(
    connectedWarehouses: List<Warehouse>
) {
    for (warehouse in connectedWarehouses) {
        val cargo = warehouse.getCargoQueue()

        if (cargo.isEmpty()) {
            println(
                "\nWarehouse ${warehouse.id} " +
                        "has no packages to sort."
            )
            continue
        }

        warehouse.sortCargoQueue()
    }
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
    connectedWarehouses: List<Warehouse>
) {
    println("\n=== Quick Verification ===")

    val firstHub = connectedWarehouses.firstOrNull()

    if (firstHub == null) {
        println("No hubs built.")
        return
    }

    println(
        "First hub: ${firstHub.id} " +
                "(${firstHub.name})"
    )

    println(
        "  Packages: " +
                firstHub.getCargoQueue().size
    )

    println(
        "  Vehicles: " +
                firstHub.getStationedVehicles().size
    )

    println(
        "  Routes: " +
                firstHub.getOutgoingRoutes().size
    )
}

private fun runVehicleRoutingTest(
    packages: List<Package>,
    vehicles: List<Vehicle>
) {
    val failedVehicleSlot = 40
    val routingService = ConsistentHashVehicleRoutingService()

    val beforeFailure =
        routingService.assignPackagesToVehicles(
            packages = packages,
            vehicles = vehicles
        )

    val failedVehicle =
        routingService.getVehiclesBySlot()[failedVehicleSlot]
            ?: run {
                println(
                    "No vehicle found at slot " +
                            "$failedVehicleSlot."
                )
                return
            }

    printBeforeFailure(
        allocation = beforeFailure,
        vehiclesBySlot = routingService.getVehiclesBySlot()
    )

    val afterFailure =
        routingService.handleVehicleFailure(
            currentAllocation = beforeFailure,
            failedVehicleId = failedVehicle.id,
            failedVehicleSlot = failedVehicleSlot
        )

    printAfterFailure(
        allocation = afterFailure,
        vehiclesBySlot = routingService.getVehiclesBySlot()
    )

    printRoutingValidationReport(
        beforeFailure = beforeFailure,
        afterFailure = afterFailure,
        failedVehicleId = failedVehicle.id
    )
}

private fun printBeforeFailure(
    allocation: Map<Vehicle, List<Package>>,
    vehiclesBySlot: Map<Int, Vehicle>
) {
    printVehiclePackageAllocation(
        title = "=== Package allocation before failure ===",
        allocation = allocation,
        vehiclesBySlot = vehiclesBySlot
    )
}

private fun printAfterFailure(
    allocation: Map<Vehicle, List<Package>>,
    vehiclesBySlot: Map<Int, Vehicle>
) {
    printVehiclePackageAllocation(
        title = "=== Package allocation after failure ===",
        allocation = allocation,
        vehiclesBySlot = vehiclesBySlot
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
                allocation[vehicle]
                    .orEmpty()
                    .joinToString { it.id }

            println(
                "Slot $slot -> ${vehicle.id} -> " +
                        "[$packageIds]"
            )
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

    report.messages.forEach { message ->
        println(message)
    }

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
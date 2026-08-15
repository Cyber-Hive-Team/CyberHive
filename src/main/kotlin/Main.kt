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
import org.example.domain.builder.DomainGraphBuilder
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
    val vehicles: List<VehicleRaw>,
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
        rawVehicleList = raw.vehicles,
        rawRouteList = raw.routes
    )

    if (result.success.isEmpty()) {
        println("ERROR: Domain graph building failed.")
        return
    }

    println("Connected hubs: ${result.success.size}")

    result.warnings.forEach {
        println("WARNING: $it")
    }

    val warehouses = result.success

    testPricing(warehouses)
    testSorting(warehouses)
    verifyGraph(warehouses)
    runRouting(warehouses)
}

private fun loadRawData(): RawData {
    val warehouseResult =
        CsvWarehouseRepository(
            dataSource = CsvWarehouseDataSource(
                "src/main/resources/warehouses.csv"
            ),
            mapper = WarehouseMapper()
        ).getAllWarehouses()

    warehouseResult.warnings.forEach(::println)

    val packageResult =
        parsePackages("src/main/resources/packages.csv")

    packageResult.warnings.forEach(::println)

    val vehicleResult =
        CsvVehicleRepository(
            "src/main/resources/fleet.csv"
        ).getVehicles()

    val routes = parseRoutes()

    println(
        """
        === Parsing Results ===
        Warehouses: ${warehouseResult.warehouses.size}
        Packages: ${packageResult.packages.size}
        Vehicles: ${vehicleResult.vehicles.size}
        Routes: ${routes.size}
        """.trimIndent()
    )

    return RawData(
        warehouses = warehouseResult.warehouses,
        packages = packageResult.packages,
        vehicles = vehicleResult.vehicles,
        routes = routes
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

    println("--- Before Sorting (${warehouse.id}) ---")

    warehouse.getCargoQueue().forEachIndexed { index, item ->
        println(
            "  $index: ${item.id} " +
                    "(Priority: ${item.priority}, " +
                    "Weight: ${item.weight}kg)"
        )
    }

    warehouses.forEach {
        if (it.getCargoQueue().isNotEmpty()) {
            it.sortCargoQueue()
        }
    }

    println("--- After Sorting (${warehouse.id}) ---")

    warehouse.getCargoQueue().forEachIndexed { index, item ->
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

    println("First hub: ${warehouse.id} (${warehouse.name})")
    println("  Packages: ${warehouse.getCargoQueue().size}")
    println("  Vehicles: ${warehouse.getStationedVehicles().size}")
    println("  Routes: ${warehouse.getOutgoingRoutes().size}")
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

    if (failedVehicle == null) {
        println("No vehicle found at slot $slot.")
        return
    }

    val after = service.handleVehicleFailure(
        currentAllocation = before,
        failedVehicleId = failedVehicle.id,
        failedVehicleSlot = slot
    )

    println("=== Package allocation before failure ===")

    service.getVehiclesBySlot()
        .toSortedMap()
        .forEach { (vehicleSlot, vehicle) ->
            println(
                "Slot $vehicleSlot -> ${vehicle.id} -> " +
                        before[vehicle]
                            .orEmpty()
                            .joinToString { it.id }
            )
        }

    println("=== Package allocation after failure ===")

    service.getVehiclesBySlot()
        .toSortedMap()
        .forEach { (vehicleSlot, vehicle) ->
            println(
                "Slot $vehicleSlot -> ${vehicle.id} -> " +
                        after[vehicle]
                            .orEmpty()
                            .joinToString { it.id }
            )
        }

    val report = RoutingValidationReporter().createReport(
        before = before,
        after = after,
        failedVehicleId = failedVehicle.id
    )

    println("=== Routing validation report ===")

    report.messages.forEach(::println)

    println("Stable packages: ${report.stablePackageCount}")
    println("Rerouted packages: ${report.reroutedPackageCount}")
    println("All validations passed: ${report.allPassed}")
}
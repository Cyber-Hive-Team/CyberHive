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

    val result = DomainGraphBuilder().buildConnectedDomainGraph(
        warehouses = data.warehouses,
        packages = data.packages,
        vehicles = data.vehicles,
        routes = data.routes
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

    val warehouses = result.success

    testPricing(warehouses)
    testSorting(warehouses)
    verifyGraph(warehouses)
    runRouting(warehouses)
}

private fun loadData(): LoadedData {
    val warehouses = CsvWarehouseRepository(
        CsvWarehouseDataSource(WAREHOUSE_FILE),
        WarehouseMapper()
    ).getAllWarehouses()

    warehouses.errorMessage?.let {
        println("WARNING: $it")
    }

    val warehouseList = warehouses.data.orEmpty()
    val warehouseMap = warehouseList.associateBy { it.id }

    val packages = CsvPackageRepository(
        CsvPackageDataSource(PACKAGE_FILE),
        PackageMapper(),
        warehouseMap
    ).getAllPackages()

    packages.errorMessage?.let {
        println("WARNING: $it")
    }

    val vehicles = CsvVehicleRepository(
        CsvVehicleDataSource(VEHICLE_FILE),
        VehicleMapper(),
        warehouseMap
    ).getVehicles()

    vehicles.errorMessage?.let {
        println("WARNING: $it")
    }

    val routes = CsvRouteRepository(
        CsvRouteDataSource(ROUTE_FILE),
        RouteMapper(),
        warehouseMap
    ).getAllRoutes()

    routes.errorMessage?.let {
        println("WARNING: $it")
    }

    println("\n=== Parsing Results ===")
    println("Warehouses: ${warehouseList.size}")
    println("Packages: ${packages.data.orEmpty().size}")
    println("Vehicles: ${vehicles.data.orEmpty().size}")
    println("Routes: ${routes.data.orEmpty().size}")

    return LoadedData(
        warehouses = warehouseList,
        packages = packages.data.orEmpty(),
        vehicles = vehicles.data.orEmpty(),
        routes = routes.data.orEmpty()
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

    println("--- Before Sorting (${warehouse.id}) ---")

    warehouse.getCargoQueue()
        .forEachIndexed { index, item ->
            println(
                "  $index: ${item.id} " +
                        "(Priority: ${item.priority}, " +
                        "Weight: ${item.weight}kg)"
            )
        }

    warehouses
        .filter { it.getCargoQueue().isNotEmpty() }
        .forEach { it.sortCargoQueue() }

    println("--- After Sorting (${warehouse.id}) ---")

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

    val allocationBefore = service.assignPackagesToVehicles(
        warehouse.getCargoQueue(),
        warehouse.getStationedVehicles()
    )

    val failedVehicle = service.getVehiclesBySlot()[FAILURE_SLOT]

    if (failedVehicle == null) {
        println("No vehicle found at slot $FAILURE_SLOT.")
        return
    }

    val allocationAfter = service.handleVehicleFailure(
        currentAllocation = allocationBefore,
        failedVehicleId = failedVehicle.id,
        failedVehicleSlot = FAILURE_SLOT
    )

    println("=== Package allocation before failure ===")
    printAllocation(service, allocationBefore)

    println("=== Package allocation after failure ===")
    printAllocation(service, allocationAfter)

    val report = RoutingValidationReporter().createReport(
        before = allocationBefore,
        after = allocationAfter,
        failedVehicleId = failedVehicle.id
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

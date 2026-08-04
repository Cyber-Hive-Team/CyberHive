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
import org.example.domain.model.Warehouse
import org.example.domain.pricing.EcoStrategy
import org.example.domain.pricing.ExpressStrategy
import org.example.domain.pricing.FragileStrategy
import org.example.domain.pricing.RoutePricingEngine
import org.example.sorting.sortPackagesByPriorityThenWeight

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

        buildResult.warnings.forEach { warning ->
            println("  - $warning")
        }
    }

    val connectedWarehouses = buildResult.success

    testPricing(connectedWarehouses)
    testSorting(connectedWarehouses)
    verifyGraph(connectedWarehouses)
}

private fun loadRawData(): RawData {
    val warehouseRaw = parseWarehouse(
        "src/main/resources/warehouses.csv"
    )
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

    if (samplePackage != null && sampleRoute != null) {
        val engine = RoutePricingEngine(EcoStrategy())

        println(
            "EcoStrategy price: $${
                engine.calculatePrice(
                    samplePackage,
                    sampleRoute
                )
            }"
        )

        engine.setStrategy(ExpressStrategy())

        println(
            "ExpressStrategy price: $${
                engine.calculatePrice(
                    samplePackage,
                    sampleRoute
                )
            }"
        )

        engine.setStrategy(FragileStrategy())

        println(
            "FragileStrategy price: $${
                engine.calculatePrice(
                    samplePackage,
                    sampleRoute
                )
            }"
        )
    } else {
        println("No package or route available to test pricing.")
    }
}

private fun testSorting(
    connectedWarehouses: List<Warehouse>
) {
    println("\n=== Selection Sort (Priority then Weight) ===")

    val firstHub = connectedWarehouses.firstOrNull()

    if (firstHub == null) {
        println("No hub available for sorting.")
        return
    }

    val cargoList =
        firstHub.getCargoQueue() as? MutableList<Package>

    if (cargoList == null || cargoList.isEmpty()) {
        println("First hub has no packages to sort.")
        return
    }

    println("\n--- Before Sorting (${firstHub.id}) ---")

    cargoList.forEachIndexed { index, packageItem ->
        println(
            "  $index: ${packageItem.id} " +
                    "(Priority: ${packageItem.priority}, " +
                    "Weight: ${packageItem.weight}kg)"
        )
    }

    sortPackagesByPriorityThenWeight(cargoList)

    println("\n--- After Sorting (${firstHub.id}) ---")

    cargoList.forEachIndexed { index, packageItem ->
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

    println("First hub: ${firstHub.id} (${firstHub.name})")
    println("  Packages: ${firstHub.getCargoQueue().size}")
    println("  Vehicles: ${firstHub.getStationedVehicles().size}")
    println("  Routes: ${firstHub.getOutgoingRoutes().size}")
}
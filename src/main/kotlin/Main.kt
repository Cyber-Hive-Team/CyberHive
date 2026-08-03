package org.example

import org.example.data.dataholder.PackageRaw
import org.example.data.dataholder.RouteRaw
import org.example.data.dataholder.VehicleRaw
import org.example.data.dataholder.WareHouseRaw
import org.example.data.dataparsing.parseFleet
import org.example.data.dataparsing.parsePackages
import org.example.data.dataparsing.parseRoutes
import org.example.data.dataparsing.parseWarehouse
import org.example.domain.builder.DomainGraphBuilder
import org.example.domain.model.Package
import org.example.domain.model.Warehouse
import org.example.domain.pricing.EcoStrategy
import org.example.domain.pricing.ExpressStrategy
import org.example.domain.pricing.FragileStrategy
import org.example.domain.pricing.RoutePricingEngine
import org.example.sorting.sortCargoQueueByWeightDescending

// Data class to hold all raw data with explicit types – eliminates need for casting and destructuring
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

    val connectedWarehouses = buildDomainGraph(rawData)

    testPricing(connectedWarehouses)
    testSorting(connectedWarehouses)
    verifyGraph(connectedWarehouses)
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

    return RawData(warehouseRaw, packageRaw, vehicleRaw, routeRaw)
}

private fun buildDomainGraph(rawData: RawData): List<Warehouse> {
    println("\n=== Building Domain Graph ===")
    val builder = DomainGraphBuilder()
    val connectedWarehouses = builder.buildConnectedDomainGraph(
        rawWarehouse = rawData.warehouses,
        rawPackage = rawData.packages,
        rawVehicle = rawData.vehicles,
        rawRoute = rawData.routes
    )
    println("Connected hubs: ${connectedWarehouses.size}")
    return connectedWarehouses
}

private fun testPricing(connectedWarehouses: List<Warehouse>) {
    println("\n=== Strategy Pattern Pricing ===")

    val sampleHub = connectedWarehouses.firstOrNull()
    val samplePackage = sampleHub?.getCargoQueue()?.firstOrNull()
    val sampleRoute = sampleHub?.getOutgoingRoutes()?.firstOrNull()

    if (samplePackage != null && sampleRoute != null) {
        val engine = RoutePricingEngine(EcoStrategy())
        println("EcoStrategy price: $${engine.calculatePrice(samplePackage, sampleRoute)}")

        engine.setStrategy(ExpressStrategy())
        println("ExpressStrategy price: $${engine.calculatePrice(samplePackage, sampleRoute)}")

        engine.setStrategy(FragileStrategy())
        println("FragileStrategy price: $${engine.calculatePrice(samplePackage, sampleRoute)}")
    } else {
        println("No package or route available to test pricing.")
    }
}

private fun testSorting(connectedWarehouses: List<Warehouse>) {
    println("\n=== Manual Quicksort (Descending by Weight) ===")

    if (connectedWarehouses.isNotEmpty()) {
        val firstHub = connectedWarehouses.first()
        val cargoList = firstHub.getCargoQueue() as? MutableList<Package>

        if (cargoList != null && cargoList.isNotEmpty()) {
            println("\n--- Before Sorting (${firstHub.id}) ---")
            cargoList.forEachIndexed { i, pkg ->
                println("  $i: ${pkg.id} (${pkg.weight}kg)")
            }

            sortCargoQueueByWeightDescending(cargoList)

            println("\n--- After Sorting (${firstHub.id}) ---")
            cargoList.forEachIndexed { i, pkg ->
                println("  $i: ${pkg.id} (${pkg.weight}kg)")
            }
        } else {
            println("First hub has no packages to sort.")
        }
    }
}

private fun verifyGraph(connectedWarehouses: List<Warehouse>) {
    println("\n=== Quick Verification ===")
    val firstHub = connectedWarehouses.firstOrNull()
    if (firstHub != null) {
        println("First hub: ${firstHub.id} (${firstHub.name})")
        println("  Packages: ${firstHub.getCargoQueue().size}")
        println("  Vehicles: ${firstHub.getStationedVehicles().size}")
        println("  Routes: ${firstHub.getOutgoingRoutes().size}")
    } else {
        println("No hubs built.")
    }
}
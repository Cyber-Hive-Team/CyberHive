
package org.example

import org.example.data.dataparsing.parseFleet
import org.example.data.dataparsing.parsePackages
import org.example.data.dataparsing.parseRoutes
import org.example.data.dataparsing.parseWarehouse
import org.example.domain.builder.DomainGraphBuilder
import org.example.domain.model.Package
import org.example.domain.pricing.EcoStrategy
import org.example.domain.pricing.ExpressStrategy
import org.example.domain.pricing.FragileStrategy
import org.example.domain.pricing.RoutePricingEngine
import org.example.sorting.sortCargoQueueByWeightDescending

fun main() {
    println("=== Cyber Hive ===")

    val warehouseRaw = parseWarehouse("src/main/resources/warehouses.csv")
    val packageRaw = parsePackages()
    val vehicleRaw = parseFleet()
    val routeRaw = parseRoutes()

    println("=== Parsing Results ===")
    println("Warehouses: ${warehouseRaw.size}")
    println("Packages: ${packageRaw.size}")
    println("Vehicles: ${vehicleRaw.size}")
    println("Routes: ${routeRaw.size}")

    if (warehouseRaw.isEmpty()) {
        println("ERROR: No warehouses found. Cannot build the domain graph.")
        return
    }

    println("\n=== Building Domain Graph ===")
    val builder = DomainGraphBuilder()
    val connectedWarehouses = builder.buildConnectedDomainGraph(
        rawWarehouse = warehouseRaw,
        rawPackage = packageRaw,
        rawVehicle = vehicleRaw,
        rawRoute = routeRaw
    )

    println("Connected hubs: ${connectedWarehouses.size}")

    println("\n===  Strategy Pattern Pricing ===")

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

    println("\n===  Manual Quicksort (Descending by Weight) ===")

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
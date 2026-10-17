package org.example.presentation


import org.example.domain.model.Package
import org.example.domain.model.Vehicle
import org.example.domain.model.Warehouse
import org.example.domain.routing.report.RoutingValidationReporter
import org.example.domain.routing.service.ConsistentHashVehicleRoutingService

private const val MIN_VEHICLES = 4
private const val FAILURE_SLOT = 40

class ConsistentHashRoutingRunner(
    private val warehouses: List<Warehouse>
) {

    fun run() {
        println("\n=== Consistent Hash Routing ===")

        val warehouse = warehouses.firstOrNull {
            it.getStationedVehicles().size >= org.example.presentation.MIN_VEHICLES
        } ?: return println("No warehouse has enough vehicles.")

        val service = ConsistentHashVehicleRoutingService()

        val before = service.assignPackagesToVehicles(
            warehouse.getCargoQueue(),
            warehouse.getStationedVehicles()
        )

        val failed = service.getVehiclesBySlot()[org.example.presentation.FAILURE_SLOT]
            ?: return println("No vehicle at slot ${org.example.presentation.FAILURE_SLOT}.")

        val after = service.handleVehicleFailure(
            before,
            failed.id,
            org.example.presentation.FAILURE_SLOT
        )

        printRoutingResult(service, before, after, failed.id)
    }

    private fun printRoutingResult(
        service: ConsistentHashVehicleRoutingService,
        before: Map<Vehicle, List<Package>>,
        after: Map<Vehicle, List<Package>>,
        failedId: String
    ) {
        println("=== Allocation Before Failure ===")
        printAllocation(service, before)

        println("=== Allocation After Failure ===")
        printAllocation(service, after)

        val report = RoutingValidationReporter()
            .createReport(before, after, failedId)

        report.messages.forEach(::println)

        println("Stable packages: " + report.stablePackageCount)
        println("Rerouted packages: " + report.reroutedPackageCount)
        println("All validations passed: " + report.allPassed)
    }

    private fun printAllocation(
        service: ConsistentHashVehicleRoutingService,
        allocation: Map<Vehicle, List<Package>>
    ) {
        service.getVehiclesBySlot()
            .toSortedMap()
            .forEach { (slot, vehicle) ->
                val packages = allocation[vehicle]
                    .orEmpty()
                    .joinToString { it.id }

                println("Slot $slot -> ${vehicle.id} -> $packages")
            }
    }
}

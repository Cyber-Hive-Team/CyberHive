package org.example.presentation

import org.example.domain.algorithm.dynamicprogramming.KnapsackCargoOptimizer
import org.example.domain.dispatch.ExpressDispatchProcessor
import org.example.domain.dispatch.StandardDispatchProcessor
import org.example.domain.dispatch.VehicleCapacityReservations
import org.example.domain.model.Package
import org.example.domain.model.Priority
import org.example.domain.model.RegionalZone
import org.example.domain.model.Vehicle
import org.example.domain.model.Warehouse
import org.example.domain.model.exception.EntityValidationException

class DispatchDemoRunner(
    private val optimizer: KnapsackCargoOptimizer,
    private val standardProcessor: StandardDispatchProcessor,
    private val expressProcessor: ExpressDispatchProcessor,
    private val reservations: VehicleCapacityReservations
) {

    fun run() {
        val origin = Warehouse(
            id = "WH-901",
            name = "Origin Warehouse",
            regionalZone = RegionalZone.NORTH,
            latitude = 31.5,
            longitude = 34.5
        )

        val destination = Warehouse(
            id = "WH-902",
            name = "Destination Warehouse",
            regionalZone = RegionalZone.NORTH,
            latitude = 32.0,
            longitude = 35.0
        )

        val vehicle = Vehicle(
            id = "TRK-9001",
            maxCapacityKg = 100.0,
            costPerKm = 2.5,
            currentHub = origin
        )

        val packagePool = listOf(
            Package(
                id = "PKG-900001",
                weight = 20.0,
                priority = Priority.STANDARD,
                originWarehouse = origin,
                destinationWarehouse = destination
            ),
            Package(
                id = "PKG-900002",
                weight = 40.0,
                priority = Priority.URGENT,
                originWarehouse = origin,
                destinationWarehouse = destination
            )
        )

        val selectedCargo = optimizer.selectOptimalPackages(
            vehicle,
            packagePool
        )

        try {
            println("=== Before dispatch ===")
            printStates(selectedCargo)

            standardProcessor.dispatch(vehicle, selectedCargo)

            println("=== After standard dispatch ===")
            printStates(selectedCargo)

            val expressPackage = Package(
                id = "PKG-900003",
                weight = 30.0,
                priority = Priority.URGENT,
                originWarehouse = origin,
                destinationWarehouse = destination
            )

            expressProcessor.dispatch(
                vehicle,
                listOf(expressPackage)
            )

            println("=== After express dispatch ===")
            printStates(listOf(expressPackage))

            println(
                "Reserved weight: " +
                        reservations.reservedWeightFor(vehicle.id)
            )
        } catch (error: EntityValidationException) {
            println("Dispatch failed: ${error.message}")
        }
    }

    private fun printStates(cargo: List<Package>) {
        cargo.forEach {
            println("${it.id}: ${it.state::class.simpleName}")
        }
    }
}

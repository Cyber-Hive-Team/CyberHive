package org.example.presentation

import org.example.domain.algorithm.dynamicprogramming.KnapsackCargoOptimizer
import org.example.domain.dispatch.BaseDispatchProcessor
import org.example.domain.model.Package
import org.example.domain.model.Vehicle
import org.example.domain.state.CreatedState

class EndToEndDispatchRunner(
    private val optimizer: KnapsackCargoOptimizer,
    private val dispatchProcessor: BaseDispatchProcessor
) {

    fun run(
        vehicles: List<Vehicle>,
        packages: List<Package>
    ) {
        println("\n=== End-to-End Cargo Dispatch ===")

        val vehicle = findSuitableVehicle(
            vehicles = vehicles, packages = packages
        ) ?: return println(
            "No vehicle with dispatchable packages was found."
        )

        val packagePool = packages.filter { cargoPackage ->
            cargoPackage.originWarehouse.id == vehicle.currentHub.id &&
                    cargoPackage.state is CreatedState
        }

        println("Vehicle: ${vehicle.id}")
        println("Hub: ${vehicle.currentHub.id}")
        println("Capacity: ${vehicle.maxCapacityKg} kg")
        println("Available packages: ${packagePool.size}")

        val selectedCargo = optimizer.selectOptimalPackages(vehicle = vehicle, packages = packagePool)

        if (selectedCargo.isEmpty()) {
            println("No packages selected by the optimizer.")
            return
        }
        printSelectedCargo(selectedCargo)
        println("\n--- Before Dispatch ---")
        printPackageStates(selectedCargo)

        dispatchProcessor.dispatch(vehicle = vehicle, cargo = selectedCargo)

        println("\n--- After Dispatch ---")
        printPackageStates(selectedCargo)
    }

    private fun findSuitableVehicle(
        vehicles: List<Vehicle>,
        packages: List<Package>
    ): Vehicle? {
        return vehicles.firstOrNull { vehicle ->
            packages.any { cargoPackage ->
                cargoPackage.originWarehouse.id == vehicle.currentHub.id &&
                        cargoPackage.state is CreatedState &&
                        cargoPackage.weight.isFinite() &&
                        cargoPackage.weight > 0.0 &&
                        cargoPackage.weight <= vehicle.maxCapacityKg
            }
        }
    }

    private fun printSelectedCargo(
        selectedCargo: List<Package>
    ) {
        println("\n--- Knapsack Selected Cargo ---")

        selectedCargo.forEach { cargoPackage ->
            println(
                "${cargoPackage.id} | " +
                        "Weight: ${cargoPackage.weight} kg | " +
                        "Priority: ${cargoPackage.priority}"
            )
        }

        println("Total selected weight: " + "${selectedCargo.sumOf { it.weight }} kg")
    }

    private fun printPackageStates(
        cargo: List<Package>
    ) {
        cargo.forEach { cargoPackage ->
            println("${cargoPackage.id}: " + cargoPackage.state.javaClass.simpleName)
        }
    }
}

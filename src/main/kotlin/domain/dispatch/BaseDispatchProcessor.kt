package org.example.domain.dispatch

import org.example.domain.model.Package
import org.example.domain.model.Vehicle
import org.example.domain.model.exception.EntityValidationException
import org.example.domain.state.CreatedState

abstract class BaseDispatchProcessor(private val capacityReservations: VehicleCapacityReservations) {

    fun dispatch(vehicle: Vehicle, cargo: List<Package>) {
        validateCargo(vehicle, cargo)
        reserveVehicleCapacity(vehicle, cargo)
        updateShipmentState(cargo)
        notifyDispatchStatus(vehicle, cargo)
    }

    protected fun validateCargo(vehicle: Vehicle, cargo: List<Package>) {
        validateCargoIsNotEmpty(cargo)
        validateNoDuplicatePackages(cargo)
        cargo.forEach { cargoPackage -> validatePackage(vehicle = vehicle, cargoPackage = cargoPackage) }
    }

    private fun validateCargoIsNotEmpty(cargo: List<Package>) {
        if (cargo.isEmpty()) {
            throw EntityValidationException("Cargo cannot be empty")
        }
    }

    private fun validateNoDuplicatePackages(cargo: List<Package>) {
        val packageIds = cargo.map { it.id }
        if (packageIds.distinct().size != packageIds.size) {
            throw EntityValidationException("Cargo contains duplicate packages")
        }
    }

    private fun validatePackage(vehicle: Vehicle, cargoPackage: Package) {
        validatePackageWeight(cargoPackage)
        validatePackageWarehouse(vehicle, cargoPackage)
        validatePackageState(cargoPackage)
    }

    private fun validatePackageWeight(cargoPackage: Package) {
        if (!cargoPackage.weight.isFinite() || cargoPackage.weight <= ZERO_WEIGHT_KG) {
            throw EntityValidationException("Invalid weight for package ${cargoPackage.id}")
        }
    }

    private fun validatePackageWarehouse(vehicle: Vehicle, cargoPackage: Package) {
        if (cargoPackage.originWarehouse.id != vehicle.currentHub.id) {
            throw EntityValidationException("Package ${cargoPackage.id} is at another warehouse")
        }
    }

    private fun validatePackageState(cargoPackage: Package) {
        if (cargoPackage.state !is CreatedState) {
            throw EntityValidationException("Package ${cargoPackage.id} is already dispatched")
        }
    }

    protected fun reserveVehicleCapacity(vehicle: Vehicle, cargo: List<Package>) {
        val totalWeight = cargo.sumOf { it.weight }

        capacityReservations.reserve(vehicle = vehicle, cargoWeight = totalWeight)
    }

    protected fun updateShipmentState(cargo: List<Package>) {
        cargo.forEach { cargoPackage ->
            cargoPackage.assignToVehicle()
            cargoPackage.startTransit()
        }
    }

    protected open fun notifyDispatchStatus(
        vehicle: Vehicle, cargo: List<Package>
    ) = Unit

    companion object {
        private const val ZERO_WEIGHT_KG = 0.0
    }
}

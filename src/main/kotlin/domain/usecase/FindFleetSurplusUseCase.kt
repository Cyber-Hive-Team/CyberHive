package org.example.domain.usecase

import org.example.domain.model.Package
import org.example.domain.model.Vehicle
import org.example.domain.model.exception.InvalidPackageWeightException
import org.example.domain.model.exception.InvalidVehicleCapacityException
import org.example.domain.model.result.FleetSurplusResult
import org.example.domain.repository.PackageRepository
import org.example.domain.repository.VehicleRepository
import org.example.domain.repository.WarehouseRepository

private const val ZERO_SURPLUS = 0.0

class FindFleetSurplusUseCase(
    private val warehouseRepository: WarehouseRepository,
    private val packageRepository: PackageRepository,
    private val vehicleRepository: VehicleRepository
) {

    operator fun invoke(): List<FleetSurplusResult> {
        return warehouseRepository
            .getAllWarehouses()
            .data
            .mapNotNull { warehouse ->
                calculateSurplus(warehouse.id)
            }
            .sortedByDescending { it.surplusKg }

    }

    private fun calculateSurplus(
        warehouseId: String
    ): FleetSurplusResult? {
        val packages = packageRepository
            .getPackagesByWarehouseId(warehouseId)
            .data
        val vehicles = vehicleRepository
            .getVehiclesByWarehouseId(warehouseId)
            .data
        validatePackages(packages, warehouseId)
        validateVehicles(vehicles, warehouseId)
        val surplus = vehicles.sumOf { it.maxCapacityKg } -
                packages.sumOf { it.weight }
        return surplus.takeIf { it > ZERO_SURPLUS }
            ?.let { FleetSurplusResult(warehouseId, it) }

    }

    private fun validatePackages(
        packages: List<Package>,
        warehouseId: String
    ) {
        if (packages.any { it.weight < ZERO_SURPLUS }) {
            throw InvalidPackageWeightException()
        }

    }

    private fun validateVehicles(
        vehicles: List<Vehicle>,
        warehouseId: String
    ) {
        if (vehicles.any { it.maxCapacityKg < ZERO_SURPLUS }) {
            throw InvalidVehicleCapacityException()
        }
    }

}

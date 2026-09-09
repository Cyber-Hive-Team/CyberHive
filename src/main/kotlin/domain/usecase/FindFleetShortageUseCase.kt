package org.example.domain.usecase

import org.example.domain.model.Package
import org.example.domain.model.Vehicle
import org.example.domain.model.exception.InvalidPackageWeightException
import org.example.domain.model.exception.InvalidVehicleCapacityException
import org.example.domain.model.result.FleetShortageResult
import org.example.domain.repository.PackageRepository
import org.example.domain.repository.VehicleRepository
import org.example.domain.repository.WarehouseRepository

private const val ZERO_SHORTAGE = 0.0

class FindFleetShortageUseCase(
    private val warehouseRepository: WarehouseRepository,
    private val packageRepository: PackageRepository,
    private val vehicleRepository: VehicleRepository
) {
    operator fun invoke(): List<FleetShortageResult> {
        return warehouseRepository.getAllWarehouses().data
            .mapNotNull { warehouse ->
                calculateShortage(warehouse.id)
            }
            .sortedByDescending { it.shortageKg }

    }

    private fun calculateShortage(
        warehouseId: String
    ): FleetShortageResult? {
        val packages = packageRepository.getPackagesByWarehouseId(warehouseId).data
        val vehicles = vehicleRepository.getVehiclesByWarehouseId(warehouseId).data
        validatePackages(packages, warehouseId)
        validateVehicles(vehicles, warehouseId)

        val shortage = packages.sumOf { it.weight } -
                vehicles.sumOf { it.maxCapacityKg }

        return shortage.takeIf { it > ZERO_SHORTAGE }
            ?.let { FleetShortageResult(warehouseId, it) }

    }

    private fun validatePackages(
        packages: List<Package>,
        warehouseId: String
    ) {
        if (packages.any { it.weight < ZERO_SHORTAGE }) {
            throw InvalidPackageWeightException("Negative package weight in warehouse: $warehouseId")
        }

    }

    private fun validateVehicles(
        vehicles: List<Vehicle>,
        warehouseId: String
    ) {
        if (vehicles.any { it.maxCapacityKg < ZERO_SHORTAGE }) {
            throw InvalidVehicleCapacityException("Negative vehicle capacity in warehouse: $warehouseId")
        }
    }

}


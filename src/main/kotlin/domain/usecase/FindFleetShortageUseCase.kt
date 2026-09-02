package org.example.domain.usecase

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
        return warehouseRepository
            .getAllWarehouses().data
            .mapNotNull { warehouse ->
                val totalCargoWeight = packageRepository
                        .getPackagesByWarehouseId(warehouse.id)
                        .data
                        .sumOf { cargoPackage ->
                            cargoPackage.weight
                        }
                val totalFleetCapacity = vehicleRepository
                        .getVehiclesByWarehouseId(warehouse.id)
                        .data
                        .sumOf { vehicle ->
                            vehicle.maxCapacityKg
                        }
                val shortage = totalCargoWeight - totalFleetCapacity
                if (shortage > ZERO_SHORTAGE) {
                    FleetShortageResult(
                        warehouseId = warehouse.id,
                        shortageKg = shortage
                    )
                } else {
                    null
                }
            }
            .sortedByDescending { result -> result.shortageKg }
    }

}


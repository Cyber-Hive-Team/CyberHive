package org.example.domain.usecase

import org.example.domain.repository.PackageRepository
import org.example.domain.repository.VehicleRepository
import org.example.domain.repository.WarehouseRepository

private const val ZERO_SURPLUS = 0.0


data class FleetSurplusResult(
    val warehouseId: String,
    val surplusKg: Double
)

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
                val cargoWeight = packageRepository
                        .getPackagesByWarehouseId(warehouse.id)
                        .data
                        .sumOf { cargoPackage ->
                            cargoPackage.weight
                        }
                val fleetCapacity = vehicleRepository
                        .getVehiclesByWarehouseId(warehouse.id)
                        .data
                        .sumOf { vehicle ->
                            vehicle.maxCapacityKg
                        }
                val surplus = fleetCapacity - cargoWeight
                if (surplus > ZERO_SURPLUS) {
                    FleetSurplusResult(warehouseId = warehouse.id, surplusKg = surplus)
                } else {
                    null
                }
            }
            .sortedByDescending { result -> result.surplusKg }
    }
}
package org.example.domain.usecase

import org.example.domain.model.Vehicle
import org.example.domain.model.Warehouse
import org.example.domain.repository.VehicleRepository

class FindStationedVehiclesByCapacityUseCase(
    private val vehicleRepository: VehicleRepository
) {
    operator fun invoke(warehouse: Warehouse, requiredWeightKg: Double
    ): List<Vehicle> {

        return vehicleRepository.getVehicles().data.filter { vehicle ->
                vehicle.currentHub.id == warehouse.id &&
                        vehicle.maxCapacityKg >= requiredWeightKg
            }
    }
}
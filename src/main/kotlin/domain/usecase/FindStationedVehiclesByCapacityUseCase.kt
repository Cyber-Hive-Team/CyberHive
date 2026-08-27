package org.example.domain.usecase

import org.example.domain.model.Vehicle
import org.example.domain.model.Warehouse

class FindStationedVehiclesByCapacityUseCase {

    operator fun invoke(warehouse: Warehouse, requiredWeightKg: Double): List<Vehicle> {
        return warehouse
            .getStationedVehicles()
            .filter { vehicle ->
                vehicle.maxCapacityKg >= requiredWeightKg
            }
    }
}
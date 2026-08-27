package org.example.domain.usecase

import org.example.domain.model.Warehouse

class GetWarehouseLoadFactorUseCase {

    operator fun invoke(warehouse: Warehouse): Double {

        val totalQueueWeight = warehouse.getCargoQueue()
            .map { it.weight }
            .fold(0.0) { total, weight ->
                total + weight
            }

        val totalFleetCapacity = warehouse.getStationedVehicles()
            .map { it.maxCapacityKg }
            .fold(0.0) { total, capacity ->
                total + capacity
            }

        return if (totalFleetCapacity == 0.0) {
            0.0
        } else {
            totalQueueWeight / totalFleetCapacity
        }
    }
}
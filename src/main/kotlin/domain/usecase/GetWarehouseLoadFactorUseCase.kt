package org.example.domain.usecase

import org.example.domain.repository.WarehouseRepository

private const val ZERO_VALUE = 0.0

class GetWarehouseLoadFactorUseCase(
    private val warehouseRepository: WarehouseRepository
) {

    operator fun invoke(warehouseId: String): Double {
        val warehouse = warehouseRepository
            .getWarehouseById(warehouseId)
            ?: return ZERO_VALUE

        val totalQueueWeight = warehouse.getCargoQueue()
            .map { it.weight }
            .fold(ZERO_VALUE) { total, weight ->
                total + weight
            }

        val totalFleetCapacity = warehouse.getStationedVehicles()
            .map { it.maxCapacityKg }
            .fold(ZERO_VALUE) { total, capacity ->
                total + capacity
            }

        return if (totalFleetCapacity == ZERO_VALUE) {
            ZERO_VALUE
        } else {
            totalQueueWeight / totalFleetCapacity
        }
    }
}
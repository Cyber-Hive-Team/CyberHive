package org.example.domain.usecase

import org.example.domain.model.Vehicle
import org.example.domain.model.Warehouse
import org.example.domain.model.exception.InvalidRequiredWeightException
import org.example.domain.repository.VehicleRepository


class FindStationedVehiclesByCapacityUseCase(
    private val vehicleRepository: VehicleRepository
) {

    operator fun invoke(
        warehouse: Warehouse,
        requiredWeightKg: Double
    ): Result<List<Vehicle>> {

        return runCatching {

            requiredWeightKg
                .takeIf { it > 0 }
                ?: throw InvalidRequiredWeightException()


            val vehicles =
                vehicleRepository
                    .getVehicles()
                    .getOrThrow()
                    .filter { vehicle ->

                        vehicle.currentHub.id == warehouse.id &&
                                vehicle.maxCapacityKg >= requiredWeightKg
                    }


            vehicles
        }
    }
}

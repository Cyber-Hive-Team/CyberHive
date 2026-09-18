package org.example.domain.usecase

import org.example.domain.algorithm.greedy.GreedyFleetDispatcher
import org.example.domain.model.RegionalZone
import org.example.domain.model.Vehicle
import org.example.domain.model.exception.VehicleNotFoundException
import org.example.domain.repository.VehicleRepository


class DispatchFleetGreedyUseCase(
    private val vehicleRepository: VehicleRepository,
    private val dispatcher: GreedyFleetDispatcher
) {

    operator fun invoke(
        targetZones: Set<RegionalZone>
    ): Result<List<Vehicle>> {

        return runCatching {

            val vehicles =
                getAvailableVehicles()

            dispatcher.dispatch(
                targetZones = targetZones,
                availableVehicles = vehicles
            )
        }
    }


    private fun getAvailableVehicles(): List<Vehicle> {

        val vehicles =
            vehicleRepository
                .getVehicles()
                .getOrThrow()

        if (vehicles.isEmpty()) {
            throw VehicleNotFoundException(
                "No vehicles available for fleet dispatch"
            )
        }

        return vehicles
    }
}

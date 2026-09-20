package org.example.domain.usecase.crud.vehicle

import org.example.domain.model.Vehicle
import org.example.domain.repository.VehicleRepository

class GetVehicleByIdUseCase(
    private val vehicleRepository: VehicleRepository
) {

    suspend operator fun invoke(vehicleId: String): Result<Vehicle> {
        return runCatching {
            vehicleRepository.getById(vehicleId)
                .getOrThrow()
        }
    }
}

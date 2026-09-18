package org.example.domain.usecase.crud.vehicle

import org.example.domain.repository.VehicleRepository

class DeleteVehicleUseCase(
    private val vehicleRepository: VehicleRepository
) {
    suspend operator fun invoke(vehicleId: String): Result<Boolean> {
        return runCatching {
            vehicleRepository.delete(vehicleId)
        }
    }
}



package org.example.domain.usecase

import org.example.domain.model.result.VehicleTrackingResult
import org.example.domain.repository.VehicleRepository

class TrackVehicleCurrentLocationUseCase(
    private val vehicleRepository: VehicleRepository
) {

    operator fun invoke(
        vehicleId: String
    ): VehicleTrackingResult? {
        val vehicle = vehicleRepository.getVehicleById(vehicleId)
            ?: return null

        return VehicleTrackingResult(
            vehicleId = vehicle.id,
            currentWarehouseId = vehicle.currentHub.id,
            currentWarehouseName = vehicle.currentHub.name
        )
    }
}

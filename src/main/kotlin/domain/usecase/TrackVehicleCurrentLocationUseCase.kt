package org.example.domain.usecase

import org.example.domain.model.result.VehicleTrackingResult
import org.example.domain.repository.VehicleRepository

class TrackVehicleCurrentLocationUseCase(
    private val vehicleRepository: VehicleRepository
) {

    suspend operator fun invoke(
        vehicleId: String
    ): Result<VehicleTrackingResult> {
        return vehicleRepository
            .getById(vehicleId)
            .map { vehicle ->
                VehicleTrackingResult(
                    vehicleId = vehicle.id,
                    currentWarehouseId =
                        vehicle.currentHub.id,
                    currentWarehouseName =
                        vehicle.currentHub.name
                )
            }
    }
}

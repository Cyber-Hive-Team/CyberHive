package org.example.data.mapper.remote

import org.example.data.remote.dto.request.CreateVehicleRequestDto
import org.example.data.remote.dto.request.UpdateVehicleRequestDto
import org.example.data.remote.dto.response.VehicleResponseDto
import org.example.domain.model.Vehicle
import org.example.data.exception.NullRequiredFieldException


fun VehicleResponseDto.toDomainModel(): Vehicle {
    val currentWarehouse = currentHub
        ?: throw NullRequiredFieldException(
            "Vehicle '$vehicleId' has null currentHub."
        )

        return Vehicle(
            id = vehicleId,
            maxCapacityKg = maxCapacityKg
                ?: throw NullRequiredFieldException(
                    "Vehicle '${vehicleId}' has null maxCapacityKg."
                ),
            costPerKm = costPerKm
                ?: throw NullRequiredFieldException(
                    "Vehicle '${vehicleId}' has null costPerKm."
                ),
            currentHub = currentWarehouse.toDomainModel()
        )
    }

fun Vehicle.toCreateRequest(): CreateVehicleRequestDto {
        return CreateVehicleRequestDto(
            vehicleId = id,
            currentHubId = currentHub.id,
            maxCapacityKg = maxCapacityKg,
            costPerKm = costPerKm
        )
    }

fun Vehicle.toUpdateRequest(currentHubId: String = currentHub.id): UpdateVehicleRequestDto {
    return UpdateVehicleRequestDto(
        currentHubId = currentHubId,
        maxCapacityKg = maxCapacityKg,
        costPerKm = costPerKm
    )
}

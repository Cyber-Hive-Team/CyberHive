package org.example.data.mapper.remote

import org.example.data.remote.dto.request.CreateVehicleRequestDto
import org.example.data.remote.dto.request.UpdateVehicleRequestDto
import org.example.data.remote.dto.response.VehicleResponseDto
import org.example.domain.model.Vehicle
import org.example.domain.model.Warehouse
import org.example.data.exception.NullRequiredFieldException


fun VehicleResponseDto.toDomainModel(
    currentHub: Warehouse
): Vehicle {

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
            currentHub = currentHub
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

fun Vehicle.toUpdateRequest(): UpdateVehicleRequestDto {
        return UpdateVehicleRequestDto(
            currentHubId = currentHub.id,
            maxCapacityKg = maxCapacityKg,
            costPerKm = costPerKm
        )
    }

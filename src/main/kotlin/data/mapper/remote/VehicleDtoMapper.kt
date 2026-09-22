package org.example.data.mapper.remote

import org.example.data.remote.dto.request.CreateVehicleRequestDto
import org.example.data.remote.dto.request.UpdateVehicleRequestDto
import org.example.data.remote.dto.response.VehicleResponseDto
import org.example.domain.model.Vehicle
import org.example.domain.model.Warehouse
import org.example.data.exception.NullRequiredFieldException

class VehicleDtoMapper {

    fun mapToDomain(
        raw: VehicleResponseDto,
        currentHub: Warehouse
    ): Vehicle {

        return Vehicle(
            id = raw.vehicleId,
            maxCapacityKg = raw.maxCapacityKg
                ?: throw NullRequiredFieldException(
                    "Vehicle '${raw.vehicleId}' has null maxCapacityKg."
                ),
            costPerKm = raw.costPerKm
                ?: throw NullRequiredFieldException(
                    "Vehicle '${raw.vehicleId}' has null costPerKm."
                ),
            currentHub = currentHub
        )
    }

    fun mapToCreateRequest(domain: Vehicle): CreateVehicleRequestDto {
        return CreateVehicleRequestDto(
            vehicleId = domain.id,
            currentHubId = domain.currentHub.id,
            maxCapacityKg = domain.maxCapacityKg,
            costPerKm = domain.costPerKm
        )
    }

    fun mapToUpdateRequest(domain: Vehicle): UpdateVehicleRequestDto {
        return UpdateVehicleRequestDto(
            currentHubId = domain.currentHub.id,
            maxCapacityKg = domain.maxCapacityKg,
            costPerKm = domain.costPerKm
        )
    }
}

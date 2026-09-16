package data.mapper.remote

import data.remote.dto.request.CreateVehicleRequestDto
import data.remote.dto.request.UpdateVehicleRequestDto
import data.remote.dto.response.VehicleResponseDto
import org.example.domain.model.Vehicle
import org.example.domain.model.Warehouse

class VehicleDtoMapper {

    fun mapToDomain(raw: VehicleResponseDto, currentHub: Warehouse): Vehicle {
        return Vehicle(
            id = raw.vehicleId,
            maxCapacityKg = raw.maxCapacityKg ?: 0.0,
            costPerKm = raw.costPerKm ?: 0.0,
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
            vehicleId = domain.id,
            currentHubId = domain.currentHub.id,
            maxCapacityKg = domain.maxCapacityKg,
            costPerKm = domain.costPerKm
        )
    }
}

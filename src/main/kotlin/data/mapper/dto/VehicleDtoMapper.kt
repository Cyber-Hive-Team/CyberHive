package data.mapper.dto

import data.remote.dto.VehicleDto
import org.example.domain.model.Vehicle
import org.example.domain.model.Warehouse

class VehicleDtoMapper {

    fun map(raw: VehicleDto, currentHub: Warehouse): Vehicle {
        return Vehicle(
            id = raw.vehicleId,
            maxCapacityKg = raw.maxCapacityKg ?: 0.0,
            costPerKm = raw.costPerKm ?: 0.0,
            currentHub = currentHub
        )
    }

    fun mapToDto(domain: Vehicle): VehicleDto {
        return VehicleDto(
            vehicleId = domain.id,
            currentHubId = domain.currentHub.id,
            maxCapacityKg = domain.maxCapacityKg,
            costPerKm = domain.costPerKm
        )
    }
}

package org.example.data.mapper.remote

import org.example.data.remote.dto.request.CreateWarehouseRequestDto
import org.example.data.remote.dto.request.UpdateWarehouseRequestDto
import org.example.data.remote.dto.response.WarehouseResponseDto
import org.example.domain.model.RegionalZone
import org.example.domain.model.Warehouse

class WarehouseRemoteMapper {

    fun mapToDomainModel(dto: WarehouseResponseDto): Warehouse {
        return Warehouse(
            id = dto.id,
            name = dto.name,
            regionalZone = RegionalZone.valueOf(dto.regionalZone),
            latitude = dto.latitude,
            longitude = dto.longitude
        )
    }

    fun mapToCreateRequest(warehouse: Warehouse): CreateWarehouseRequestDto {
        return CreateWarehouseRequestDto(
            id = warehouse.id,
            name = warehouse.name,
            regionalZone = warehouse.regionalZone.name,
            latitude = warehouse.latitude,
            longitude = warehouse.longitude
        )

    }

    fun mapToUpdateRequest(
        name: String? = null,
        regionalZone: RegionalZone? = null,
        latitude: Double? = null,
        longitude: Double? = null
    ): UpdateWarehouseRequestDto {
        return UpdateWarehouseRequestDto(
            name = name,
            regionalZone = regionalZone?.name,
            latitude = latitude,
            longitude = longitude
        )

    }

}

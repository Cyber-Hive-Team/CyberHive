package org.example.data.mapper.remote

import org.example.data.exception.NullRequiredFieldException
import org.example.data.remote.dto.request.CreateWarehouseRequestDto
import org.example.data.remote.dto.request.UpdateWarehouseRequestDto
import org.example.data.remote.dto.response.WarehouseResponseDto
import org.example.domain.model.RegionalZone
import org.example.domain.model.Warehouse

class WarehouseRemoteMapper {

    fun mapToDomainModel(
        dto: WarehouseResponseDto
    ): Warehouse {

        validateRequiredFields(dto)

        return Warehouse(
            id = dto.id,
            name = dto.name,
            regionalZone = RegionalZone.valueOf(dto.regionalZone!!),
            latitude = dto.latitude!!,
            longitude = dto.longitude!!
        )
    }


    private fun validateRequiredFields(
        dto: WarehouseResponseDto
    ) {

        val missingFields =
            mutableListOf<String>()

        if (dto.regionalZone.isNullOrBlank()) {
            missingFields.add("regionalZone")
        }

        if (dto.latitude == null) {
            missingFields.add("latitude")
        }

        if (dto.longitude == null) {
            missingFields.add("longitude")
        }

        if (missingFields.isNotEmpty()) {
            throw NullRequiredFieldException(
                "Warehouse '${dto.id}' has null fields: ${missingFields.joinToString()}"
            )
        }
    }


    fun mapToCreateRequest(
        warehouse: Warehouse
    ): CreateWarehouseRequestDto {
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

package org.example.data.mapper.remote

import org.example.data.exception.NullRequiredFieldException
import org.example.data.remote.dto.request.CreateWarehouseRequestDto
import org.example.data.remote.dto.request.UpdateWarehouseRequestDto
import org.example.data.remote.dto.response.WarehouseResponseDto
import org.example.domain.model.RegionalZone
import org.example.domain.model.Warehouse
import org.example.domain.model.input.UpdateWarehouseInput


fun WarehouseResponseDto.toDomainModel(): Warehouse {

    validateRequiredFields()

        return Warehouse(
            id = id,
            name = name,
            regionalZone = RegionalZone.valueOf(regionalZone!!),
            latitude = latitude!!,
            longitude = longitude!!
        )
    }

private fun WarehouseResponseDto.validateRequiredFields() {

        val missingFields =
            mutableListOf<String>()

    if (regionalZone.isNullOrBlank()) {
            missingFields.add("regionalZone")
        }

    if (latitude == null) {
            missingFields.add("latitude")
        }

    if (longitude == null) {
            missingFields.add("longitude")
        }

        if (missingFields.isNotEmpty()) {
            throw NullRequiredFieldException(
                "Warehouse '${id}' has null fields: ${missingFields.joinToString()}"
            )
        }
    }


fun Warehouse.toCreateRequest(): CreateWarehouseRequestDto {

        return CreateWarehouseRequestDto(
            id = id,
            name = name,
            regionalZone = regionalZone.name,
            latitude = latitude,
            longitude = longitude
        )
    }


fun UpdateWarehouseInput.toUpdateRequest(): UpdateWarehouseRequestDto {

        return UpdateWarehouseRequestDto(
            name = name,
            regionalZone = regionalZone?.name,
            latitude = latitude,
            longitude = longitude
        )
    }


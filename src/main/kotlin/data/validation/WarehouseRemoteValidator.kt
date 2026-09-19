package org.example.data.validation

import org.example.data.exception.NullRequiredFieldException
import org.example.data.remote.dto.response.WarehouseResponseDto

class WarehouseRemoteValidator {
    fun validate(
        dto: WarehouseResponseDto
    ) {

        val errors = mutableListOf<String>()

        if (dto.regionalZone.isNullOrBlank()) {
            errors.add("regionalZone is null")
        }

        if (dto.latitude == null) {
            errors.add("latitude is null")
        }

        if (dto.longitude == null) {
            errors.add("longitude is null")
        }


        if (errors.isNotEmpty()) {

            throw NullRequiredFieldException(
                "Warehouse '${dto.id}': ${errors.joinToString()}"
            )
        }
    }
}


package org.example.data.validation

import org.example.data.exception.NullRequiredFieldException
import org.example.data.remote.dto.response.VehicleResponseDto

class VehicleRemoteValidator {

    fun validate(
        dto: VehicleResponseDto
    ) {

        if (dto.maxCapacityKg == null) {
            throw NullRequiredFieldException(
                "Vehicle '${dto.vehicleId}' has null maxCapacityKg."
            )
        }


        if (dto.costPerKm == null) {
            throw NullRequiredFieldException(
                "Vehicle '${dto.vehicleId}' has null costPerKm."
            )
        }
    }
}

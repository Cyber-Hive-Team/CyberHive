package org.example.data.validation

import org.example.data.exception.NullRequiredFieldException
import org.example.data.remote.dto.response.RouteResponseDto

class RouteRemoteValidator {

    fun validate(
        dto: RouteResponseDto
    ) {

        if (dto.distanceKm == null) {

            throw NullRequiredFieldException(
                "Route '${dto.routeId}' has null distanceKm."
            )
        }


        if (dto.typicalDelayMin == null) {

            throw NullRequiredFieldException(
                "Route '${dto.routeId}' has null typicalDelayMin."
            )
        }
    }
}

package org.example.data.mapper.remote

import org.example.data.exception.NullRequiredFieldException
import org.example.data.remote.dto.request.CreateRouteRequestDto
import org.example.data.remote.dto.request.UpdateRouteRequestDto
import org.example.data.remote.dto.response.RouteResponseDto
import org.example.domain.model.Route


fun RouteResponseDto.toDomainModel(): Route {
    return Route(
        id = routeId,
        distanceKm = distanceKm.requiredField(
            "Route '$routeId' has null distanceKm."
        ),
        typicalDelayMin = typicalDelayMin.requiredField(
            "Route '$routeId' has null typicalDelayMin."
        ),
        originWarehouse = originHub.requiredField(
            "Route '$routeId' has null originHub."
        ).toDomainModel(),
        destinationWarehouse = destinationHub.requiredField(
            "Route '$routeId' has null destinationHub."
        ).toDomainModel()
    )
}

fun Route.toCreateRequest(): CreateRouteRequestDto {
        return CreateRouteRequestDto(
            routeId = id,
            originHubId = originWarehouse.id,
            destinationHubId = destinationWarehouse.id,
            distanceKm = distanceKm,
            typicalDelayMin = typicalDelayMin
        )
    }

fun Route.toUpdateRequest(): UpdateRouteRequestDto {
        return UpdateRouteRequestDto(
            originHubId = originWarehouse.id,
            destinationHubId = destinationWarehouse.id,
            distanceKm = distanceKm,
            typicalDelayMin = typicalDelayMin
        )
    }

private fun <T : Any> T?.requiredField(message: String): T {
    return this ?: throw NullRequiredFieldException(message)
}


package org.example.data.mapper.remote

import org.example.data.exception.NullRequiredFieldException
import org.example.data.remote.dto.request.CreateRouteRequestDto
import org.example.data.remote.dto.request.UpdateRouteRequestDto
import org.example.data.remote.dto.response.RouteResponseDto
import org.example.domain.model.Route
import org.example.domain.model.Warehouse


fun RouteResponseDto.toDomainModel(
        originWarehouse: Warehouse,
        destinationWarehouse: Warehouse
    ): Route {
        return Route(
            id = routeId,
            distanceKm = distanceKm
                ?: throw NullRequiredFieldException(
                    "Route '${routeId}' has null distanceKm."
                ),
            typicalDelayMin = typicalDelayMin
                ?: throw NullRequiredFieldException(
                    "Route '${routeId}' has null typicalDelayMin."
                ),
            originWarehouse = originWarehouse,
            destinationWarehouse = destinationWarehouse
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



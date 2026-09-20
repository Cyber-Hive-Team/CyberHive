package org.example.data.mapper.remote

import org.example.data.remote.dto.response.RouteResponseDto
import org.example.data.remote.dto.request.CreateRouteRequestDto
import org.example.data.remote.dto.request.UpdateRouteRequestDto
import org.example.domain.model.Route
import org.example.domain.model.Warehouse

class RouteDtoMapper {

    fun mapToDomain(
        raw: RouteResponseDto,
        originWarehouse: Warehouse,
        destinationWarehouse: Warehouse
    ): Route {
        return Route(
            id = raw.routeId,
            distanceKm = raw.distanceKm!!,
            typicalDelayMin = raw.typicalDelayMin!!,
            originWarehouse = originWarehouse,
            destinationWarehouse = destinationWarehouse
        )
    }

    fun mapToCreateRequest(domain: Route): CreateRouteRequestDto {
        return CreateRouteRequestDto(
            routeId = domain.id,
            originHubId = domain.originWarehouse.id,
            destinationHubId = domain.destinationWarehouse.id,
            distanceKm = domain.distanceKm,
            typicalDelayMin = domain.typicalDelayMin
        )
    }

    fun mapToUpdateRequest(domain: Route): UpdateRouteRequestDto {
        return UpdateRouteRequestDto(
            originHubId = domain.originWarehouse.id,
            destinationHubId = domain.destinationWarehouse.id,
            distanceKm = domain.distanceKm,
            typicalDelayMin = domain.typicalDelayMin
        )
    }

}

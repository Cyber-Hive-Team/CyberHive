package data.mapper.remote

import data.remote.dto.RouteDto
import org.example.domain.model.Route
import org.example.domain.model.Warehouse

class RouteDtoMapper {

    fun map(
        raw: RouteDto,
        originWarehouse: Warehouse,
        destinationWarehouse: Warehouse
    ): Route {
        return Route(
            id = raw.routeId,
            distanceKm = raw.distanceKm,
            typicalDelayMin = raw.typicalDelayMinutes ?: 0,
            originWarehouse = originWarehouse,
            destinationWarehouse = destinationWarehouse
        )
    }

    fun mapToDto(domain: Route): RouteDto {
        return RouteDto(
            routeId = domain.id,
            originHubId = domain.originWarehouse.id,
            destinationHubId = domain.destinationWarehouse.id,
            distanceKm = domain.distanceKm,
            typicalDelayMinutes = domain.typicalDelayMin
        )
    }
}

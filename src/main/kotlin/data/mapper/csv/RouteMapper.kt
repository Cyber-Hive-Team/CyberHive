package org.example.data.mapper.csv

import org.example.data.datasource.local.model.RouteLocalData
import org.example.domain.model.Route
import org.example.data.exception.NullRequiredFieldException

fun RouteLocalData.toDomainModel(): Route {

    return Route(
        id = routeRaw.id,
        distanceKm = routeRaw.distanceKm
            ?: throw NullRequiredFieldException(
            "Route '${routeRaw.id}' has null distanceKm"
        ),
        typicalDelayMin = routeRaw.typicalDelayMin,
        originWarehouse = originWarehouse.toDomainModel(),
        destinationWarehouse = destinationWarehouse.toDomainModel()
    )
}

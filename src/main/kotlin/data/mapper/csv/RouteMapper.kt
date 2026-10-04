package org.example.data.mapper.csv

import org.example.data.datasource.local.model.RouteLocalData
import org.example.domain.model.Route

fun RouteLocalData.toDomainModel(): Route {

    return Route(
        id = routeRaw.id,
        distanceKm = routeRaw.distanceKm,
        typicalDelayMin = routeRaw.typicalDelayMin,
        originWarehouse = originWarehouse.toDomainModel(),
        destinationWarehouse = destinationWarehouse.toDomainModel()
    )
}

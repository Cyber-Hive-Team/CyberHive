package org.example.data.mapper

import org.example.data.dataholder.RouteRaw
import org.example.domain.model.Route
import org.example.domain.model.Warehouse

class RouteMapper {

    fun map(
        raw: RouteRaw,
        originWarehouse: Warehouse,
        destinationWarehouse: Warehouse
    ): Route {
        return Route(
            id = raw.id,
            distanceKm = raw.distanceKm,
            typicalDelayMin = raw.typicalDelayMin,
            originWarehouse = originWarehouse,
            destinationWarehouse = destinationWarehouse
        )
    }
}
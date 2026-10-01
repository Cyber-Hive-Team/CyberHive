package org.example.data.mapper.csv

import org.example.data.dataholder.RouteRaw
import org.example.domain.model.Route
import org.example.domain.model.Warehouse


fun RouteRaw.toDomainModel(
        originWarehouse: Warehouse,
        destinationWarehouse: Warehouse
    ): Route {
        return Route(
            id = id,
            distanceKm = distanceKm,
            typicalDelayMin = typicalDelayMin,
            originWarehouse = originWarehouse,
            destinationWarehouse = destinationWarehouse
        )
    }


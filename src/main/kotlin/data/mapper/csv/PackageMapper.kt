package org.example.data.mapper.csv

import org.example.data.dataholder.PackageRaw
import org.example.domain.model.Package
import org.example.domain.model.Warehouse

fun PackageRaw.toDomainModel(
    originWarehouse: Warehouse,
    destinationWarehouse: Warehouse
): Package {
    return Package(
        id = id,
        weight = weight,
        priority = priority,
        originWarehouse = originWarehouse,
        destinationWarehouse = destinationWarehouse
    )
}

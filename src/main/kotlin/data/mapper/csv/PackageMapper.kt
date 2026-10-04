package org.example.data.mapper.csv

import org.example.domain.model.Package
import org.example.data.datasource.local.model.PackageLocalData

fun PackageLocalData.toDomainModel(): Package {

    return Package(
        id = packageRaw.id,
        weight = packageRaw.weight,
        priority = packageRaw.priority,
        originWarehouse = originWarehouse.toDomainModel(),
        destinationWarehouse = destinationWarehouse.toDomainModel()
    )

}

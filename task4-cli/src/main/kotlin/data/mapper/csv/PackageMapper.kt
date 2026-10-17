package org.example.data.mapper.csv

import org.example.domain.model.Package
import org.example.data.datasource.local.model.PackageLocalData
import org.example.data.exception.NullRequiredFieldException

fun PackageLocalData.toDomainModel(): Package {

    return Package(
        id = packageRaw.id,
        weight = packageRaw.weight
          ?: throw NullRequiredFieldException("Package '${packageRaw.id}' has null weight"),
        priority = packageRaw.priority,
        originWarehouse = originWarehouse.toDomainModel(),
        destinationWarehouse = destinationWarehouse.toDomainModel()
    )

}

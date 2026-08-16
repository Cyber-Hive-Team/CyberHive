package org.example.data.mapper

import org.example.data.dataholder.PackageRaw
import org.example.domain.model.Package
import org.example.domain.model.Warehouse

class PackageMapper {

    fun map(
        raw: PackageRaw,
        originWarehouse: Warehouse,
        destinationWarehouse: Warehouse
    ): Package {
        return Package(
            id = raw.id,
            weight = raw.weight,
            priority = raw.priority,
            originWarehouse = originWarehouse,
            destinationWarehouse = destinationWarehouse
        )
    }
}
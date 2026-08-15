package org.example.data.mapper

import org.example.data.dataholder.PackageMappingResult
import org.example.data.dataholder.PackageRaw
import org.example.domain.model.Package
import org.example.domain.model.Warehouse

class PackageMapper(
    private val warehouseMap: Map<String, Warehouse>
) {

    fun map(raw: PackageRaw): PackageMappingResult {
        val originWarehouse = warehouseMap[raw.originHubId]
        val destinationWarehouse = warehouseMap[raw.destinationHubId]

        val warnings = mutableListOf<String>()

        if (originWarehouse == null) {
            warnings.add(
                "Warning: Package ${raw.id} skipped - " +
                        "origin warehouse not found: ${raw.originHubId}"
            )
        }

        if (destinationWarehouse == null) {
            warnings.add(
                "Warning: Package ${raw.id} skipped - " +
                        "destination warehouse not found: ${raw.destinationHubId}"
            )
        }

        if (originWarehouse == null || destinationWarehouse == null) {
            return PackageMappingResult(
                packageItem = null,
                warnings = warnings
            )
        }

        return PackageMappingResult(
            packageItem = Package(
                id = raw.id,
                weight = raw.weight,
                priority = raw.priority,
                originWarehouse = originWarehouse,
                destinationWarehouse = destinationWarehouse
            ),
            warnings = emptyList()
        )
    }
}
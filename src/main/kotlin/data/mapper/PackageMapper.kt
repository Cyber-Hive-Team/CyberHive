package org.example.data.mapper

import org.example.data.dataholder.PackageMappingResult
import org.example.data.dataholder.PackageRaw
import org.example.domain.model.Package
import org.example.domain.model.Warehouse

class PackageMapper(
    private val warehouseMap: Map<String, Warehouse>
) {

    fun map(raw: PackageRaw): PackageMappingResult {
        val warnings = mutableListOf<String>()

        val originWarehouse = findWarehouse(
            warehouseId = raw.originHubId,
            packageId = raw.id,
            warehouseType = "origin",
            warnings = warnings
        )

        val destinationWarehouse = findWarehouse(
            warehouseId = raw.destinationHubId,
            packageId = raw.id,
            warehouseType = "destination",
            warnings = warnings
        )

        if (originWarehouse == null || destinationWarehouse == null) {
            return PackageMappingResult(
                packageItem = null,
                warnings = warnings
            )
        }

        return PackageMappingResult(
            packageItem = createPackage(
                raw = raw,
                originWarehouse = originWarehouse,
                destinationWarehouse = destinationWarehouse
            ),
            warnings = warnings
        )
    }

    private fun findWarehouse(
        warehouseId: String,
        packageId: String,
        warehouseType: String,
        warnings: MutableList<String>
    ): Warehouse? {
        val warehouse = warehouseMap[warehouseId]

        if (warehouse == null) {
            warnings.add(
                "Warning: Package $packageId skipped - " +
                        "$warehouseType warehouse not found: $warehouseId"
            )
        }

        return warehouse
    }


    private fun createPackage(
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
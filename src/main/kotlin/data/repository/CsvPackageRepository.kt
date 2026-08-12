package org.example.data.repository

import org.example.data.dataparsing.parsePackages
import org.example.domain.model.Package
import org.example.domain.repository.PackageRepository
import org.example.domain.repository.WarehouseRepository

class CsvPackageRepository(
    private val filePath: String,
    private val warehouseRepository: WarehouseRepository
) : PackageRepository {

    override fun getAllPackages(): List<Package> {
        val rawPackages = parsePackages(filePath)
        val warehouseMap = warehouseRepository.getAllWarehouses().associateBy { it.id }

        return rawPackages.mapNotNull { raw ->
            val originWarehouse = warehouseMap[raw.originHubId]
            val destinationWarehouse = warehouseMap[raw.destinationHubId]

            if (originWarehouse == null || destinationWarehouse == null) {
                println("Warning: Package ${raw.id} skipped - warehouse not found.")
                return@mapNotNull null
            }

            Package(
                id = raw.id,
                weight = raw.weight,
                priority = raw.priority,
                originWarehouse = originWarehouse,
                destinationWarehouse = destinationWarehouse
            )
        }
    }
}
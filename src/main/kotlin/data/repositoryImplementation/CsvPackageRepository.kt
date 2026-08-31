package org.example.data.repository

import org.example.data.dataholder.PackageRaw
import org.example.data.datasource.PackageDataSource
import org.example.data.mapper.PackageMapper
import org.example.domain.model.Package
import org.example.domain.model.Result
import org.example.domain.model.Warehouse
import org.example.domain.repository.PackageRepository

class CsvPackageRepository(
    private val dataSource: PackageDataSource,
    private val mapper: PackageMapper,
    private val warehouseMap: Map<String, Warehouse>
) : PackageRepository {

    override fun getAllPackages(): Result<List<Package>> {
        val rawResults = dataSource.getPackages()
        val warnings = rawResults.mapNotNull { it.errorMessage }.toMutableList()
        val rawPackages = rawResults.mapNotNull { it.rawData }
        val packages = mapPackages(rawPackages = rawPackages, warnings = warnings)
        return Result(
            data = packages,
            errorMessage = warnings
                .takeIf { it.isNotEmpty() }
                ?.joinToString("; ")
        )
    }

    private fun mapPackages(
        rawPackages: List<PackageRaw>,
        warnings: MutableList<String>
    ): List<Package> =
        rawPackages.mapNotNull { raw ->
            val origin = warehouseMap[normalizeId(raw.originHubId)]
            val destination = warehouseMap[normalizeId(raw.destinationHubId)]

            val validation = validate(raw, origin, destination)

            if (validation.isNotEmpty()) {
                warnings.addAll(validation)
                null
            } else {
                mapper.map(raw, origin!!, destination!!)
            }
        }

    private fun validate(
        raw: PackageRaw,
        origin: Warehouse?,
        destination: Warehouse?
    ): List<String> {
        val warnings = mutableListOf<String>()

        if (raw.id.isBlank()) {
            warnings.add("Warning: Package skipped - missing id")
        }

        if (origin == null) {
            warnings.add(
                "Warning: Package ${raw.id} skipped - " +
                        "origin warehouse not found: ${raw.originHubId}"
            )
        }

        if (destination == null) {
            warnings.add(
                "Warning: Package ${raw.id} skipped - " +
                        "destination warehouse not found: ${raw.destinationHubId}"
            )
        }

        return warnings
    }

    private fun normalizeId(id: String): String =
        id.trim().uppercase()

    override fun getPackagesByWarehouseId(warehouseId: String): Result<List<Package>> {
        val result = getAllPackages()
        return Result(
            data = result.data.filter { cargoPackage ->
                cargoPackage.originWarehouse.id == warehouseId
            },
            errorMessage = result.errorMessage
        )
    }
}
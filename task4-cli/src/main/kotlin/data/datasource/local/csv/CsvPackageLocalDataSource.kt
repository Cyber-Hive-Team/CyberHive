package org.example.data.datasource.local.csv

import org.example.data.dataholder.RawResult
import org.example.data.dataparsing.parsePackages
import org.example.data.datasource.PackageDataSource
import org.example.data.datasource.WarehouseDataSource
import org.example.data.datasource.local.model.PackageLocalData
import org.example.data.dataholder.PackageRaw
import org.example.data.dataholder.WarehouseRaw
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class CsvPackageLocalDataSource(
    private val filePath: String,
    private val warehouseDataSource: WarehouseDataSource
) : PackageDataSource {

    override suspend fun getPackages(): List<RawResult<PackageLocalData>> =
        withContext(Dispatchers.IO) {
            val warehousesById = loadWarehousesById()

            parsePackages(filePath).map { result ->
                resolvePackage(result, warehousesById)
            }
        }

    private suspend  fun loadWarehousesById(): Map<String, WarehouseRaw> {
        return warehouseDataSource.getWarehouses()
            .mapNotNull { it.rawData }
            .associateBy { it.id }
    }

    private fun resolvePackage(
        result: RawResult<PackageRaw>,
        warehousesById: Map<String, WarehouseRaw>
    ): RawResult<PackageLocalData> {
        val raw = result.rawData
            ?: return RawResult(null, result.errorMessage)

        val originWarehouse = warehousesById[raw.originHubId]
        val destinationWarehouse = warehousesById[raw.destinationHubId]

        return when {
            originWarehouse == null -> RawResult(
                null,
                "Package '${raw.id}' origin warehouse '${raw.originHubId}' not found."
            )

            destinationWarehouse == null -> RawResult(
                null,
                "Package '${raw.id}' destination warehouse '${raw.destinationHubId}' not found."
            )

            else -> RawResult(
                rawData = PackageLocalData(
                    packageRaw = raw,
                    originWarehouse = originWarehouse,
                    destinationWarehouse = destinationWarehouse
                )
            )
        }
    }

    override suspend fun replaceAll(packages: List<PackageRaw>) =
        withContext(Dispatchers.IO) {
            val content = buildString {
                appendLine("id,weight,originHubId,destinationHubId,priority")

                packages.forEach { packageRaw ->
                    appendLine(
                        listOf(
                            packageRaw.id,
                            packageRaw.weight ?: "",
                            packageRaw.originHubId,
                            packageRaw.destinationHubId,
                            packageRaw.priority.name
                        ).joinToString(",")
                    )
                }
            }

            replaceCsvFile(
                filePath = filePath,
                content = content
            )
        }



}

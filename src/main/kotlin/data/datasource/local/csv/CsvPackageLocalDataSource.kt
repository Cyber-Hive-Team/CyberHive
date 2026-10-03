package org.example.data.datasource.local.csv

import org.example.data.dataholder.RawResult
import org.example.data.dataparsing.parsePackages
import org.example.data.datasource.PackageDataSource
import org.example.data.datasource.WarehouseDataSource
import org.example.data.datasource.local.model.PackageLocalData

class CsvPackageLocalDataSource(
    private val filePath: String,
    private val warehouseDataSource: WarehouseDataSource
) : PackageDataSource {

    override fun getPackages(): List<RawResult<PackageLocalData>> {

        val warehousesById = warehouseDataSource.getWarehouses().mapNotNull { it.rawData }.associateBy { it.id }

        return parsePackages(filePath).map { result ->
            val raw = result.rawData
            if (raw == null) {
                RawResult(
                    rawData = null,
                    errorMessage = result.errorMessage
                )
            } else {

                val originWarehouse = warehousesById[raw.originHubId]
                val destinationWarehouse = warehousesById[raw.destinationHubId]

                when {
                    originWarehouse == null -> {
                        RawResult(
                            rawData = null,
                            errorMessage =
                                "Package '${raw.id}' origin warehouse '${raw.originHubId}' not found."
                        )
                    }

                    destinationWarehouse == null -> {
                        RawResult(
                            rawData = null,
                            errorMessage =
                                "Package '${raw.id}' destination warehouse '${raw.destinationHubId}' not found."
                        )
                    }

                    else -> {
                        RawResult(
                            rawData = PackageLocalData(
                                packageRaw = raw,
                                originWarehouse = originWarehouse,
                                destinationWarehouse = destinationWarehouse
                            )
                        )
                    }
                }
            }
        }
    }
}

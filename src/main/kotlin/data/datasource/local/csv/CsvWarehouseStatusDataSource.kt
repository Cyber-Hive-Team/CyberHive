package org.example.data.datasource.local.csv

import org.example.domain.model.WarehouseStatus
import java.io.File

class CsvWarehouseStatusDataSource(
    private val filePath: String
) {
    companion object {
        private const val CSV_SEPARATOR = ","
        private const val HEADER_ROW_INDEX = 1
        private const val WAREHOUSE_ID_INDEX = 0
        private const val STATUS_INDEX = 1
        private const val EXPECTED_COLUMN_COUNT = 2
    }

    fun getStatus(
        warehouseId: String
    ): WarehouseStatus {

        return readStatuses()[warehouseId]
            ?: WarehouseStatus.OPERATIONAL
    }


    fun updateStatus(
        warehouseId: String,
        status: WarehouseStatus
    ): Boolean {

        val statuses =
            readStatuses()
                .toMutableMap()

        statuses[warehouseId] = status

        return saveStatuses(statuses)
    }


    private fun readStatuses(): Map<String, WarehouseStatus> {

        val file = File(filePath)

        if (!file.exists()) {
            return emptyMap()
        }

        return file.readLines()
            .drop(HEADER_ROW_INDEX)
            .mapNotNull { row ->

                val columns =
                    row.split(CSV_SEPARATOR)

                if (columns.size != EXPECTED_COLUMN_COUNT) {
                    null
                } else {

                    val id =
                        columns[WAREHOUSE_ID_INDEX]
                            .trim()

                    val status =
                        runCatching {
                            WarehouseStatus.valueOf(
                                columns[STATUS_INDEX].trim()
                            )
                        }.getOrNull()

                    status?.let {
                        id to it
                    }
                }
            }
            .toMap()
    }


    private fun saveStatuses(
        statuses: Map<String, WarehouseStatus>
    ): Boolean {

        return runCatching {

            val file = File(filePath)

            file.parentFile?.mkdirs()

            file.writeText(
                buildString {

                    appendLine("warehouseId,status")

                    statuses.forEach { (id, status) ->

                        appendLine(
                            "$id,$status"
                        )
                    }
                }
            )

        }.isSuccess
    }
}

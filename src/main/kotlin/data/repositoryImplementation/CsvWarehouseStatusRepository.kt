package org.example.data.repositoryImplementation

import org.example.domain.model.WarehouseStatus
import org.example.domain.repository.WarehouseStatusRepository
import java.io.File

private const val CSV_SEPARATOR = ","
private const val HEADER_ROW_INDEX = 0
private const val WAREHOUSE_ID_INDEX = 0
private const val STATUS_INDEX = 1
private const val EXPECTED_COLUMN_COUNT = 2
private const val CSV_HEADER = "warehouseId,status"

class CsvWarehouseStatusRepository(
    private val filePath: String
) : WarehouseStatusRepository {

    override fun getStatus(warehouseId: String): WarehouseStatus {
        return readStatuses()[warehouseId]
            ?: WarehouseStatus.OPERATIONAL
    }

    override fun updateStatus(
        warehouseId: String,
        status: WarehouseStatus
    ): Boolean {
        val statuses = readStatuses().toMutableMap()
        statuses[warehouseId] = status

        return saveStatuses(statuses)
    }

    private fun readStatuses(): Map<String, WarehouseStatus> {
        val file = File(filePath)

        if (!file.exists()) {
            return emptyMap()
        }

        return file.readLines()
            .drop(HEADER_ROW_INDEX + 1)
            .mapNotNull(::parseStatusRow)
            .toMap()
    }

    private fun parseStatusRow(
        row: String
    ): Pair<String, WarehouseStatus>? {
        val columns = row.split(CSV_SEPARATOR)

        if (columns.size != EXPECTED_COLUMN_COUNT) {
            return null
        }

        val warehouseId = columns[WAREHOUSE_ID_INDEX].trim()

        val status = runCatching {
            WarehouseStatus.valueOf(
                columns[STATUS_INDEX].trim()
            )
        }.getOrNull()

        return status?.let {
            warehouseId to it
        }
    }

    private fun saveStatuses(
        statuses: Map<String, WarehouseStatus>
    ): Boolean {
        return runCatching {
            val file = File(filePath)

            file.parentFile?.mkdirs()

            file.writeText(
                buildString {
                    appendLine(CSV_HEADER)

                    statuses.forEach { (warehouseId, status) ->
                        appendLine(
                            "$warehouseId$CSV_SEPARATOR$status"
                        )
                    }
                }
            )
        }.isSuccess
    }
}

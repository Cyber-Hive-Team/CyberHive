package org.example.data.csv

import org.example.data.dataholder.VehicleRaw
import org.example.data.dataparsing.parseFleetRow
import org.example.domain.repository.VehicleRepository
import org.example.domain.repository.VehicleRepositoryResult
import kotlin.io.path.Path
import kotlin.io.path.exists
import kotlin.io.path.readLines

private const val NUMBER_OF_COLUMNS = 4
private const val HEADER_OFFSET = 1

class CsvVehicleRepository(
    private val filePath: String
) : VehicleRepository {

    override fun getVehicles(): VehicleRepositoryResult {
        val lines = loadFile()
        val vehicles = mutableListOf<VehicleRaw>()
        val warnings = mutableListOf<String>()

        if (lines.isEmpty()) {
            warnings.add("Fleet file was not found or is empty.")

            return VehicleRepositoryResult(
                vehicles = vehicles,
                warnings = warnings
            )
        }

        for (index in 1 until lines.size) {
            val cleanColumns = trimData(lines[index])

            if (!handleParsingErrors(cleanColumns)) {
                warnings.add(
                    "Fleet row ${index + HEADER_OFFSET} has invalid data."
                )
                continue
            }

            val vehicle = parseFleetRow(
                vehicleId = cleanColumns[0],
                currentHubId = cleanColumns[1],
                maxCapacityKgValue = cleanColumns[2],
                costPerKmValue = cleanColumns[3]
            )

            if (vehicle != null) {
                vehicles.add(vehicle)
            } else {
                warnings.add(
                    "Fleet row ${index + HEADER_OFFSET} has missing ID."
                )
            }
        }

        return VehicleRepositoryResult(
            vehicles = vehicles,
            warnings = warnings
        )
    }

    private fun loadFile(): List<String> {
        val path = Path(filePath)

        if (!path.exists()) {
            return emptyList()
        }

        return path.readLines()
    }

    private fun trimData(
        line: String
    ): MutableList<String> {

        val cleanColumns = mutableListOf<String>()

        for (column in line.split(",")) {
            cleanColumns.add(column.trim())
        }

        return cleanColumns
    }

    private fun handleParsingErrors(
        columns: List<String>
    ): Boolean {

        var isValid = true

        if (columns.size != NUMBER_OF_COLUMNS) {
            isValid = false
        }

        return isValid
    }
}
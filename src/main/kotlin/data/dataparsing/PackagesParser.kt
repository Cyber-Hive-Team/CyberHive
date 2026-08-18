package org.example.data.dataparsing

import org.example.data.dataholder.PackageRaw
import org.example.data.dataholder.RawResult
import org.example.domain.model.Priority
import java.io.File

private const val FIRST_DATA_ROW_INDEX = 1
private const val EXPECTED_COLUMN_COUNT = 5

private const val ID_COLUMN_INDEX = 0
private const val WEIGHT_COLUMN_INDEX = 1
private const val ORIGIN_COLUMN_INDEX = 2
private const val DESTINATION_COLUMN_INDEX = 3
private const val PRIORITY_COLUMN_INDEX = 4

private const val INVALID_WEIGHT = -1.0

fun parsePackages(filePath: String): List<RawResult<PackageRaw>> {
    val lines = readPackageLines(filePath)

    val rawPackagesResultList: List<RawResult<PackageRaw>> =
        lines
            .drop(FIRST_DATA_ROW_INDEX)
            .mapIndexed { index, line ->
                parsePackageLine(
                    line = line,
                    lineNumber = index + FIRST_DATA_ROW_INDEX
                )
            }
    return rawPackagesResultList
}

private fun readPackageLines(
    filePath: String,
): List<String> {
    val packagesFile = File(filePath)

    if (!packagesFile.exists()) {
        return emptyList()
    }

    return packagesFile.readLines()
}

private fun parsePackageLine(
    line: String,
    lineNumber: Int
): RawResult<PackageRaw> {

    if (line.isBlank()) {
        return RawResult(
            rawData = null,
            errorMessage = "Package row $lineNumber is empty"
        )
    }

    val columns = splitAndCleanColumns(line)

    if (columns.size < EXPECTED_COLUMN_COUNT) {
        return RawResult(
            rawData = null,
            errorMessage = "Invalid package row $lineNumber: " +
                    "expected $EXPECTED_COLUMN_COUNT columns, got ${columns.size}"
        )
    }

    val packageRaw = createPackageRaw(columns)

    val validationError = validatePackageFields(
        id = packageRaw.id,
        originHubId = packageRaw.originHubId,
        destinationHubId = packageRaw.destinationHubId,
        lineNumber = lineNumber
    )

    if (validationError != null) {
        return RawResult(
            rawData = null,
            errorMessage = validationError
        )
    }

    return RawResult(
        rawData = packageRaw,
        errorMessage = null
    )
}


private fun createPackageRaw(
    columns: List<String>
): PackageRaw {
    return PackageRaw(
        id = columns[ID_COLUMN_INDEX].uppercase(),
        weight = parseWeight(columns[WEIGHT_COLUMN_INDEX]),
        originHubId = columns[ORIGIN_COLUMN_INDEX].uppercase(),
        destinationHubId = columns[DESTINATION_COLUMN_INDEX].uppercase(),
        priority = parsePriority(columns[PRIORITY_COLUMN_INDEX])
    )
}

private fun validatePackageFields(
    id: String,
    originHubId: String,
    destinationHubId: String,
    lineNumber: Int,
): String? {
    if (
        id.isBlank() ||
        originHubId.isBlank() ||
        destinationHubId.isBlank()
    ) {

        return "Warning: package row $lineNumber has missing required fields"


    }

    if (
        !originHubId.startsWith("WH-") ||
        !destinationHubId.startsWith("WH-")
    ) {
        return "Warning: package row $lineNumber has invalid hub ID → " +
                "Origin: '$originHubId', Dest: '$destinationHubId'"


    }

    return null
}

private fun splitAndCleanColumns(
    line: String
): List<String> {
    return line
        .split(",")
        .map { it.trim() }
}

private fun parseWeight(
    value: String
): Double {
    val cleaned = value
        .replace("kg", "", ignoreCase = true)
        .replace(" ", "")
        .trim()

    return cleaned.toDoubleOrNull() ?: INVALID_WEIGHT
}

private fun parsePriority(
    value: String
): Priority {
    return when (value.trim().uppercase()) {
        "URGENT" -> Priority.URGENT
        "STANDARD" -> Priority.STANDARD
        "LOW" -> Priority.LOW
        else -> Priority.LOW
    }
}
package org.example.data.dataparsing

import org.example.data.dataholder.PackageRaw
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

fun parsePackages(filePath: String): List<PackageRaw> {
    val packages = mutableListOf<PackageRaw>()
    val lines = readPackageLines(filePath)

    for (index in FIRST_DATA_ROW_INDEX until lines.size) {
        val packageItem = parsePackageLine(lines[index], index + 1)
        if (packageItem != null) {
            packages.add(packageItem)
        }
    }
    return packages
}

fun readPackageLines(filePath: String): List<String> {
    val packagesFile = File(filePath)
    if (!packagesFile.exists()) {
        println("Warning: packages.csv was not found at: $filePath")
        return emptyList()
    }
    return packagesFile.readLines()
}

fun parsePackageLine(line: String, lineNumber: Int): PackageRaw? {
    if (line.isBlank()) return null

    val columns = splitAndCleanColumns(line)

    if (columns.size < EXPECTED_COLUMN_COUNT) {
        println(
            "Warning: invalid package row $lineNumber skipped " +
                    "(columns = ${columns.size}): $line"
        )
        return null
    }

    val id = columns[ID_COLUMN_INDEX].uppercase()
    val weight = parseWeight(columns[WEIGHT_COLUMN_INDEX])
    val originHubId = columns[ORIGIN_COLUMN_INDEX].uppercase()
    val destinationHubId = columns[DESTINATION_COLUMN_INDEX].uppercase()
    val priority = parsePriority(columns[PRIORITY_COLUMN_INDEX])

    if (!validatePackageFields(id, originHubId, destinationHubId, lineNumber)) {
        return null
    }

    return PackageRaw(
        id = id,
        weight = weight,
        originHubId = originHubId,
        destinationHubId = destinationHubId,
        priority = priority
    )
}

fun validatePackageFields(
    id: String,
    originHubId: String,
    destinationHubId: String,
    lineNumber: Int
): Boolean {
    if (id.isBlank() || originHubId.isBlank() || destinationHubId.isBlank()) {
        println("Warning: package row $lineNumber has missing required fields")
        return false
    }

    if (!originHubId.startsWith("WH-") || !destinationHubId.startsWith("WH-")) {
        println(
            "Warning: package row $lineNumber has invalid hub ID → " +
                    "Origin: '$originHubId', Dest: '$destinationHubId'"
        )
        return false
    }
    return true
}

fun splitAndCleanColumns(line: String): List<String> {
    return line.split(",").map { it.trim() }
}

fun parseWeight(value: String): Double {
    val cleaned = value.replace("kg", "", ignoreCase = true)
        .replace(" ", "")
        .trim()
    return cleaned.toDoubleOrNull() ?: INVALID_WEIGHT
}

fun parsePriority(value: String): Priority {
    return when (value.trim().uppercase()) {
        "URGENT" -> Priority.URGENT
        "STANDARD" -> Priority.STANDARD
        "LOW" -> Priority.LOW
        else -> Priority.LOW
    }
}
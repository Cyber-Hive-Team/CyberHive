package org.example.data.dataparsing

import org.example.data.dataholder.PackageRaw
import org.example.domain.model.Priority
import java.io.File

fun parsePackages(filePath: String): List<PackageRaw> {
    val lines = readPackageLines(filePath)
    return lines.drop(1).mapIndexedNotNull { index, line ->
        parsePackageLine(line, index + 2)
    }
}

private fun readPackageLines(filePath: String): List<String> {
    val packagesFile = File(filePath)
    if (!packagesFile.exists()) {
        println("Warning: packages.csv was not found.")
        return emptyList()
    }
    return packagesFile.readLines()
}

private fun parsePackageLine(line: String, lineNumber: Int): PackageRaw? {
    if (line.isBlank()) return null

    val columns = splitAndCleanColumns(line)

    if (columns.size < 5) {
        println("Warning: invalid package row $lineNumber skipped (columns = ${columns.size}): $line")
        return null
    }

    val id = columns[0].uppercase()
    val weight = parseWeight(columns[1])
    val originHubId = columns[2].uppercase()
    val destinationHubId = columns[3].uppercase()
    val priority = parsePriority(columns[4])

    if (!validatePackageFields(
            id = id,
            originHubId = originHubId,
            destinationHubId = destinationHubId,
            weight = weight,
            lineNumber = lineNumber
        )
    ) {
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

private fun validatePackageFields(
    id: String,
    originHubId: String,
    destinationHubId: String,
    weight: Double,
    lineNumber: Int
): Boolean {
    if (id.isBlank() || originHubId.isBlank() || destinationHubId.isBlank()) {
        println("Warning: package row $lineNumber has missing required fields")
        return false
    }

    if (!originHubId.startsWith("WH-") || !destinationHubId.startsWith("WH-")) {
        println("Warning: package row $lineNumber has invalid hub ID → Origin: '$originHubId', Dest: '$destinationHubId'")
        return false
    }

    if (weight < 0) {
        println("Warning: package row $lineNumber has invalid weight")
        return false
    }

    return true
}

private fun splitAndCleanColumns(line: String): List<String> {
    return line.split(",").map(String::trim)
}

private fun parseWeight(value: String): Double {
    val cleaned = value.replace("kg", "", ignoreCase = true).replace(" ", "").trim()
    return cleaned.toDoubleOrNull() ?: -1.0
}

private fun parsePriority(value: String): Priority {
    return when (value.trim().uppercase()) {
        "URGENT" -> Priority.URGENT
        "STANDARD" -> Priority.STANDARD
        "LOW" -> Priority.LOW
        else -> Priority.LOW
    }
}
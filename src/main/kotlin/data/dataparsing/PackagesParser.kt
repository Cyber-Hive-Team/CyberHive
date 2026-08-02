package org.example.data.dataparsing

import org.example.data.dataholder.PackageRaw
import org.example.domain.model.Priority
import java.io.File

fun parsePackages(): List<PackageRaw> {
    val packages = mutableListOf<PackageRaw>()
    val lines = readPackageLines()

    for (index in 1 until lines.size) {
        val packageItem = parsePackageLine(lines[index], index + 1)
        if (packageItem != null) {
            packages.add(packageItem)
        }
    }
    return packages
}

fun readPackageLines(): List<String> {
    val packagesFile = File("src/main/resources/packages.csv")
    if (!packagesFile.exists()) {
        println("Warning: packages.csv was not found.")
        return emptyList()
    }
    return packagesFile.readLines()
}

fun parsePackageLine(line: String, lineNumber: Int): PackageRaw? {
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

    if (id.isBlank() || originHubId.isBlank() || destinationHubId.isBlank()) {
        println("Warning: package row $lineNumber has missing required fields")
        return null
    }
    if (!originHubId.startsWith("WH-") || !destinationHubId.startsWith("WH-")) {
        println(
            "Warning: package row" + "$lineNumber " +
                    "has invalid hub ID → Origin: " + "'$originHubId'," +
                    " Dest: '$destinationHubId'"
        )
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
fun splitAndCleanColumns(line: String): List<String> {
    return line
        .split(",")
        .map { it.trim() }
}

fun parseWeight(value: String): Double {
    val invalidWeight = -1.0

    val cleaned = value
        .replace("kg", "", ignoreCase = true)
        .replace(" ", "")
        .trim()

    return cleaned.toDoubleOrNull() ?: invalidWeight
}

fun parsePriority(value: String): Priority {
    return when (value.trim().uppercase()) {
        "URGENT" -> Priority.URGENT
        "STANDARD" -> Priority.STANDARD
        "LOW" -> Priority.LOW
        else -> Priority.LOW
    }
}
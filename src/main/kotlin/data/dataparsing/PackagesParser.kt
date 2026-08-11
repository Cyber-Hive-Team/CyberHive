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

    if (columns.size < 4) {
        println(
            "Warning: invalid package row $lineNumber skipped " +
                    "(columns = ${columns.size}): $line"
        )
        return null
    }

    val id = columns[0].uppercase()
    val weight = parseWeight(columns[1])
    val originHubId = columns[2].uppercase()
    val destinationHubId = columns[3].uppercase()


    val rawPriority = columns.getOrNull(4) ?: "STANDARD"
    val priority = parsePriority(rawPriority)


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

    if (!originHubId.startsWith("WH-") ||
        !destinationHubId.startsWith("WH-")
    ) {
        println(
            "Warning: package row $lineNumber has invalid hub ID → " +
                    "Origin: '$originHubId', Dest: '$destinationHubId'"
        )
        return false
    }

    return true
}

fun splitAndCleanColumns(line: String): List<String> {

    return line
        .split(",")
        .map { it.replace("\u00A0", "").trim() }
}

fun parseWeight(value: String): Double {
    val defaultWeight = 1.0

    val cleaned = value
        .replace("kg", "", ignoreCase = true)
        .replace("\u00A0", "")
        .replace(" ", "")
        .trim()

    return cleaned.toDoubleOrNull() ?: defaultWeight
}

fun parsePriority(value: String): Priority {
    return when (value.trim().uppercase()) {
        "URGENT" -> Priority.URGENT
        "STANDARD" -> Priority.STANDARD
        "LOW" -> Priority.LOW
        else -> Priority.STANDARD
    }
}
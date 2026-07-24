package org.example.DataParsing
import org.example.data.dataHolder.PackageRaw
import org.example.data.dataHolder.Priority
import java.io.File
fun parsePackages(): List<PackageRaw> {
    val packages = mutableListOf<PackageRaw>()
    val lines = readPackageLines()
    for (index in 1 until lines.size) {
        val packageItem = parsePackageLine(lines[index])
        if (packageItem != null) {
            packages.add(packageItem)
        } }
    return packages
}
fun readPackageLines(): List<String> {
        val packagesFile = File("src/main/resources/packages.csv")
        if (!packagesFile.exists()) {
            println("Warning: the file was not found.")
            return emptyList()
        }
        return packagesFile.readLines()
    }
fun parsePackageLine(line: String): PackageRaw? {
    if (line.isBlank()) {
        return null }
    val columns = splitAndCleanColumns(line)

    if (!hasValidColumnCount(columns)) {
        println("Warning: invalid package row skipped: $line")
        return null }

    if (hasMissingRequiredFields(columns)) {
        println("Warning: package row has missing required fields: $line")
        return null }
    return createPackageFromColumns(columns)
}

fun hasValidColumnCount(columns: List<String>): Boolean {
    val expectedColumnCount = 4
    return columns.size == expectedColumnCount
}

fun hasMissingRequiredFields(columns: List<String>): Boolean {
    val id = columns[0]
    val destinationHubId = columns[2]
    return id.isBlank() || destinationHubId.isBlank()
}

fun createPackageFromColumns(columns: List<String>): PackageRaw {
    val id = columns[0]
    val weight = parseWeight(columns[1])
    val destinationHubId = columns[2]
    val priority = parsePriority(columns[3])

    return PackageRaw(
        id = id.uppercase(),
        weight = weight,
        destinationHubId = destinationHubId.uppercase(),
        priority = priority)
}
fun splitAndCleanColumns(line: String): List<String> {
    return line
        .split(",")
        .map { column -> column.trim() }
}
fun parseWeight(value: String): Double {
    val invalidWeight = -1.0
    return value.toDoubleOrNull() ?: invalidWeight
}
fun parsePriority(value: String): Priority {
    return when (value.uppercase()) {
        "URGENT" -> Priority.URGENT
        "STANDARD" -> Priority.STANDARD
        "LOW" -> Priority.LOW
        else -> Priority.LOW
    }
}
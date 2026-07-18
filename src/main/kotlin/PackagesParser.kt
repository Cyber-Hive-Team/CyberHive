package org.example

fun parsePackages(): List<Package> {
    val packages = mutableListOf<Package>()
    val lines = readPackageLines()

    for (index in 1 until lines.size) {
        val packageItem = parsePackageLine(lines[index])

        if (packageItem != null) {
            packages.add(packageItem)
        } }
    return packages
}

fun readPackageLines(): List<String> {
    val inputStream =
        object {}.javaClass.getResourceAsStream("/packages.csv")

    if (inputStream == null) {
        println("Warning: packages.csv was not found.")
        return emptyList()
    }

    return inputStream
        .bufferedReader()
        .use { reader -> reader.readLines() }
}

fun parsePackageLine(line: String): Package? {
    val expectedColumnCount = 4

    if (line.isBlank()) {
        return null
    }

    val columns = splitAndCleanColumns(line)

    if (columns.size != expectedColumnCount) {
        println("Warning: invalid package row skipped: $line")
        return null
    }

    val id = columns[0]
    val weight = parseWeight(columns[1], line)
    val destinationHubId = columns[2]
    val priority = parsePriority(columns[3])

    if (id.isBlank() || destinationHubId.isBlank()) {
        println("Warning: package row has missing required fields: $line")
        return null
    }

    return Package(
        id = id.uppercase(),
        weight = weight,
        destinationHubId = destinationHubId.uppercase(),
        priority = priority
    )
}

fun splitAndCleanColumns(line: String): List<String> {
    return line
        .split(",")
        .map { column -> column.trim() }
}

fun parseWeight(
    value: String,
    originalLine: String
): Double {
    val invalidWeight = -1.0
    val weight = value.toDoubleOrNull()

    if (weight == null) {
        println(
            "Warning: invalid package weight. Using $invalidWeight: $originalLine"
        ) }
    return weight ?: invalidWeight
}

fun parsePriority(value: String): Priority {
    return when (value.uppercase()) {
        "URGENT" -> Priority.URGENT
        "STANDARD" -> Priority.STANDARD
        "LOW" -> Priority.LOW
        else -> Priority.LOW
    }
}
package org.example

import java.io.File

fun parseWarehouses(filePath: String): List<WareHouse> {

    val warehouses = mutableListOf<WareHouse>()

    val lines = File(filePath).readLines()

    for (index in 1 until lines.size) {

        val line = lines[index]

        if (line.isBlank()) {
            continue
        }

        val columns = line.split(",")

        if (columns.size != 3) {
            println("Warning: Row ${index + 1} skipped - invalid columns")
            continue
        }

        val id = columns[0].trim()
        val name = columns[1].trim()
        val regionalZone = columns[2].trim()

        if (id.isBlank()) {
            println("Warning: Row ${index + 1} skipped - missing id")
            continue
        }

        warehouses.add(
            WareHouse(
                id = id,
                name = name,
                regionalZone = regionalZone
            )
        )
    }

    return warehouses
}
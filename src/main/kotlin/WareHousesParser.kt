package org.example

import java.io.File

fun parseWarehouses(filePath: String): List<WareHouseRaw> {

    val warehouses = mutableListOf<WareHouseRaw>()

    val csvLines = File(filePath).readLines()

    for (rowIndex  in 1 until csvLines.size) {

        val currentLine  = csvLines[rowIndex]

        if (currentLine.isBlank()) {
            continue
        }


        val columnValues  = currentLine.split(",")

        if (columnValues.size != 3) {
            println("Warning: Row ${rowIndex + 1} skipped - invalid columns")
            continue
        }


        val id = columnValues[0].trim()
        val name = columnValues[1].trim()
        val zoneText = columnValues[2].trim()

        if (id.isBlank()) {
            println("Warning: Row ${rowIndex + 1} skipped - missing id")
            continue
        }
        if (name.isBlank()) {
            println("Warning: Row ${rowIndex + 1} skipped - missing name")
            continue
        }
        val zone = RegionalZone.entries.find{
            it.name.equals(zoneText, ignoreCase = true)
        }
        if (zone==null) {
            println("Warning: Row ${rowIndex + 1} skipped - invalid zone")
            continue
        }

        warehouses.add(
            WareHouseRaw(
                id = id,
                name = name,
                regionalZone =zone
            )
        )
    }

    return warehouses
}
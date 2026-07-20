package org.example.DataParsing
import org.example.Data.WareHouseRaw
import org.example.Data.RegionalZone
import java.io.File
private const val FIRST_DATA_ROW_INDEX = 1
private const val REQUIRED_COLUMNS_COUNT = 3
private const val USER_ROW_NUMBER_OFFSET = 1
private const val ID_COLUMN_INDEX = 0
private const val NAME_COLUMN_INDEX = 1
private const val ZONE_COLUMN_INDEX = 2
// Reads the file and starts processing the rows.
fun parseWarehouse(filePath: String): List<WareHouseRaw> {
    val rows = readFile(filePath)
    return processRows(rows)
}
// Reads all file lines.
fun readFile(filePath: String): List<String> {
    return File(filePath).readLines()
}
// Processes each row and keeps only valid warehouses.
fun processRows(rows: List<String>): List<WareHouseRaw> {
    val warehouses = mutableListOf<WareHouseRaw>()
    for (index in FIRST_DATA_ROW_INDEX until rows.size) {
        val warehouse = getWarehouseFromRow(
            rows[index],
            index
        )
        if (warehouse != null) {
            warehouses.add(warehouse)
        }
    }
    return warehouses
}
// Converts one row into a warehouse
fun getWarehouseFromRow(row: String, rowIndex: Int): WareHouseRaw? {
    if (row.isBlank()) {
        return null
    }
    val columns = row.split(",")
    if (!hasRequiredColumns(columns, rowIndex)) {
        return null
    }
    val zone = convertZone(
        columns[ZONE_COLUMN_INDEX].trim(),
        rowIndex
    ) ?: return null
    val warehouse = extractData(
        columns,
        zone
    )
    if (!isValidData(warehouse, rowIndex)) {
        return null
    }
    return warehouse
}
// Checks if the row contains the required number of columns.
fun hasRequiredColumns(columns: List<String>, rowIndex: Int): Boolean {
    if (columns.size != REQUIRED_COLUMNS_COUNT) {
        println(
            "Warning: Row ${getUserRowNumber(rowIndex)} skipped - invalid columns"
        )
        return false
    }
    return true
}
// Creates warehouse data from columns.
fun extractData(columns: List<String>, zone: RegionalZone): WareHouseRaw {
    return WareHouseRaw(
        id = columns[ID_COLUMN_INDEX].trim(),
        name = columns[NAME_COLUMN_INDEX].trim(),
        regionalZone = zone
    )
}
// Validates required warehouse fields.
fun isValidData(warehouse: WareHouseRaw, rowIndex: Int): Boolean {
    if (warehouse.id.isBlank()) {
        println(
            "Warning: Row ${getUserRowNumber(rowIndex)} skipped - missing id"
        )
        return false
    }
    if (warehouse.name.isBlank()) {
        println(
            "Warning: Row ${getUserRowNumber(rowIndex)} skipped - missing name"
        )
        return false
    }
    return true
}
// Converts text zone into RegionalZone enum.
fun convertZone(
    zoneText: String,
    rowIndex: Int
): RegionalZone? {
    val zone = RegionalZone.entries.find {
        it.name.equals(
            zoneText,
            ignoreCase = true
        )
    }
    if (zone == null) {
        println(
            "Warning: Row ${getUserRowNumber(rowIndex)} skipped - invalid zone"
        )
    }
    return zone
}
// Converts internal index into user row number.
fun getUserRowNumber(rowIndex: Int): Int {
    return rowIndex + USER_ROW_NUMBER_OFFSET
}
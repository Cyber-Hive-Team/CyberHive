package org.example.data.dataparsing

import org.example.data.dataholder.VehicleRaw

fun parseFleetRow(
    vehicleId: String,
    currentHubId: String,
    maxCapacityKgValue: String,
    costPerKmValue: String
): VehicleRaw? {

    if (vehicleId.isBlank() || currentHubId.isBlank()) {
        return null
    }
    val maxCapacityKg = parseNumericValue(maxCapacityKgValue)
    val costPerKm = parseNumericValue(costPerKmValue)
    return VehicleRaw(
        vehicleId, currentHubId,
        maxCapacityKg, costPerKm
    )
}

private fun parseNumericValue(
    value: String
): Double {
    var number = -1.0
    if (value.isNotBlank()) {
        number = value.toDoubleOrNull() ?: -1.0
    }
    return number
}

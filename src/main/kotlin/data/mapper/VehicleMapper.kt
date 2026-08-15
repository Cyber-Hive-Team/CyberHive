package org.example.data.mapper

import org.example.data.dataholder.VehicleMappingResult
import org.example.data.dataholder.VehicleRaw
import org.example.domain.model.Vehicle
import org.example.domain.model.Warehouse

private const val MIN_CAPACITY_KG = 0.0
private const val MIN_COST_PER_KM = 0.0

class VehicleMapper(
    private val warehouseMap: Map<String, Warehouse>
) {

    fun map(raw: VehicleRaw): VehicleMappingResult {
        val warnings = validate(raw)

        val currentHub = warehouseMap[raw.currentHubId]

        if (currentHub == null) {
            warnings.add(
                "Warning: Vehicle ${raw.id} skipped - " +
                        "warehouse not found: ${raw.currentHubId}"
            )
        }

        if (warnings.isNotEmpty()) {
            return VehicleMappingResult(
                vehicle = null,
                warnings = warnings
            )
        }

        return VehicleMappingResult(
            vehicle = createVehicle(raw, currentHub!!),
            warnings = emptyList()
        )
    }

    private fun createVehicle(
        raw: VehicleRaw,
        currentHub: Warehouse
    ): Vehicle {
        return Vehicle(
            id = raw.id,
            currentHub = currentHub,
            maxCapacityKg = raw.maxCapacityKg,
            costPerKm = raw.costPerKm
        )
    }

    private fun validate(
        raw: VehicleRaw
    ): MutableList<String> {
        val warnings = mutableListOf<String>()

        if (raw.id.isBlank()) {
            warnings.add(
                "Warning: Vehicle skipped - missing id"
            )
        }

        if (raw.currentHubId.isBlank()) {
            warnings.add(
                "Warning: Vehicle ${raw.id} skipped - " +
                        "missing hub id"
            )
        }

        if (raw.maxCapacityKg <= MIN_CAPACITY_KG) {
            warnings.add(
                "Warning: Vehicle ${raw.id} skipped - " +
                        "invalid capacity"
            )
        }

        if (raw.costPerKm < MIN_COST_PER_KM) {
            warnings.add(
                "Warning: Vehicle ${raw.id} skipped - " +
                        "invalid cost per km"
            )
        }

        return warnings
    }
}
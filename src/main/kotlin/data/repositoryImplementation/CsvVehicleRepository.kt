package org.example.data.repository

import org.example.data.dataholder.VehicleRaw
import org.example.data.datasource.VehicleDataSource
import org.example.data.mapper.VehicleMapper
import org.example.domain.model.Result
import org.example.domain.model.Vehicle
import org.example.domain.model.Warehouse
import org.example.domain.repository.VehicleRepository

private const val MIN_CAPACITY_KG = 0.0
private const val MIN_COST_PER_KM = 0.0

class CsvVehicleRepository(
    private val dataSource: VehicleDataSource,
    private val mapper: VehicleMapper,
    private val warehouseMap: Map<String, Warehouse>
) : VehicleRepository {

    override fun getVehicles(): Result<List<Vehicle>> {
        val rawResults = dataSource.getVehicles()
        val vehicles = mutableListOf<Vehicle>()
        val warnings = rawResults.mapNotNull { it.errorMessage }.toMutableList()
        val rawVehicles = rawResults.mapNotNull { it.rawData }
        rawVehicles.forEach { raw ->
            val currentHub = warehouseMap[raw.currentHubId]
            val validationWarnings = validate(raw, currentHub)

            if (validationWarnings.isEmpty()) {
                vehicles.add(
                    mapper.map(
                        raw,
                        currentHub!!
                    )
                )
            } else {
                warnings.addAll(validationWarnings)
            }
        }


        return Result(
            data = vehicles,
            errorMessage = warnings
                .takeIf { it.isNotEmpty() }
                ?.joinToString("; ")
        )
    }

    private fun validate(
        raw: VehicleRaw,
        currentHub: Warehouse?
    ): List<String> {
        return listOfNotNull(
            validateId(raw),
            validateHubId(raw),
            validateWarehouse(raw, currentHub),
            validateCapacity(raw),
            validateCost(raw)
        )
    }

    private fun validateId(raw: VehicleRaw): String? =
        if (raw.id.isBlank()) {
            "Warning: Vehicle skipped - missing id"
        } else {
            null
        }

    private fun validateHubId(raw: VehicleRaw): String? =
        if (raw.currentHubId.isBlank()) {
            "Warning: Vehicle ${raw.id} skipped - missing hub id"
        } else {
            null
        }

    private fun validateWarehouse(
        raw: VehicleRaw,
        currentHub: Warehouse?
    ): String? =
        if (currentHub == null) {
            "Warning: Vehicle ${raw.id} skipped - " +
                    "warehouse not found: ${raw.currentHubId}"
        } else {
            null
        }

    private fun validateCapacity(raw: VehicleRaw): String? =
        if (raw.maxCapacityKg <= MIN_CAPACITY_KG) {
            "Warning: Vehicle ${raw.id} skipped - invalid capacity"
        } else {
            null
        }

    private fun validateCost(raw: VehicleRaw): String? =
        if (raw.costPerKm < MIN_COST_PER_KM) {
            "Warning: Vehicle ${raw.id} skipped - invalid cost per km"
        } else {
            null
        }
}
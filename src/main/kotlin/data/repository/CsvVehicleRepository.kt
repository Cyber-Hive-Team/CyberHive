package org.example.data.repository

import org.example.data.dataholder.VehicleRaw
import org.example.data.datasource.VehicleDataSource
import org.example.data.mapper.VehicleMapper
import org.example.domain.model.Vehicle
import org.example.domain.model.Warehouse
import org.example.domain.repository.Result
import org.example.domain.repository.VehicleRepository

private const val MIN_CAPACITY_KG = 0.0
private const val MIN_COST_PER_KM = 0.0

class CsvVehicleRepository(
    private val dataSource: VehicleDataSource,
    private val mapper: VehicleMapper,
    private val warehouseMap: Map<String, Warehouse>
) : VehicleRepository {

    override fun getVehicles(): Result<List<Vehicle>> {
        val result = dataSource.getVehicles()
        val vehicles = mutableListOf<Vehicle>()
        val warnings = result.warnings.toMutableList()

        result.vehicles.forEach { rawVehicle ->
            val currentHub = warehouseMap[rawVehicle.currentHubId]
            val validationWarnings = validate(rawVehicle, currentHub)

            if (validationWarnings.isEmpty()) {
                vehicles.add(
                    mapper.map(
                        raw = rawVehicle,
                        currentHub = currentHub!!
                    )
                )
            } else {
                warnings.addAll(validationWarnings)
            }
        }

        val errorMessage = warnings
            .takeIf { it.isNotEmpty() }
            ?.joinToString("; ")

        return Result(
            data = vehicles,
            errorMessage = errorMessage
        )
    }

    private fun validate(
        raw: VehicleRaw,
        currentHub: Warehouse?
    ): List<String> {
        val warnings = mutableListOf<String>()

        if (raw.id.isBlank()) {
            warnings.add(
                "Warning: Vehicle skipped - missing id"
            )
        }

        if (raw.currentHubId.isBlank()) {
            warnings.add(
                "Warning: Vehicle ${raw.id} skipped - missing hub id"
            )
        }

        if (currentHub == null) {
            warnings.add(
                "Warning: Vehicle ${raw.id} skipped - " +
                        "warehouse not found: ${raw.currentHubId}"
            )
        }

        if (raw.maxCapacityKg <= MIN_CAPACITY_KG) {
            warnings.add(
                "Warning: Vehicle ${raw.id} skipped - invalid capacity"
            )
        }

        if (raw.costPerKm < MIN_COST_PER_KM) {
            warnings.add(
                "Warning: Vehicle ${raw.id} skipped - invalid cost per km"
            )
        }

        return warnings
    }
}
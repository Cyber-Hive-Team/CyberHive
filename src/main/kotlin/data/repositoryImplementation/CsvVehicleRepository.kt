package org.example.data.repository

import org.example.data.dataholder.VehicleRaw
import org.example.data.datasource.VehicleDataSource
import org.example.data.mapper.VehicleMapper
import org.example.domain.model.result.Result
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
    private val vehicles = mutableListOf<Vehicle>()
    private var isLoaded = false

    override fun getVehicles(): Result<List<Vehicle>> {
        if (isLoaded) {
            return Result(data = vehicles.toList(), errorMessage = null)
        }
        val rawResults = dataSource.getVehicles()
        val warnings = rawResults
                .mapNotNull { it.errorMessage }
                .toMutableList()
        val rawVehicles = rawResults.mapNotNull { it.rawData }
        rawVehicles.forEach { raw ->
            val currentHub = warehouseMap[raw.currentHubId]
            val validationWarnings = validate(raw, currentHub)
            if (validationWarnings.isEmpty()) {
                vehicles.add(
                    mapper.map(raw, currentHub!!)
                )
            } else {
                warnings.addAll(validationWarnings)
            }
        }
        isLoaded = true
        return Result(
            data = vehicles.toList(),
            errorMessage = warnings
                .takeIf { it.isNotEmpty() }
                ?.joinToString("; ")
        )
    }

    override fun getVehicleById(vehicleId: String): Vehicle? {

        return getVehicles().data.firstOrNull { it.id == vehicleId }
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
    override fun getVehiclesByWarehouseId(
        warehouseId: String
    ): Result<List<Vehicle>> {

        val result = getVehicles()

        return Result(
            data = result.data.filter { vehicle ->
                vehicle.currentHub.id == warehouseId
            },
            errorMessage = result.errorMessage
        )
    }

    override fun reassignVehicle(vehicleId: String, warehouseId: String): Boolean {
        getVehicles()
        val index =
            vehicles.indexOfFirst { vehicle ->
                vehicle.id == vehicleId
            }
        if (index == -1) {
            return false
        }
        val targetWarehouse =
            warehouseMap[warehouseId]
                ?: return false
        vehicles[index] =
            vehicles[index].copy(
                currentHub = targetWarehouse
            )
        return true
    }

    override fun removeVehicle(vehicleId: String): Boolean {
        getVehicles()

        return vehicles.removeIf { it.id == vehicleId }
    }
}

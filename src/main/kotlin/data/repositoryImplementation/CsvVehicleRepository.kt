package org.example.data.repository

import org.example.data.datasource.VehicleDataSource
import org.example.data.mapper.VehicleMapper
import org.example.data.validation.VehicleValidator
import org.example.domain.model.Vehicle
import org.example.domain.model.Warehouse
import org.example.domain.model.result.Result
import org.example.domain.repository.VehicleRepository

class CsvVehicleRepository(
    private val dataSource: VehicleDataSource,
    private val mapper: VehicleMapper,
    private val warehouseMap: Map<String, Warehouse>,
    private val validator: VehicleValidator

) : VehicleRepository {
    private val vehicles = mutableListOf<Vehicle>()
    private var isLoaded = false

    @Suppress("TooGenericExceptionCaught", "LongMethod", "ReturnCount")
    override fun getVehicles(): Result<List<Vehicle>> {
        return try {
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
            val validationWarnings = validator.validate(raw, currentHub)
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
        } catch (e: Exception) {
            Result(data = emptyList(), errorMessage = "Failed to load vehicles: ${e.message}")
        }

    }

    override fun getVehicleById(vehicleId: String): Vehicle? {

        return getVehicles().data.firstOrNull { it.id == vehicleId }
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

package org.example.data.repository

import org.example.data.datasource.VehicleDataSource
import org.example.data.mapper.VehicleMapper
import org.example.domain.model.Vehicle
import org.example.domain.repository.VehicleRepository
import org.example.domain.repository.VehicleRepositoryResult

class CsvVehicleRepository(
    private val dataSource: VehicleDataSource,
    private val mapper: VehicleMapper
) : VehicleRepository {

    override fun getVehicles(): VehicleRepositoryResult {
        val result = dataSource.getVehicles()
        val vehicles = mutableListOf<Vehicle>()
        val warnings = result.warnings.toMutableList()

        result.vehicles.forEach { rawVehicle ->
            val mapped = mapper.map(rawVehicle)

            warnings.addAll(mapped.warnings)

            mapped.vehicle?.let {
                vehicles.add(it)
            }
        }

        return VehicleRepositoryResult(
            vehicles = vehicles,
            warnings = warnings
        )
    }
}
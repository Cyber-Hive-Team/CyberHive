package org.example.data.mapper.csv

import org.example.data.datasource.local.model.VehicleLocalData
import org.example.domain.model.Vehicle
import org.example.data.exception.NullRequiredFieldException

fun VehicleLocalData.toDomainModel(): Vehicle {

    return Vehicle(
        id = vehicleRaw.id,
        currentHub = currentWarehouse.toDomainModel(),
        maxCapacityKg = vehicleRaw.maxCapacityKg
            ?: throw NullRequiredFieldException(
                "Vehicle '${vehicleRaw.id}' has null maxCapacityKg"
            ),
        costPerKm = vehicleRaw.costPerKm
    )
}

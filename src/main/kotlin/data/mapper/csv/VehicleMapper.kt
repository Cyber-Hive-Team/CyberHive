package org.example.data.mapper.csv

import org.example.data.datasource.local.model.VehicleLocalData
import org.example.domain.model.Vehicle

fun VehicleLocalData.toDomainModel(): Vehicle {

    return Vehicle(
        id = vehicleRaw.id,
        currentHub = currentWarehouse.toDomainModel(),
        maxCapacityKg = vehicleRaw.maxCapacityKg,
        costPerKm = vehicleRaw.costPerKm
    )
}

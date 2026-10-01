package org.example.data.mapper.csv

import org.example.data.dataholder.VehicleRaw
import org.example.domain.model.Vehicle
import org.example.domain.model.Warehouse


fun VehicleRaw.toDomainModel(currentHub: Warehouse): Vehicle {
        return Vehicle(
            id = id,
            currentHub = currentHub,
            maxCapacityKg = maxCapacityKg,
            costPerKm = costPerKm
        )
    }

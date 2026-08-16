package org.example.data.mapper

import org.example.data.dataholder.VehicleRaw
import org.example.domain.model.Vehicle
import org.example.domain.model.Warehouse

class VehicleMapper {

    fun map(
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
}
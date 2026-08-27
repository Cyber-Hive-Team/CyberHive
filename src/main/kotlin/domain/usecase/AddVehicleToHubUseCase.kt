package org.example.domain.usecase

import org.example.domain.model.Vehicle
import org.example.domain.model.Warehouse

class AddVehicleToHubUseCase {

    operator fun invoke(vehicle: Vehicle, warehouse: Warehouse) {
        warehouse.addVehicles(listOf(vehicle))
    }
}
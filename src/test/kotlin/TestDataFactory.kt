package org.example.test

import org.example.domain.model.Package
import org.example.domain.model.Priority
import org.example.domain.model.RegionalZone
import org.example.domain.model.Route
import org.example.domain.model.Warehouse
import org.example.domain.model.Vehicle

class TestDataFactory {

    @Suppress("LongParameterList")
    fun createPackage(
        baseRate: Double = 10.0,
        id: String = "PKG-000001",
        weight: Double = 5.0,
        priority: Priority = Priority.STANDARD,
        origin: Warehouse = createWarehouse("WH-001"),
        destination: Warehouse = createWarehouse("WH-002")
    ): Package = Package(
        id = id,
        weight = weight,
        priority = priority,
        originWarehouse = origin,
        destinationWarehouse = destination,
        baseRate = baseRate
    )

    fun createWarehouse(id: String): Warehouse = Warehouse(
        id = id,
        name = "Warehouse $id",
        regionalZone = RegionalZone.NORTH,
        latitude = 31.5,
        longitude = 34.5
    )

    fun createRoute(cargoPackage: Package): Route = Route(
        id = "RT-00001",
        distanceKm = 50.0,
        typicalDelayMin = 10,
        originWarehouse = cargoPackage.originWarehouse,
        destinationWarehouse = cargoPackage.destinationWarehouse
    )



    fun createVehicle(
        id: String = "TRK-0001",
        maxCapacityKg: Double = 100.0,
        costPerKm: Double = 2.5,
        currentHub: Warehouse = createWarehouse("WH-001")
    ): Vehicle = Vehicle(
        id = id,
        maxCapacityKg = maxCapacityKg,
        costPerKm = costPerKm,
        currentHub = currentHub
    )
}

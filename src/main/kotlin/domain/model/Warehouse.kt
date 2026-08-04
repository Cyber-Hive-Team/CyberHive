package org.example.domain.model

import domain.sorting.sortCargoQueueByWeightDescending
import org.example.sorting.sortPackagesByPriorityThenWeight

class Warehouse(
    val id: String,
    val name: String,
    val regionalZone: RegionalZone,
    val latitude: Double,
    val longitude: Double
) {
    private val cargoQueue = mutableListOf<Package>()
    private val outgoingRoutes = mutableListOf<Route>()
    private val stationedVehicles = mutableListOf<Vehicle>()

    fun addPackage(packageItem: Package) {
        cargoQueue.add(packageItem)
    }

    fun addRoute(route: Route) {
        outgoingRoutes.add(route)
    }

    fun addVehicle(vehicle: Vehicle) {
        stationedVehicles.add(vehicle)
    }

    fun getCargoQueue(): List<Package> {
        return cargoQueue.toList()
    }

    fun getOutgoingRoutes(): List<Route> {
        return outgoingRoutes.toList()
    }

    fun getStationedVehicles(): List<Vehicle> {
        return stationedVehicles.toList()
    }
    fun sortCargoQueue() {
        sortCargoQueueByWeightDescending(cargoQueue)
    }

    fun sortCargoByPriorityThenWeight() {
        sortPackagesByPriorityThenWeight(cargoQueue)
    }
}

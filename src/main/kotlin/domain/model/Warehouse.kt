package org.example.domain.model

import org.example.domain.algorithm.sorting.sortCargoQueueDescendingByWeight
import org.example.domain.algorithm.sorting.sortPackagesByPriorityThenWeight

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

    fun addPackages(packages: List<Package>) {
        cargoQueue.addAll(packages)
    }

    fun addRoutes(routes: List<Route>) {
        outgoingRoutes.addAll(routes)
    }

    fun addVehicles(vehicles: List<Vehicle>) {
        stationedVehicles.addAll(vehicles)
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
        sortCargoQueueDescendingByWeight(cargoQueue)
    }

    fun sortCargoByPriorityThenWeight() {
        sortPackagesByPriorityThenWeight(cargoQueue)
    }
    fun replacePackageInQueue(
        packageId: String,
        updatedPackage: Package
    ): Boolean {

        val packageIndex = cargoQueue.indexOfFirst {
            it.id == packageId
        }

        if (packageIndex == -1) {
            return false
        }

        cargoQueue[packageIndex] = updatedPackage

        return true
    }
}

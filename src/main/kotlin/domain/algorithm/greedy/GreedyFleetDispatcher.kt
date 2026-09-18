package org.example.domain.algorithm.greedy

import org.example.domain.model.RegionalZone
import org.example.domain.model.Vehicle


class GreedyFleetDispatcher {


    fun dispatch(
        targetZones: Set<RegionalZone>,
        availableVehicles: List<Vehicle>
    ): List<Vehicle> {

        val uncoveredZones =
            targetZones.toMutableSet()

        val dispatchedVehicles =
            mutableListOf<Vehicle>()


        while (uncoveredZones.isNotEmpty()) {

            val vehicleWithMaximumCoverage =
                findVehicleWithMaximumCoverage(
                    availableVehicles,
                    dispatchedVehicles,
                    uncoveredZones
                )


            if (
                vehicleWithMaximumCoverage == null
            ) {
                break
            }


            dispatchedVehicles.add(
                vehicleWithMaximumCoverage
            )


            uncoveredZones.removeAll(
                getVehicleCoveredZones(
                    vehicleWithMaximumCoverage
                )
            )
        }


        return dispatchedVehicles
    }


    private fun findVehicleWithMaximumCoverage(
        availableVehicles: List<Vehicle>,
        dispatchedVehicles: List<Vehicle>,
        uncoveredZones: Set<RegionalZone>
    ): Vehicle? {

        return availableVehicles
            .asSequence()
            .filterNot {
                it in dispatchedVehicles
            }
            .maxByOrNull { vehicle ->

                getVehicleCoveredZones(vehicle)
                    .count {
                        it in uncoveredZones
                    }
            }
            ?.takeIf { vehicle ->

                getVehicleCoveredZones(vehicle)
                    .any {
                        it in uncoveredZones
                    }
            }
    }


    private fun getVehicleCoveredZones(
        vehicle: Vehicle
    ): Set<RegionalZone> {

        return setOf(
            vehicle.currentHub.regionalZone
        )
    }
}

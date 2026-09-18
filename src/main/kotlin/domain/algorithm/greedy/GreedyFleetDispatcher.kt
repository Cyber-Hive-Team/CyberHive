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
                availableVehicles
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


            if (
                vehicleWithMaximumCoverage == null ||
                getVehicleCoveredZones(vehicleWithMaximumCoverage)
                    .none {
                        it in uncoveredZones
                    }
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


    private fun getVehicleCoveredZones(
        vehicle: Vehicle
    ): Set<RegionalZone> {

        return setOf(
            vehicle.currentHub.regionalZone
        )
    }
}

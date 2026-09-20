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

        // The greedy algorithm checks the available vehicles and
        // chooses the vehicle that covers the most uncovered zones.
        // In the worst case, it may check N vehicles for each of N
        // selections, so the time complexity is O(N^2).
        //
        // A brute-force search would try every possible combination
        // of the N vehicles. Each vehicle can be selected or not selected,
        // giving 2^N possible combinations. Therefore, its time complexity
        // can be O(2^N), which grows much faster than O(N^2).

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


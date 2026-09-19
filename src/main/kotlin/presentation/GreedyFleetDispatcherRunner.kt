package org.example.presentation

import org.example.domain.model.RegionalZone
import org.example.domain.usecase.DispatchFleetGreedyUseCase


class GreedyFleetDispatcherRunner(
    private val dispatchFleetGreedyUseCase: DispatchFleetGreedyUseCase
) {


    suspend fun run() {

        println("\n=== Greedy Fleet Dispatcher ===")


        val targetZones =
            setOf(
                RegionalZone.NORTH,
                RegionalZone.SOUTH
            )


        dispatchFleetGreedyUseCase(targetZones)
            .onSuccess { vehicles ->

                println(
                    "Selected vehicles:"
                )


                vehicles.forEach { vehicle ->

                    println(
                        "Vehicle: ${vehicle.id} | " +
                                "Zone: ${vehicle.currentHub.regionalZone}"
                    )
                }
            }
            .onFailure { error ->

                println(
                    "Fleet dispatch failed: ${error.message}"
                )
            }
    }
}

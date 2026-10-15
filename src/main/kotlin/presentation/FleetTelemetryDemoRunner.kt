package org.example.presentation

import kotlinx.coroutines.flow.take
import org.example.domain.model.result.FleetTrackingEvent
import org.example.domain.usecase.TrackActiveFleetUseCase

class FleetTelemetryDemoRunner(
    private val trackActiveFleetUseCase: TrackActiveFleetUseCase
) {

    suspend fun run(vehicleIds: List<String>) {
        if (vehicleIds.isEmpty()) {
            println("No vehicles available.")
            return
        }

        println("=== Fleet Telemetry Demo ===")
        trackActiveFleetUseCase(vehicleIds)
            .take(12)
            .collect { event ->
                when (event) {
                    is FleetTrackingEvent.TelemetryUpdated -> {
                        val telemetry = event.telemetry
                        println(
                            "Vehicle: ${telemetry.vehicleId} | " +
                                    "Speed: ${telemetry.speedKmh} km/h | " +
                                    "Fuel: ${telemetry.fuelPercentage}% | " +
                                    "GPS: ${telemetry.latitude}, ${telemetry.longitude}"
                        )
                    }

                    is FleetTrackingEvent.PotentialDelayDetected -> {
                        println("Potential delay detected: ${event.vehicleId}")
                    }
                }
            }

        println("=== Demo Finished ===")
    }
}

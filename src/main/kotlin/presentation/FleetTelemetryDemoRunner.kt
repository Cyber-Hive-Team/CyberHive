package org.example.presentation

import kotlinx.coroutines.flow.take
import org.example.domain.model.result.FleetTrackingEvent
import org.example.domain.usecase.TrackActiveFleetUseCase

class FleetTelemetryDemoRunner(
    private val trackActiveFleetUseCase: TrackActiveFleetUseCase
) {
    private companion object {
        const val DEMO_EVENT_LIMIT = 12
    }
    suspend fun run(vehicleIds: List<String>) {
        if (vehicleIds.isEmpty()) {
            println("No vehicles available.")
            return
        }

        println("=== Fleet Telemetry Demo ===")
        trackActiveFleetUseCase(vehicleIds)
            .take(DEMO_EVENT_LIMIT)
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

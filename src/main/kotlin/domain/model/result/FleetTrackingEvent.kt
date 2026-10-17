package org.example.domain.model.result

import org.example.domain.model.VehicleTelemetry

sealed interface FleetTrackingEvent {
    data class TelemetryUpdated(val telemetry: VehicleTelemetry) : FleetTrackingEvent
    data class PotentialDelayDetected(val vehicleId: String, val detectedAtMillis: Long) : FleetTrackingEvent

}

package org.example.domain.usecase

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.flow.flow
import org.example.domain.model.VehicleTelemetry
import org.example.domain.model.result.FleetTrackingEvent
import org.example.domain.repository.VehicleRepository

class TrackActiveFleetUseCase(
    private val vehicleRepository: VehicleRepository
) {

    operator fun invoke(vehicleIds: List<String>): Flow<FleetTrackingEvent> = flow {
        val stoppedSince = mutableMapOf<String, Long>()
        val alertedVehicles = mutableSetOf<String>()
        vehicleRepository.streamTelemetry(vehicleIds)
            .filter { telemetry -> isValidTelemetry(telemetry) }
            .collect { telemetry ->
                emit(FleetTrackingEvent.TelemetryUpdated(telemetry))
                if (telemetry.speedKmh > 0.0) {
                    stoppedSince.remove(telemetry.vehicleId)
                    alertedVehicles.remove(telemetry.vehicleId)
                } else {
                    val stoppedAt = stoppedSince.getOrPut(telemetry.vehicleId) {
                        telemetry.timestampMillis
                    }
                    val stoppedDuration = telemetry.timestampMillis - stoppedAt
                    if (stoppedDuration >= DELAY_THRESHOLD_MS && telemetry.vehicleId !in alertedVehicles) {
                        emit(
                            FleetTrackingEvent.PotentialDelayDetected(
                                vehicleId = telemetry.vehicleId,
                                detectedAtMillis = telemetry.timestampMillis
                            )
                        )
                        alertedVehicles.add(telemetry.vehicleId)
                    }
                }
            }
    }

    private fun isValidTelemetry(
        telemetry: VehicleTelemetry
    ): Boolean {
        return telemetry.vehicleId.isNotBlank() &&
                telemetry.timestampMillis >= 0L &&
                telemetry.speedKmh.isFinite() &&
                telemetry.speedKmh >= 0.0 &&
                telemetry.fuelPercentage.isFinite() &&
                telemetry.fuelPercentage in 0.0..100.0 &&
                telemetry.latitude.isFinite() &&
                telemetry.latitude in -90.0..90.0 &&
                telemetry.longitude.isFinite() &&
                telemetry.longitude in -180.0..180.0
    }

    private companion object {
        const val DELAY_THRESHOLD_MS = 10_000L
    }
}


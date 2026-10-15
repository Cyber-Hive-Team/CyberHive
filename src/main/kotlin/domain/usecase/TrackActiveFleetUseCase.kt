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
                telemetry.timestampMillis >= MIN_TIMESTAMP_MS &&
                telemetry.speedKmh.isFinite() &&
                telemetry.speedKmh >= MIN_SPEED_KMH &&
                telemetry.fuelPercentage.isFinite() &&
                telemetry.fuelPercentage in MIN_FUEL_PERCENTAGE..MAX_FUEL_PERCENTAGE &&
                telemetry.latitude.isFinite() &&
                telemetry.latitude in MIN_LATITUDE..MAX_LATITUDE &&
                telemetry.longitude.isFinite() &&
                telemetry.longitude in MIN_LONGITUDE..MAX_LONGITUDE
    }

    private companion object {
        const val DELAY_THRESHOLD_MS = 10_000L
        const val MIN_TIMESTAMP_MS = 0L
        const val MIN_SPEED_KMH = 0.0

        const val MIN_FUEL_PERCENTAGE = 0.0
        const val MAX_FUEL_PERCENTAGE = 100.0

        const val MIN_LATITUDE = -90.0
        const val MAX_LATITUDE = 90.0

        const val MIN_LONGITUDE = -180.0
        const val MAX_LONGITUDE = 180.0
    }
}


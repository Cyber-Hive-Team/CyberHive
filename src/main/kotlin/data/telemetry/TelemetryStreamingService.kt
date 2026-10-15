package org.example.data.telemetry

import kotlin.math.cos
import kotlin.math.sin
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import org.example.domain.model.Vehicle
import org.example.domain.model.VehicleTelemetry

class TelemetryStreamingService(
    private val updateIntervalMillis: Long = DEFAULT_UPDATE_INTERVAL_MS
) {

    companion object {
        private const val DEFAULT_UPDATE_INTERVAL_MS = 2000L
        private const val MILLIS_PER_HOUR = 3_600_000.0
        private const val KM_PER_DEGREE_LATITUDE = 111.32

        private const val MIN_SPEED_KMH = 30.0
        private const val MAX_SPEED_KMH = 70.0
        private const val FUEL_PERCENTAGE_POINTS_PER_KM = 0.25

        private const val MILLIS_PER_SECOND = 1000.0
        private const val SPEED_CHANGE_KMH_PER_SECOND = 3.0
        private const val SPEED_VARIATION_KMH = 8.0
        private const val INITIAL_FUEL_PERCENTAGE = 50.0
        private const val FUEL_SEED_DIVISOR = 43L
        private const val FUEL_SEED_MODULUS = 46L

        private const val HEADING_SEED_DIVISOR = 7L
        private const val HEADING_SEED_MODULUS = 360L

        private const val SPEED_SEED_MODULUS = 41L
    }

    init {
        require(updateIntervalMillis > 0) { "Update interval must be greater than zero" }
    }

    fun stream(vehicles: List<Vehicle>): Flow<VehicleTelemetry> = flow {
        val states = vehicles.distinctBy { it.id }
            .associate { vehicle -> vehicle.id to createInitialState(vehicle) }.toMutableMap()
        if (states.isEmpty()) {
            return@flow
        }
        while (true) {
            for ((vehicleId, state) in states) {
                emit(
                    VehicleTelemetry(
                        vehicleId = vehicleId,
                        latitude = state.latitude,
                        longitude = state.longitude,
                        speedKmh = state.speedKmh,
                        fuelPercentage = state.fuelPercentage,
                        timestampMillis = System.currentTimeMillis()
                    )
                )
            }
            delay(updateIntervalMillis)
            states.replaceAll { _, state -> simulateNextState(state) }
        }
    }

    private fun createInitialState(vehicle: Vehicle): VehicleSimulationState {
        val seed = vehicle.id.hashCode().toUInt().toLong()
        return VehicleSimulationState(
            latitude = vehicle.currentHub.latitude,
            longitude = vehicle.currentHub.longitude,
            speedKmh = 0.0,
            fuelPercentage = INITIAL_FUEL_PERCENTAGE + (seed / FUEL_SEED_DIVISOR % FUEL_SEED_MODULUS),
            headingDegrees = (seed / HEADING_SEED_DIVISOR % HEADING_SEED_MODULUS).toDouble(),
            cruisingSpeedKmh = MIN_SPEED_KMH + seed % SPEED_SEED_MODULUS
        )
    }


    private fun simulateNextState(state: VehicleSimulationState): VehicleSimulationState {
        if (state.fuelPercentage <= 0.0) {
            return state.copy(speedKmh = 0.0)
        }
        val newSpeed = calculateNextSpeed(state)
        val requestedDistance = calculateDistance(state.speedKmh, newSpeed)
        val actualDistance = minOf(requestedDistance, state.fuelPercentage / FUEL_PERCENTAGE_POINTS_PER_KM)
        val actualSpeed = if (actualDistance < requestedDistance) 0.0 else newSpeed
        val (newLatitude, newLongitude) = calculateNextPosition(state, actualDistance)
        return state.copy(
            latitude = newLatitude,
            longitude = newLongitude,
            speedKmh = actualSpeed,
            fuelPercentage = calculateRemainingFuel(state.fuelPercentage, actualDistance)
        )
    }

    private fun calculateNextSpeed(state: VehicleSimulationState): Double {
        val speedVariation = kotlin.random.Random.nextDouble(-SPEED_VARIATION_KMH, SPEED_VARIATION_KMH)
        val targetSpeed = (state.cruisingSpeedKmh + speedVariation).coerceIn(0.0, MAX_SPEED_KMH)
        val maximumChange = SPEED_CHANGE_KMH_PER_SECOND * updateIntervalMillis / MILLIS_PER_SECOND
        return (state.speedKmh + (targetSpeed - state.speedKmh).coerceIn(-maximumChange, maximumChange)).coerceIn(
            0.0,
            MAX_SPEED_KMH
        )
    }

    private fun calculateDistance(oldSpeed: Double, newSpeed: Double): Double {
        val averageSpeed = (oldSpeed + newSpeed) / 2.0
        return averageSpeed * updateIntervalMillis / MILLIS_PER_HOUR
    }

    private fun calculateNextPosition(state: VehicleSimulationState, distanceKm: Double): Pair<Double, Double> {
        val heading = Math.toRadians(state.headingDegrees)
        val latitudeChange = distanceKm * cos(heading) / KM_PER_DEGREE_LATITUDE
        val longitudeKmPerDegree = KM_PER_DEGREE_LATITUDE * cos(Math.toRadians(state.latitude))
        val longitudeChange = distanceKm * sin(heading) / longitudeKmPerDegree
        return Pair(state.latitude + latitudeChange, state.longitude + longitudeChange)
    }


    private fun calculateRemainingFuel(currentFuel: Double, distanceKm: Double): Double {
        return (currentFuel - distanceKm * FUEL_PERCENTAGE_POINTS_PER_KM).coerceIn(0.0, 100.0)
    }


    private data class VehicleSimulationState(
        val latitude: Double,
        val longitude: Double,
        val speedKmh: Double,
        val fuelPercentage: Double,
        val headingDegrees: Double,
        val cruisingSpeedKmh: Double
    )
}


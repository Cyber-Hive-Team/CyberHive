package org.example.domain.algorithm.geohash

import org.example.domain.model.exception.InvalidLatitudeException
import org.example.domain.model.exception.InvalidLongitudeException
import kotlin.math.atan2
import kotlin.math.cos
import kotlin.math.pow
import kotlin.math.sin
import kotlin.math.sqrt

class GeographicDistanceCalculator {

    fun calculateDistance(
        startLatitude: Double,
        startLongitude: Double,
        endLatitude: Double,
        endLongitude: Double
    ): Double {
        validateLatitude(startLatitude)
        validateLatitude(endLatitude)
        validateLongitude(startLongitude)
        validateLongitude(endLongitude)

        val latitudeDifference = (endLatitude - startLatitude).toRadians()
        val longitudeDifference = (endLongitude - startLongitude).toRadians()

        val haversine = calculateHaversine(
            latitudeDifference = latitudeDifference,
            longitudeDifference = longitudeDifference,
            startLatitude = startLatitude.toRadians(),
            endLatitude = endLatitude.toRadians()
        )

        return EARTH_RADIUS_KILOMETERS * calculateAngularDistance(haversine)
    }

    private fun calculateHaversine(
        latitudeDifference: Double,
        longitudeDifference: Double,
        startLatitude: Double,
        endLatitude: Double
    ): Double {
        val latitudeTerm = sin(latitudeDifference / 2).pow(2)
        val longitudeTerm = cos(startLatitude) *
                cos(endLatitude) *
                sin(longitudeDifference / 2).pow(2)

        return (latitudeTerm + longitudeTerm).coerceIn(0.0, 1.0)
    }

    private fun calculateAngularDistance(haversine: Double): Double =
        2 * atan2(
            sqrt(haversine),
            sqrt(1.0 - haversine)
        )

    private fun Double.toRadians(): Double =
        Math.toRadians(this)

    private fun validateLatitude(latitude: Double) {
        if (
            !latitude.isFinite() ||
            latitude !in MIN_LATITUDE..MAX_LATITUDE
        ) {
            throw InvalidLatitudeException()
        }
    }

    private fun validateLongitude(longitude: Double) {
        if (
            !longitude.isFinite() ||
            longitude !in MIN_LONGITUDE..MAX_LONGITUDE
        ) throw InvalidLongitudeException()
    }

    private companion object {
        const val MIN_LATITUDE = -90.0
        const val MAX_LATITUDE = 90.0
        const val MIN_LONGITUDE = -180.0
        const val MAX_LONGITUDE = 180.0
    }
}

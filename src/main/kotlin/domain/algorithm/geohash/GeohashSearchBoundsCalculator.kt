package org.example.domain.algorithm.geohash

import kotlin.math.asin
import kotlin.math.cos
import kotlin.math.max
import kotlin.math.min
import kotlin.math.sin

class GeohashSearchBoundsCalculator {

    fun calculate(
        latitude: Double,
        longitude: Double,
        radiusKm: Double
    ): GeohashSearchBounds {
        val angularRadius = radiusKm / EARTH_RADIUS_KILOMETERS
        val latitudeDelta = Math.toDegrees(angularRadius)

        val latitudeBounds = calculateLatitudeBounds(
            latitude = latitude,
            latitudeDelta = latitudeDelta
        )

        val longitudeBounds = calculateLongitudeBounds(
            latitude = latitude,
            longitude = longitude,
            angularRadius = angularRadius,
            latitudeBounds = latitudeBounds
        )

        return GeohashSearchBounds(
            latitudeBounds = latitudeBounds,
            longitudeBounds = longitudeBounds
        )
    }

    private fun calculateLatitudeBounds(
        latitude: Double,
        latitudeDelta: Double
    ): CoordinateBounds = CoordinateBounds(
        min = max(MIN_LATITUDE, latitude - latitudeDelta),
        max = min(MAX_LATITUDE, latitude + latitudeDelta)
    )

    private fun calculateLongitudeBounds(
        latitude: Double,
        longitude: Double,
        angularRadius: Double,
        latitudeBounds: CoordinateBounds
    ): List<CoordinateBounds> {
        if (reachesPole(latitudeBounds)) {
            return listOf(
                CoordinateBounds(MIN_LONGITUDE, MAX_LONGITUDE)
            )
        }

        val latitudeRadians = Math.toRadians(latitude)
        val longitudeDelta = Math.toDegrees(
            asin(
                (sin(angularRadius) / cos(latitudeRadians))
                    .coerceIn(-1.0, 1.0)
            )
        )

        return splitAtAntimeridian(
            longitude = longitude,
            longitudeDelta = longitudeDelta
        )
    }

    private fun reachesPole(bounds: CoordinateBounds): Boolean =
        bounds.min == MIN_LATITUDE ||
                bounds.max == MAX_LATITUDE

    private fun splitAtAntimeridian(
        longitude: Double,
        longitudeDelta: Double
    ): List<CoordinateBounds> {
        val minimumLongitude = longitude - longitudeDelta
        val maximumLongitude = longitude + longitudeDelta

        return when {
            minimumLongitude < MIN_LONGITUDE -> listOf(
                CoordinateBounds(MIN_LONGITUDE, maximumLongitude),
                CoordinateBounds(
                    minimumLongitude + LONGITUDE_RANGE,
                    MAX_LONGITUDE
                )
            )

            maximumLongitude > MAX_LONGITUDE -> listOf(
                CoordinateBounds(minimumLongitude, MAX_LONGITUDE),
                CoordinateBounds(
                    MIN_LONGITUDE,
                    maximumLongitude - LONGITUDE_RANGE
                )
            )

            else -> listOf(
                CoordinateBounds(minimumLongitude, maximumLongitude)
            )
        }
    }

    private companion object {
        const val EARTH_RADIUS_KILOMETERS = 6371.0088
        const val MIN_LATITUDE = -90.0
        const val MAX_LATITUDE = 90.0
        const val MIN_LONGITUDE = -180.0
        const val MAX_LONGITUDE = 180.0
        const val LONGITUDE_RANGE = 360.0
    }
}

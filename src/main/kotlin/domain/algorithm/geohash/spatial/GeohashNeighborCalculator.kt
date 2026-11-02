package org.example.domain.algorithm.geohash.spatial

import org.example.domain.algorithm.geohash.core.GeohashDirection
import org.example.domain.algorithm.geohash.encoding.GeohashDecoder
import org.example.domain.algorithm.geohash.encoding.GeohashEncoder

class GeohashNeighborCalculator(
    private val encoder: GeohashEncoder = GeohashEncoder(),
    private val decoder: GeohashDecoder = GeohashDecoder()
) {
    fun calculateNeighbors(geohash: String): Map<GeohashDirection, String> {
        val cell = decoder.decode(geohash)
        val latitudeStep = cell.latitudeBounds.max - cell.latitudeBounds.min
        val longitudeStep = cell.longitudeBounds.max - cell.longitudeBounds.min

        return GeohashDirection.entries.associateWith { direction ->
            val latitude = cell.centerLatitude +
                    direction.latitudeOffset * latitudeStep
            val longitude = cell.centerLongitude +
                    direction.longitudeOffset * longitudeStep

            val (normalizedLatitude, normalizedLongitude) =
                normalizeCoordinates(latitude, longitude)

            encoder.encode(
                normalizedLatitude,
                normalizedLongitude,
                geohash.length
            )
        }
    }

    private fun normalizeCoordinates(
        latitude: Double,
        longitude: Double
    ): Pair<Double, Double> {
        val normalizedLatitude = normalizeLatitude(latitude)
        val crossesPole = latitude !in LATITUDE_MIN..LATITUDE_MAX

        val adjustedLongitude = if (crossesPole) {
            longitude + HALF_CIRCLE_LONGITUDE
        } else {
            longitude
        }

        return normalizedLatitude to normalizeLongitude(adjustedLongitude)
    }

    private fun normalizeLatitude(latitude: Double): Double {
        val normalized = (latitude - LATITUDE_MIN).mod(LATITUDE_MODULUS)

        return when {
            normalized <= LATITUDE_MODULUS_HALF ->
                normalized + LATITUDE_MIN

            else ->
                LATITUDE_MAX - (normalized - LATITUDE_MODULUS_HALF)
        }
    }

    private fun normalizeLongitude(longitude: Double): Double =
        (longitude - LONGITUDE_MIN).mod(LONGITUDE_RANGE) + LONGITUDE_MIN

    private companion object {
        const val LATITUDE_MIN = -90.0
        const val LATITUDE_MAX = 90.0
        const val LATITUDE_MODULUS = 360.0
        const val LATITUDE_MODULUS_HALF = 180.0
        const val LONGITUDE_MIN = -180.0
        const val LONGITUDE_RANGE = 360.0
        const val HALF_CIRCLE_LONGITUDE = 180.0
    }
}

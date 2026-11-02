package org.example.domain.algorithm.geohash.core

data class CoordinateBounds(
    val min: Double,
    val max: Double
) {
    val center: Double
        get() = (min + max) / 2.0
}

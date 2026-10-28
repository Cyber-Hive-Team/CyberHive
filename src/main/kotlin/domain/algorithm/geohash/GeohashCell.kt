package org.example.domain.algorithm.geohash

data class GeohashCell(
    val latitudeBounds: CoordinateBounds,
    val longitudeBounds: CoordinateBounds
) {
    val centerLatitude: Double
        get() = latitudeBounds.center

    val centerLongitude: Double
        get() = longitudeBounds.center
}

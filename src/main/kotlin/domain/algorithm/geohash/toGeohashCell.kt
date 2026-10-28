package org.example.domain.algorithm.geohash

fun DecodingBounds.toGeohashCell(): GeohashCell =
    GeohashCell(
        latitudeBounds = CoordinateBounds(latitudeMin, latitudeMax),
        longitudeBounds = CoordinateBounds(longitudeMin, longitudeMax)
    )

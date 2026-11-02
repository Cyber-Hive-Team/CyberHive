package org.example.domain.algorithm.geohash.encoding

import org.example.domain.algorithm.geohash.core.CoordinateBounds
import org.example.domain.algorithm.geohash.core.GeohashCell

fun DecodingBounds.toGeohashCell(): GeohashCell =
    GeohashCell(
        latitudeBounds = CoordinateBounds(latitudeMin, latitudeMax),
        longitudeBounds = CoordinateBounds(longitudeMin, longitudeMax)
    )

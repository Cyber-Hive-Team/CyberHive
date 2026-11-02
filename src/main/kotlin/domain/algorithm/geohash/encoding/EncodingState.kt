package org.example.domain.algorithm.geohash.encoding

import org.example.domain.algorithm.geohash.core.CoordinateRange

data class EncodingState(
    val latRange: CoordinateRange,
    val lonRange: CoordinateRange,
    val isEven: Boolean
)

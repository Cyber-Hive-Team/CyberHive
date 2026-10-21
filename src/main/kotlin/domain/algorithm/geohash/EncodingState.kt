package org.example.domain.algorithm.geohash

data class EncodingState(
    val latRange: CoordinateRange,
    val lonRange: CoordinateRange,
    val isEven: Boolean
)

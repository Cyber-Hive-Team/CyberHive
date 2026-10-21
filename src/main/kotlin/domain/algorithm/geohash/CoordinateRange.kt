package org.example.domain.algorithm.geohash

data class CoordinateRange(
    val min: Double,
    val max: Double,
    val value: Double
) {
    fun bisect(): Pair<Int, CoordinateRange> {
        val midpoint = (min + max) / 2.0

        return if (value >= midpoint) {
            1 to copy(min = midpoint)
        } else {
            0 to copy(max = midpoint)
        }
    }
}

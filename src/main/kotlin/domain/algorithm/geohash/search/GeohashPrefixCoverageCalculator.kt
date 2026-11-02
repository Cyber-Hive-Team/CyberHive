package org.example.domain.algorithm.geohash.search

import org.example.domain.algorithm.geohash.core.GeohashPrecision
import org.example.domain.algorithm.geohash.encoding.GeohashDecoder

class GeohashPrefixCoverageCalculator(
    private val decoder: GeohashDecoder = GeohashDecoder(),
    private val precision: GeohashPrecision = GeohashPrecision.CITY
) {
    fun calculate(bounds: GeohashSearchBounds): List<String> =
        findIntersectingPrefixes(prefix = "", bounds = bounds)

    private fun findIntersectingPrefixes(
        prefix: String,
        bounds: GeohashSearchBounds
    ): List<String> =
        if (prefix.isEmpty()) {
            expandPrefixes(prefix, bounds)
        } else {
            val cell = decoder.decode(prefix)

            when {
                !bounds.intersects(cell) -> emptyList()

                bounds.contains(cell) || isMaximumPrecision(prefix) ->
                    listOf(prefix)

                else -> expandPrefixes(prefix, bounds)
            }
        }

    private fun expandPrefixes(
        prefix: String,
        bounds: GeohashSearchBounds
    ): List<String> =
        BASE32_ALPHABET.flatMap { character ->
            findIntersectingPrefixes(prefix + character, bounds)
        }

    private fun isMaximumPrecision(prefix: String): Boolean =
        prefix.length == precision.value

    private companion object {
        const val BASE32_ALPHABET =
            "0123456789bcdefghjkmnpqrstuvwxyz"
    }
}

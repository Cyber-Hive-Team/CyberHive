package org.example.test.domain.algorithm.geohash

import com.google.common.truth.Truth.assertThat
import org.example.domain.algorithm.geohash.CoordinateBounds
import org.example.domain.algorithm.geohash.GeohashDecoder
import org.example.domain.algorithm.geohash.GeohashEncoder
import org.example.domain.algorithm.geohash.GeohashPrecision
import org.example.domain.algorithm.geohash.GeohashPrefixCoverageCalculator
import org.example.domain.algorithm.geohash.GeohashSearchBounds
import org.junit.jupiter.api.Test

class GeohashPrefixCoverageCalculatorTest {

    private val encoder = GeohashEncoder()

    private val calculator = GeohashPrefixCoverageCalculator(
        precision = GeohashPrecision.CITY
    )

    @Test
    fun `should include geohash cell containing search location`() {
        val bounds = searchBounds()
        val geohash = encodeSearchLocation(GeohashPrecision.CITY)

        val prefixes = calculator.calculate(bounds)

        assertThat(prefixes).contains(geohash)
    }

    @Test
    fun `should exclude geohash cells outside search bounds`() {
        val bounds = searchBounds()
        val distantGeohash = encoder.encode(
            latitude = DISTANT_LATITUDE,
            longitude = DISTANT_LONGITUDE,
            precision = GeohashPrecision.CITY.value
        )

        val prefixes = calculator.calculate(bounds)

        assertThat(
            prefixes.any { prefix ->
                distantGeohash.startsWith(prefix) ||
                        prefix.startsWith(distantGeohash)
            }
        ).isFalse()
    }

    @Test
    fun `should not exceed configured geohash precision`() {
        val prefixes = calculator.calculate(searchBounds())

        assertThat(prefixes.map(String::length))
            .containsNoneIn(
                (GeohashPrecision.CITY.value + 1)..MAX_PRECISION
            )
    }

    @Test
    fun `should include prefixes for cells partially intersecting search bounds`() {
        val geohash = encodeSearchLocation(GeohashPrecision.CITY)
        val bounds = partiallyIntersectingBounds(geohash)

        val prefixes = calculator.calculate(bounds)

        assertThat(prefixes).contains(geohash)
    }

    @Test
    fun `should expand partially intersecting cells to configured precision`() {
        val geohash = encodeSearchLocation(GeohashPrecision.CITY)
        val bounds = partiallyExtendedBounds(geohash)
        val streetCalculator = calculatorFor(GeohashPrecision.STREET)

        val prefixes = streetCalculator.calculate(bounds)

        assertThat(prefixes.any { prefix ->
            isStreetPrefixWithinCell(prefix, geohash)
        }).isTrue()
    }

    @Test
    fun `should include prefixes from cells across search boundaries`() {
        val geohash = encodeSearchLocation(GeohashPrecision.CITY)
        val bounds = partiallyExtendedBounds(geohash)
        val streetCalculator = calculatorFor(GeohashPrecision.STREET)

        val prefixes = streetCalculator.calculate(bounds)

        assertThat(prefixes.any { prefix ->
            isOutsideCellHierarchy(prefix, geohash)
        }).isTrue()
    }

    @Test
    fun `should include prefixes on both sides of antimeridian`() {
        val precision = GeohashPrecision.STREET
        val antimeridianCalculator = calculatorFor(precision)
        val prefixes = antimeridianCalculator.calculate(antimeridianBounds())

        assertCoverageIncludesBothAntimeridianSides(prefixes, precision)
    }

    private fun encodeSearchLocation(
        precision: GeohashPrecision
    ): String = encoder.encode(
        latitude = SEARCH_LATITUDE,
        longitude = SEARCH_LONGITUDE,
        precision = precision.value
    )

    private fun partiallyIntersectingBounds(
        geohash: String
    ): GeohashSearchBounds {
        val cell = GeohashDecoder().decode(geohash)

        return GeohashSearchBounds(
            latitudeBounds = CoordinateBounds(
                min = cell.latitudeBounds.center,
                max = cell.latitudeBounds.max
            ),
            longitudeBounds = listOf(
                CoordinateBounds(
                    min = cell.longitudeBounds.center,
                    max = cell.longitudeBounds.max
                )
            )
        )
    }

    private fun partiallyExtendedBounds(
        geohash: String
    ): GeohashSearchBounds {
        val cell = GeohashDecoder().decode(geohash)

        return GeohashSearchBounds(
            latitudeBounds = CoordinateBounds(
                min = cell.latitudeBounds.center,
                max = cell.latitudeBounds.max + BOUNDS_EXTENSION
            ),
            longitudeBounds = listOf(
                CoordinateBounds(
                    min = cell.longitudeBounds.center,
                    max = cell.longitudeBounds.max + BOUNDS_EXTENSION
                )
            )
        )
    }

    private fun calculatorFor(
        precision: GeohashPrecision
    ): GeohashPrefixCoverageCalculator =
        GeohashPrefixCoverageCalculator(
            precision = precision
        )

    private fun isStreetPrefixWithinCell(
        prefix: String,
        geohash: String
    ): Boolean =
        prefix.length == GeohashPrecision.STREET.value &&
                prefix.startsWith(geohash)

    private fun isOutsideCellHierarchy(
        prefix: String,
        geohash: String
    ): Boolean =
        !prefix.startsWith(geohash) &&
                !geohash.startsWith(prefix)

    private fun antimeridianBounds() = GeohashSearchBounds(
        latitudeBounds = CoordinateBounds(
            min = -ANTIMERIDIAN_TEST_LATITUDE,
            max = ANTIMERIDIAN_TEST_LATITUDE
        ),
        longitudeBounds = listOf(
            CoordinateBounds(
                min = ANTIMERIDIAN_WESTERN_BOUND,
                max = ANTIMERIDIAN_MAX_LONGITUDE
            ),
            CoordinateBounds(
                min = ANTIMERIDIAN_MIN_LONGITUDE,
                max = ANTIMERIDIAN_EASTERN_BOUND
            )
        )
    )

    private fun assertCoverageIncludesBothAntimeridianSides(
        prefixes: Collection<String>,
        precision: GeohashPrecision
    ) {
        val easternGeohash = encoder.encode(
            latitude = ANTIMERIDIAN_TEST_LATITUDE,
            longitude = ANTIMERIDIAN_EASTERN_BOUND,
            precision = precision.value
        )
        val westernGeohash = encoder.encode(
            latitude = ANTIMERIDIAN_TEST_LATITUDE,
            longitude = ANTIMERIDIAN_WESTERN_BOUND,
            precision = precision.value
        )

        assertThat(prefixes.any { easternGeohash.startsWith(it) }).isTrue()
        assertThat(prefixes.any { westernGeohash.startsWith(it) }).isTrue()
    }

    private fun searchBounds() = GeohashSearchBounds(
        latitudeBounds = CoordinateBounds(
            min = MIN_LATITUDE,
            max = MAX_LATITUDE
        ),
        longitudeBounds = listOf(
            CoordinateBounds(
                min = MIN_LONGITUDE,
                max = MAX_LONGITUDE
            )
        )
    )

    private companion object {
        const val SEARCH_LATITUDE = 31.5
        const val SEARCH_LONGITUDE = 34.5
        const val DISTANT_LATITUDE = 51.5074
        const val DISTANT_LONGITUDE = -0.1278
        const val MIN_LATITUDE = 31.49
        const val MAX_LATITUDE = 31.51
        const val MIN_LONGITUDE = 34.49
        const val MAX_LONGITUDE = 34.51
        const val MAX_PRECISION = 12
        const val BOUNDS_EXTENSION = 0.001
        const val ANTIMERIDIAN_TEST_LATITUDE = 0.1
        const val ANTIMERIDIAN_WESTERN_BOUND = 179.8
        const val ANTIMERIDIAN_EASTERN_BOUND = -179.8
        const val ANTIMERIDIAN_MIN_LONGITUDE = -180.0
        const val ANTIMERIDIAN_MAX_LONGITUDE = 180.0
    }
}

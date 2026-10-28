package org.example.test.domain.algorithm.geohash

import com.google.common.truth.Truth.assertThat
import org.example.domain.algorithm.geohash.GeohashDecoder
import org.example.domain.algorithm.geohash.GeohashDirection
import org.example.domain.algorithm.geohash.GeohashEncoder
import org.example.domain.algorithm.geohash.GeohashNeighborCalculator
import org.junit.jupiter.api.Test

class GeohashNeighborCalculatorTest {

    private val encoder = GeohashEncoder()
    private val decoder = GeohashDecoder()
    private val calculator = GeohashNeighborCalculator(encoder, decoder)

    @Test
    fun `should return all eight neighboring directions`() {
        val geohash = encoder.encode(LATITUDE, LONGITUDE, PRECISION)

        val neighbors = calculator.calculateNeighbors(geohash)

        assertThat(neighbors.keys)
            .containsExactlyElementsIn(GeohashDirection.entries)
    }

    @Test
    fun `should preserve the original geohash precision`() {
        val geohash = encoder.encode(LATITUDE, LONGITUDE, PRECISION)

        val neighbors = calculator.calculateNeighbors(geohash)

        assertThat(neighbors.values.map { it.length })
            .containsExactlyElementsIn(
                GeohashDirection.entries.map { PRECISION }
            )
    }

    @Test
    fun `should not include the original cell among its neighbors`() {
        val geohash = encoder.encode(LATITUDE, LONGITUDE, PRECISION)

        val neighbors = calculator.calculateNeighbors(geohash)

        assertThat(neighbors.values).doesNotContain(geohash)
    }

    @Test
    fun `should wrap east neighbor across the longitude boundary`() {
        val geohash = encoder.encode(
            EQUATOR_LATITUDE,
            DATELINE_LONGITUDE,
            PRECISION
        )

        val neighbors = calculator.calculateNeighbors(geohash)
        val easternNeighbor = decoder.decode(
            neighbors.getValue(GeohashDirection.EAST)
        )

        assertThat(easternNeighbor.centerLongitude).isLessThan(0.0)
    }

    @Test
    fun `should adjust longitude when northern neighbor crosses the pole`() {
        val geohash = encoder.encode(
            NEAR_NORTH_POLE,
            LONGITUDE,
            PRECISION
        )

        val neighbors = calculator.calculateNeighbors(geohash)
        val northernNeighbor = decoder.decode(
            neighbors.getValue(GeohashDirection.NORTH)
        )

        assertThat(kotlin.math.abs(northernNeighbor.centerLongitude))
            .isGreaterThan(90.0)
    }

    private companion object {
        const val LATITUDE = 31.5
        const val LONGITUDE = 34.5
        const val EQUATOR_LATITUDE = 0.0
        const val DATELINE_LONGITUDE = 179.999
        const val NEAR_NORTH_POLE = 89.99
        const val PRECISION = 5
    }
}

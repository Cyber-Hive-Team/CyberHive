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
    fun `returns all eight neighboring directions`() {
        // Given
        val geohash = encoder.encode(LATITUDE, LONGITUDE, PRECISION)

        // When
        val neighbors = calculator.calculateNeighbors(geohash)

        // Then
        assertThat(neighbors.keys)
            .containsExactlyElementsIn(GeohashDirection.entries)
    }

    @Test
    fun `preserves the original geohash precision`() {
        // Given
        val geohash = encoder.encode(LATITUDE, LONGITUDE, PRECISION)

        // When
        val neighbors = calculator.calculateNeighbors(geohash)

        // Then
        assertThat(neighbors.values.map { it.length })
            .containsExactlyElementsIn(
                GeohashDirection.entries.map { PRECISION }
            )
    }

    @Test
    fun `does not include the original cell among its neighbors`() {
        // Given
        val geohash = encoder.encode(LATITUDE, LONGITUDE, PRECISION)

        // When
        val neighbors = calculator.calculateNeighbors(geohash)

        // Then
        assertThat(neighbors.values).doesNotContain(geohash)
    }

    @Test
    fun `wraps east neighbor across the longitude boundary`() {
        // Given
        val geohash = encoder.encode(
            EQUATOR_LATITUDE,
            DATELINE_LONGITUDE,
            PRECISION
        )

        // When
        val neighbors = calculator.calculateNeighbors(geohash)
        val easternNeighbor = decoder.decode(
            neighbors.getValue(GeohashDirection.EAST)
        )

        // Then
        assertThat(easternNeighbor.centerLongitude).isLessThan(0.0)
    }

    @Test
    fun `adjusts longitude when northern neighbor crosses the pole`() {
        // Given
        val geohash = encoder.encode(
            NEAR_NORTH_POLE,
            LONGITUDE,
            PRECISION
        )

        // When
        val neighbors = calculator.calculateNeighbors(geohash)
        val northernNeighbor = decoder.decode(
            neighbors.getValue(GeohashDirection.NORTH)
        )

        // Then
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

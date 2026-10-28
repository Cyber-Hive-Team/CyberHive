package org.example.test.domain.algorithm.geohash

import com.google.common.truth.Truth.assertThat
import org.example.domain.algorithm.geohash.GeohashDecoder
import org.example.domain.algorithm.geohash.GeohashEncoder
import org.example.domain.model.exception.InvalidGeohashException
import org.junit.jupiter.api.Assertions.assertThrows
import org.junit.jupiter.api.Test

class GeohashDecoderTest {

    private val decoder = GeohashDecoder()
    private val encoder = GeohashEncoder()

    @Test
    fun `decodes minimum latitude for geohash s`() {
        val cell = decoder.decode(SINGLE_CHARACTER_GEOHASH)

        assertThat(cell.latitudeBounds.min).isEqualTo(MINIMUM_COORDINATE)
    }

    @Test
    fun `decodes maximum latitude for geohash s`() {
        val cell = decoder.decode(SINGLE_CHARACTER_GEOHASH)

        assertThat(cell.latitudeBounds.max).isEqualTo(MAXIMUM_LATITUDE)
    }

    @Test
    fun `decodes minimum longitude for geohash s`() {
        val cell = decoder.decode(SINGLE_CHARACTER_GEOHASH)

        assertThat(cell.longitudeBounds.min).isEqualTo(MINIMUM_COORDINATE)
    }

    @Test
    fun `decodes maximum longitude for geohash s`() {
        val cell = decoder.decode(SINGLE_CHARACTER_GEOHASH)

        assertThat(cell.longitudeBounds.max).isEqualTo(MAXIMUM_LATITUDE)
    }

    @Test
    fun `calculates center latitude for geohash s`() {
        val cell = decoder.decode(SINGLE_CHARACTER_GEOHASH)

        assertThat(cell.centerLatitude).isEqualTo(EXPECTED_CENTER)
    }

    @Test
    fun `calculates center longitude for geohash s`() {
        val cell = decoder.decode(SINGLE_CHARACTER_GEOHASH)

        assertThat(cell.centerLongitude).isEqualTo(EXPECTED_CENTER)
    }

    @Test
    fun `decoded cell contains the encoded latitude`() {
        val geohash = encoder.encode(
            TEST_LATITUDE,
            TEST_LONGITUDE,
            TEST_PRECISION
        )
        val cell = decoder.decode(geohash)

        assertThat(TEST_LATITUDE)
            .isAtLeast(cell.latitudeBounds.min)
    }

    @Test
    fun `decoded cell contains the encoded longitude`() {
        val geohash = encoder.encode(
            TEST_LATITUDE,
            TEST_LONGITUDE,
            TEST_PRECISION
        )
        val cell = decoder.decode(geohash)

        assertThat(TEST_LONGITUDE)
            .isAtLeast(cell.longitudeBounds.min)
    }

    @Test
    fun `higher precision produces narrower latitude bounds`() {
        val coarseCell = decoder.decode(COARSE_GEOHASH)
        val fineCell = decoder.decode(FINE_GEOHASH)

        assertThat(fineCell.latitudeBounds.max - fineCell.latitudeBounds.min)
            .isLessThan(coarseCell.latitudeBounds.max - coarseCell.latitudeBounds.min)
    }

    @Test
    fun `higher precision produces narrower longitude bounds`() {
        val coarseCell = decoder.decode(COARSE_GEOHASH)
        val fineCell = decoder.decode(FINE_GEOHASH)

        assertThat(fineCell.longitudeBounds.max - fineCell.longitudeBounds.min)
            .isLessThan(coarseCell.longitudeBounds.max - coarseCell.longitudeBounds.min)
    }

    @Test
    fun `rejects empty geohash`() {
        assertThrows(InvalidGeohashException::class.java) {
            decoder.decode(EMPTY_GEOHASH)
        }
    }

    @Test
    fun `rejects invalid geohash characters`() {
        assertThrows(InvalidGeohashException::class.java) {
            decoder.decode(INVALID_GEOHASH)
        }
    }

    @Test
    fun `rejects uppercase geohash characters`() {
        assertThrows(InvalidGeohashException::class.java) {
            decoder.decode(UPPERCASE_GEOHASH)
        }
    }

    private companion object {
        const val MINIMUM_COORDINATE = 0.0
        const val MAXIMUM_LATITUDE = 45.0
        const val EXPECTED_CENTER = 22.5
        const val TEST_LATITUDE = 31.5
        const val TEST_LONGITUDE = 34.5
        const val TEST_PRECISION = 5
        const val SINGLE_CHARACTER_GEOHASH = "s"
        const val COARSE_GEOHASH = "s"
        const val FINE_GEOHASH = "sv8e1"
        const val EMPTY_GEOHASH = ""
        const val INVALID_GEOHASH = "s!v8"
        const val UPPERCASE_GEOHASH = "SV8E1"
    }
}

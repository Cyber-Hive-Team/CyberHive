package org.example.test.domain.algorithm.geohash

import com.google.common.truth.Truth.assertThat
import org.example.domain.algorithm.geohash.GeohashEncoder
import org.example.domain.algorithm.geohash.GeohashPrecision
import org.example.domain.model.exception.InvalidGeohashPrecisionException
import org.example.domain.model.exception.InvalidLatitudeException
import org.example.domain.model.exception.InvalidLongitudeException
import org.junit.jupiter.api.Assertions.assertThrows
import org.junit.jupiter.api.Test

class GeohashEncoderTest {

    private val encoder = GeohashEncoder()

    @Test
    fun `encodes London at city precision`() {
        val result = encoder.encode(
            LONDON_LATITUDE,
            LONDON_LONGITUDE,
            CITY_PRECISION
        )

        assertThat(result).isEqualTo(LONDON_GEOHASH_CITY)
    }

    @Test
    fun `encodes London at street precision`() {
        val result = encoder.encode(
            LONDON_LATITUDE,
            LONDON_LONGITUDE,
            STREET_PRECISION
        )

        assertThat(result).isEqualTo(LONDON_GEOHASH_STREET)
    }

    @Test
    fun `encodes origin at city precision`() {
        val result = encoder.encode(ORIGIN, ORIGIN, CITY_PRECISION)

        assertThat(result).isEqualTo(ORIGIN_GEOHASH_CITY)
    }

    @Test
    fun `encodes origin at minimum precision`() {
        val result = encoder.encode(ORIGIN, ORIGIN, MIN_PRECISION)

        assertThat(result).isEqualTo(ORIGIN_GEOHASH_MIN_PRECISION)
    }

    @Test
    fun `encodes regional point at precision one`() {
        val result = encoder.encode(
            REGIONAL_LATITUDE,
            REGIONAL_LONGITUDE,
            MIN_PRECISION
        )

        assertThat(result).isEqualTo("s")
    }

    @Test
    fun `encodes regional point at precision two`() {
        val result = encoder.encode(
            REGIONAL_LATITUDE,
            REGIONAL_LONGITUDE,
            2
        )

        assertThat(result).isEqualTo("sv")
    }

    @Test
    fun `encodes regional point at precision three`() {
        val result = encoder.encode(
            REGIONAL_LATITUDE,
            REGIONAL_LONGITUDE,
            3
        )

        assertThat(result).isEqualTo("sv8")
    }

    @Test
    fun `encodes regional point at precision four`() {
        val result = encoder.encode(
            REGIONAL_LATITUDE,
            REGIONAL_LONGITUDE,
            4
        )

        assertThat(result).isEqualTo("sv8e")
    }

    @Test
    fun `encodes regional point at precision five`() {
        val result = encoder.encode(
            REGIONAL_LATITUDE,
            REGIONAL_LONGITUDE,
            CITY_PRECISION
        )

        assertThat(result).isEqualTo("sv8e1")
    }

    @Test
    fun `encodes regional point at precision six`() {
        val result = encoder.encode(
            REGIONAL_LATITUDE,
            REGIONAL_LONGITUDE,
            6
        )

        assertThat(result).isEqualTo("sv8e1n")
    }

    @Test
    fun `encodes regional point at precision seven`() {
        val result = encoder.encode(
            REGIONAL_LATITUDE,
            REGIONAL_LONGITUDE,
            STREET_PRECISION
        )

        assertThat(result).isEqualTo("sv8e1n6")
    }

    @Test
    fun `encodes regional point at precision eight`() {
        val result = encoder.encode(
            REGIONAL_LATITUDE,
            REGIONAL_LONGITUDE,
            8
        )

        assertThat(result).isEqualTo("sv8e1n6h")
    }

    @Test
    fun `encodes regional point at precision nine`() {
        val result = encoder.encode(
            REGIONAL_LATITUDE,
            REGIONAL_LONGITUDE,
            9
        )

        assertThat(result).isEqualTo("sv8e1n6hu")
    }

    @Test
    fun `encodes regional point at precision ten`() {
        val result = encoder.encode(
            REGIONAL_LATITUDE,
            REGIONAL_LONGITUDE,
            10
        )

        assertThat(result).isEqualTo("sv8e1n6hu3")
    }

    @Test
    fun `encodes regional point at precision eleven`() {
        val result = encoder.encode(
            REGIONAL_LATITUDE,
            REGIONAL_LONGITUDE,
            11
        )

        assertThat(result).isEqualTo("sv8e1n6hu38")
    }

    @Test
    fun `encodes regional point at maximum precision`() {
        val result = encoder.encode(
            REGIONAL_LATITUDE,
            REGIONAL_LONGITUDE,
            MAX_PRECISION
        )

        assertThat(result).isEqualTo("sv8e1n6hu38e")
    }

    @Test
    fun `result length equals requested precision`() {
        val result = encoder.encode(
            REGIONAL_LATITUDE,
            REGIONAL_LONGITUDE,
            STREET_PRECISION
        )

        assertThat(result).hasLength(STREET_PRECISION)
    }

    @Test
    fun `CITY enum produces expected geohash`() {
        val result = encoder.encode(
            REGIONAL_LATITUDE,
            REGIONAL_LONGITUDE,
            GeohashPrecision.CITY
        )

        assertThat(result).isEqualTo("sv8e1")
    }

    @Test
    fun `STREET enum produces expected geohash`() {
        val result = encoder.encode(
            REGIONAL_LATITUDE,
            REGIONAL_LONGITUDE,
            GeohashPrecision.STREET
        )

        assertThat(result).isEqualTo("sv8e1n6")
    }

    @Test
    fun `higher precision extends the lower precision geohash`() {
        val cityHash = encoder.encode(
            LONDON_LATITUDE,
            LONDON_LONGITUDE,
            CITY_PRECISION
        )
        val streetHash = encoder.encode(
            LONDON_LATITUDE,
            LONDON_LONGITUDE,
            STREET_PRECISION
        )

        assertThat(streetHash).startsWith(cityHash)
    }

    @Test
    fun `same point produces the same geohash`() {
        val firstResult = encoder.encode(
            REGIONAL_LATITUDE,
            REGIONAL_LONGITUDE,
            STREET_PRECISION
        )
        val secondResult = encoder.encode(
            REGIONAL_LATITUDE,
            REGIONAL_LONGITUDE,
            STREET_PRECISION
        )

        assertThat(firstResult).isEqualTo(secondResult)
    }

    @Test
    fun `result contains only valid Base32 characters`() {
        val result = encoder.encode(
            LONDON_LATITUDE,
            LONDON_LONGITUDE,
            MAX_PRECISION
        )

        assertThat(result.all { it in BASE32_ALPHABET }).isTrue()
    }

    @Test
    fun `encodes maximum latitude`() {
        val result = encoder.encode(
            MAX_LATITUDE,
            ORIGIN,
            CITY_PRECISION
        )

        assertThat(result).isEqualTo(MAX_LATITUDE_GEOHASH)
    }

    @Test
    fun `encodes minimum latitude`() {
        val result = encoder.encode(
            MIN_LATITUDE,
            ORIGIN,
            CITY_PRECISION
        )

        assertThat(result).isEqualTo(MIN_LATITUDE_GEOHASH)
    }

    @Test
    fun `encodes maximum longitude`() {
        val result = encoder.encode(
            ORIGIN,
            MAX_LONGITUDE,
            CITY_PRECISION
        )

        assertThat(result).isEqualTo(MAX_LONGITUDE_GEOHASH)
    }

    @Test
    fun `encodes minimum longitude`() {
        val result = encoder.encode(
            ORIGIN,
            MIN_LONGITUDE,
            CITY_PRECISION
        )

        assertThat(result).isEqualTo(MIN_LONGITUDE_GEOHASH)
    }

    @Test
    fun `rejects zero precision`() {
        assertThrows(InvalidGeohashPrecisionException::class.java) {
            encoder.encode(ORIGIN, ORIGIN, INVALID_MIN_PRECISION)
        }
    }

    @Test
    fun `rejects precision above maximum`() {
        assertThrows(InvalidGeohashPrecisionException::class.java) {
            encoder.encode(ORIGIN, ORIGIN, INVALID_MAX_PRECISION)
        }
    }

    @Test
    fun `rejects negative precision`() {
        assertThrows(InvalidGeohashPrecisionException::class.java) {
            encoder.encode(ORIGIN, ORIGIN, NEGATIVE_PRECISION)
        }
    }

    @Test
    fun `validates precision before latitude`() {
        assertThrows(InvalidGeohashPrecisionException::class.java) {
            encoder.encode(INVALID_LATITUDE, ORIGIN, INVALID_MIN_PRECISION)
        }
    }

    @Test
    fun `rejects latitude above maximum`() {
        assertThrows(InvalidLatitudeException::class.java) {
            encoder.encode(INVALID_LATITUDE, ORIGIN, CITY_PRECISION)
        }
    }

    @Test
    fun `rejects latitude below minimum`() {
        assertThrows(InvalidLatitudeException::class.java) {
            encoder.encode(BELOW_MIN_LATITUDE, ORIGIN, CITY_PRECISION)
        }
    }

    @Test
    fun `rejects NaN latitude`() {
        assertThrows(InvalidLatitudeException::class.java) {
            encoder.encode(Double.NaN, ORIGIN, CITY_PRECISION)
        }
    }

    @Test
    fun `rejects infinite latitude`() {
        assertThrows(InvalidLatitudeException::class.java) {
            encoder.encode(Double.POSITIVE_INFINITY, ORIGIN, CITY_PRECISION)
        }
    }

    @Test
    fun `rejects longitude above maximum`() {
        assertThrows(InvalidLongitudeException::class.java) {
            encoder.encode(ORIGIN, INVALID_LONGITUDE, CITY_PRECISION)
        }
    }

    @Test
    fun `rejects longitude below minimum`() {
        assertThrows(InvalidLongitudeException::class.java) {
            encoder.encode(ORIGIN, BELOW_MIN_LONGITUDE, CITY_PRECISION)
        }
    }

    @Test
    fun `rejects NaN longitude`() {
        assertThrows(InvalidLongitudeException::class.java) {
            encoder.encode(ORIGIN, Double.NaN, CITY_PRECISION)
        }
    }

    @Test
    fun `rejects infinite longitude`() {
        assertThrows(InvalidLongitudeException::class.java) {
            encoder.encode(ORIGIN, Double.NEGATIVE_INFINITY, CITY_PRECISION)
        }
    }

    private companion object {
        const val MIN_PRECISION = 1
        const val MAX_PRECISION = 12
        const val CITY_PRECISION = 5
        const val STREET_PRECISION = 7
        const val INVALID_MIN_PRECISION = 0
        const val INVALID_MAX_PRECISION = 13
        const val NEGATIVE_PRECISION = -1
        const val ORIGIN = 0.0
        const val LONDON_LATITUDE = 51.5074
        const val LONDON_LONGITUDE = -0.1278
        const val REGIONAL_LATITUDE = 31.5
        const val REGIONAL_LONGITUDE = 34.5
        const val MIN_LATITUDE = -90.0
        const val MAX_LATITUDE = 90.0
        const val MIN_LONGITUDE = -180.0
        const val MAX_LONGITUDE = 180.0
        const val INVALID_LATITUDE = 90.0001
        const val BELOW_MIN_LATITUDE = -90.0001
        const val INVALID_LONGITUDE = 180.0001
        const val BELOW_MIN_LONGITUDE = -180.0001
        const val LONDON_GEOHASH_CITY = "gcpvj"
        const val LONDON_GEOHASH_STREET = "gcpvj0d"
        const val ORIGIN_GEOHASH_CITY = "s0000"
        const val ORIGIN_GEOHASH_MIN_PRECISION = "s"
        const val MAX_LATITUDE_GEOHASH = "upbpb"
        const val MIN_LATITUDE_GEOHASH = "h0000"
        const val MAX_LONGITUDE_GEOHASH = "xbpbp"
        const val MIN_LONGITUDE_GEOHASH = "80000"
        const val BASE32_ALPHABET = "0123456789bcdefghjkmnpqrstuvwxyz"
    }
}

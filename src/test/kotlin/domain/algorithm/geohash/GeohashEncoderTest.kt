package org.example.test.domain.algorithm.geohash

import org.example.domain.algorithm.geohash.GeohashEncoder
import org.example.domain.algorithm.geohash.GeohashPrecision
import org.example.domain.model.exception.InvalidGeohashPrecisionException
import org.example.domain.model.exception.InvalidLatitudeException
import org.example.domain.model.exception.InvalidLongitudeException
import org.junit.jupiter.api.Assertions.assertThrows
import org.junit.jupiter.api.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class GeohashEncoderTest {

    private val encoder = GeohashEncoder()

    @Test
    fun `encodes London at precision 5 to gcpvj`() {
        val result = encoder.encode(
            LONDON_LATITUDE,
            LONDON_LONGITUDE,
            CITY_PRECISION
        )

        assertEquals(LONDON_GEOHASH_CITY, result)
    }

    @Test
    fun `encodes London at precision 7 to gcpvj0d`() {
        val result = encoder.encode(
            LONDON_LATITUDE,
            LONDON_LONGITUDE,
            STREET_PRECISION
        )

        assertEquals(LONDON_GEOHASH_STREET, result)
    }

    @Test
    fun `encodes origin at precision 5 to s0000`() {
        val result = encoder.encode(
            ORIGIN,
            ORIGIN,
            CITY_PRECISION
        )

        assertEquals(ORIGIN_GEOHASH_CITY, result)
    }

    @Test
    fun `encodes origin at precision 1 to s`() {
        val result = encoder.encode(
            ORIGIN,
            ORIGIN,
            MIN_PRECISION
        )

        assertEquals(ORIGIN_GEOHASH_MIN_PRECISION, result)
    }

    @Test
    fun `encodes all precision levels from 1 to 12 at regional point`() {
        val expectedHashes = mapOf(
            1 to "s",
            2 to "sv",
            3 to "sv8",
            4 to "sv8e",
            5 to "sv8e1",
            6 to "sv8e1n",
            7 to "sv8e1n6",
            8 to "sv8e1n6h",
            9 to "sv8e1n6hu",
            10 to "sv8e1n6hu3",
            11 to "sv8e1n6hu38",
            12 to "sv8e1n6hu38e"
        )

        expectedHashes.forEach { (precision, expectedHash) ->
            assertEquals(
                expectedHash,
                encoder.encode(
                    REGIONAL_LATITUDE,
                    REGIONAL_LONGITUDE,
                    precision
                ),
                "Mismatch at precision $precision"
            )
        }
    }

    @Test
    fun `result length equals requested precision for all valid values`() {
        (MIN_PRECISION..MAX_PRECISION).forEach { precision ->
            val result = encoder.encode(
                REGIONAL_LATITUDE,
                REGIONAL_LONGITUDE,
                precision
            )

            assertEquals(
                precision,
                result.length,
                "Length mismatch at precision $precision"
            )
        }
    }

    @Test
    fun `enum overload CITY produces same result as integer precision`() {
        val viaEnum = encoder.encode(
            REGIONAL_LATITUDE,
            REGIONAL_LONGITUDE,
            GeohashPrecision.CITY
        )
        val viaInt = encoder.encode(
            REGIONAL_LATITUDE,
            REGIONAL_LONGITUDE,
            CITY_PRECISION
        )

        assertEquals(viaInt, viaEnum)
        assertEquals(CITY_PRECISION, viaEnum.length)
    }

    @Test
    fun `enum overload STREET produces same result as integer precision`() {
        val viaEnum = encoder.encode(
            REGIONAL_LATITUDE,
            REGIONAL_LONGITUDE,
            GeohashPrecision.STREET
        )
        val viaInt = encoder.encode(
            REGIONAL_LATITUDE,
            REGIONAL_LONGITUDE,
            STREET_PRECISION
        )

        assertEquals(viaInt, viaEnum)
        assertEquals(STREET_PRECISION, viaEnum.length)
    }

    @Test
    fun `higher precision result extends lower precision result`() {
        val expectedHashes = listOf(
            "g",
            "gc",
            "gcp",
            "gcpv",
            "gcpvj",
            "gcpvj0",
            "gcpvj0d"
        )

        expectedHashes.forEachIndexed { index, expectedHash ->
            val precision = index + MIN_PRECISION
            val result = encoder.encode(
                LONDON_LATITUDE,
                LONDON_LONGITUDE,
                precision
            )

            assertEquals(
                expectedHash,
                result,
                "Prefix extension failed at precision $precision"
            )
        }
    }

    @Test
    fun `same point always produces same hash`() {
        val first = encoder.encode(
            REGIONAL_LATITUDE,
            REGIONAL_LONGITUDE,
            STREET_PRECISION
        )
        val second = encoder.encode(
            REGIONAL_LATITUDE,
            REGIONAL_LONGITUDE,
            STREET_PRECISION
        )

        assertEquals(first, second)
    }

    @Test
    fun `result contains only valid Base32 characters`() {
        val result = encoder.encode(
            LONDON_LATITUDE,
            LONDON_LONGITUDE,
            MAX_PRECISION
        )

        assertTrue(
            result.all { it in BASE32_ALPHABET },
            "Result contains an invalid Base32 character: $result"
        )
    }

    @Test
    fun `encodes maximum latitude 90 at precision 5`() {
        val result = encoder.encode(
            MAX_LATITUDE,
            ORIGIN,
            CITY_PRECISION
        )

        assertEquals(MAX_LATITUDE_GEOHASH, result)
    }

    @Test
    fun `encodes minimum latitude minus 90 at precision 5`() {
        val result = encoder.encode(
            MIN_LATITUDE,
            ORIGIN,
            CITY_PRECISION
        )

        assertEquals(MIN_LATITUDE_GEOHASH, result)
    }

    @Test
    fun `encodes maximum longitude 180 at precision 5`() {
        val result = encoder.encode(
            ORIGIN,
            MAX_LONGITUDE,
            CITY_PRECISION
        )

        assertEquals(MAX_LONGITUDE_GEOHASH, result)
    }

    @Test
    fun `encodes minimum longitude minus 180 at precision 5`() {
        val result = encoder.encode(
            ORIGIN,
            MIN_LONGITUDE,
            CITY_PRECISION
        )

        assertEquals(MIN_LONGITUDE_GEOHASH, result)
    }

    @Test
    fun `throws InvalidGeohashPrecisionException for precision zero`() {
        assertThrows(InvalidGeohashPrecisionException::class.java) {
            encoder.encode(ORIGIN, ORIGIN, INVALID_MIN_PRECISION)
        }
    }

    @Test
    fun `throws InvalidGeohashPrecisionException for precision above maximum`() {
        assertThrows(InvalidGeohashPrecisionException::class.java) {
            encoder.encode(ORIGIN, ORIGIN, INVALID_MAX_PRECISION)
        }
    }

    @Test
    fun `throws InvalidGeohashPrecisionException for negative precision`() {
        assertThrows(InvalidGeohashPrecisionException::class.java) {
            encoder.encode(ORIGIN, ORIGIN, NEGATIVE_PRECISION)
        }
    }

    @Test
    fun `precision is validated before coordinates`() {
        assertThrows(InvalidGeohashPrecisionException::class.java) {
            encoder.encode(INVALID_LATITUDE, ORIGIN, INVALID_MIN_PRECISION)
        }
    }

    @Test
    fun `throws InvalidLatitudeException for latitude above maximum`() {
        assertThrows(InvalidLatitudeException::class.java) {
            encoder.encode(INVALID_LATITUDE, ORIGIN, CITY_PRECISION)
        }
    }

    @Test
    fun `throws InvalidLatitudeException for latitude below minimum`() {
        assertThrows(InvalidLatitudeException::class.java) {
            encoder.encode(BELOW_MIN_LATITUDE, ORIGIN, CITY_PRECISION)
        }
    }

    @Test
    fun `throws InvalidLatitudeException for NaN latitude`() {
        assertThrows(InvalidLatitudeException::class.java) {
            encoder.encode(Double.NaN, ORIGIN, CITY_PRECISION)
        }
    }

    @Test
    fun `throws InvalidLatitudeException for infinite latitude`() {
        assertThrows(InvalidLatitudeException::class.java) {
            encoder.encode(Double.POSITIVE_INFINITY, ORIGIN, CITY_PRECISION)
        }
    }

    @Test
    fun `throws InvalidLongitudeException for longitude above maximum`() {
        assertThrows(InvalidLongitudeException::class.java) {
            encoder.encode(ORIGIN, INVALID_LONGITUDE, CITY_PRECISION)
        }
    }

    @Test
    fun `throws InvalidLongitudeException for longitude below minimum`() {
        assertThrows(InvalidLongitudeException::class.java) {
            encoder.encode(ORIGIN, BELOW_MIN_LONGITUDE, CITY_PRECISION)
        }
    }

    @Test
    fun `throws InvalidLongitudeException for NaN longitude`() {
        assertThrows(InvalidLongitudeException::class.java) {
            encoder.encode(ORIGIN, Double.NaN, CITY_PRECISION)
        }
    }

    @Test
    fun `throws InvalidLongitudeException for infinite longitude`() {
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

        const val BASE32_ALPHABET =
            "0123456789bcdefghjkmnpqrstuvwxyz"
    }
}

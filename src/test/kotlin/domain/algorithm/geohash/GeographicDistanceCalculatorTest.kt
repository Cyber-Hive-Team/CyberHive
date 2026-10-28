package org.example.test.domain.algorithm.geohash

import com.google.common.truth.Truth.assertThat
import org.example.domain.algorithm.geohash.GeographicDistanceCalculator
import org.example.domain.model.exception.InvalidLatitudeException
import org.example.domain.model.exception.InvalidLongitudeException
import org.junit.jupiter.api.Test

class GeographicDistanceCalculatorTest {

    private val calculator = GeographicDistanceCalculator()

    @Test
    fun `should return zero for identical coordinates`() {
        val distance = calculator.calculateDistance(
            startLatitude = VALID_LATITUDE,
            startLongitude = VALID_LONGITUDE,
            endLatitude = VALID_LATITUDE,
            endLongitude = VALID_LONGITUDE
        )

        assertThat(distance).isEqualTo(ZERO_DISTANCE)
    }

    @Test
    fun `should calculate distance between known coordinates`() {
        val distance = calculator.calculateDistance(
            startLatitude = EQUATOR_LATITUDE,
            startLongitude = EQUATOR_LONGITUDE,
            endLatitude = EQUATOR_LATITUDE,
            endLongitude = ONE_DEGREE_LONGITUDE
        )

        assertThat(distance).isWithin(DISTANCE_TOLERANCE_KM)
            .of(DISTANCE_PER_LONGITUDE_DEGREE_KM)
    }

    @Test
    fun `should calculate shortest distance across antimeridian`() {
        val distance = calculator.calculateDistance(
            startLatitude = EQUATOR_LATITUDE,
            startLongitude = EAST_ANTIMERIDIAN_LONGITUDE,
            endLatitude = EQUATOR_LATITUDE,
            endLongitude = WEST_ANTIMERIDIAN_LONGITUDE
        )

        assertThat(distance).isWithin(DISTANCE_TOLERANCE_KM)
            .of(ANTIMERIDIAN_DISTANCE_KM)
    }

    @Test
    fun `should reject latitude above maximum`() {
        val result = runCatching {
            calculator.calculateDistance(
                startLatitude = INVALID_HIGH_LATITUDE,
                startLongitude = VALID_LONGITUDE,
                endLatitude = VALID_LATITUDE,
                endLongitude = VALID_LONGITUDE
            )
        }

        assertThat(result.exceptionOrNull())
            .isInstanceOf(InvalidLatitudeException::class.java)
    }

    @Test
    fun `should reject latitude below minimum`() {
        val result = runCatching {
            calculator.calculateDistance(
                startLatitude = INVALID_LOW_LATITUDE,
                startLongitude = VALID_LONGITUDE,
                endLatitude = VALID_LATITUDE,
                endLongitude = VALID_LONGITUDE
            )
        }

        assertThat(result.exceptionOrNull())
            .isInstanceOf(InvalidLatitudeException::class.java)
    }

    @Test
    fun `should reject longitude above maximum`() {
        val result = runCatching {
            calculator.calculateDistance(
                startLatitude = VALID_LATITUDE,
                startLongitude = INVALID_HIGH_LONGITUDE,
                endLatitude = VALID_LATITUDE,
                endLongitude = VALID_LONGITUDE
            )
        }

        assertThat(result.exceptionOrNull())
            .isInstanceOf(InvalidLongitudeException::class.java)
    }

    @Test
    fun `should reject longitude below minimum`() {
        val result = runCatching {
            calculator.calculateDistance(
                startLatitude = VALID_LATITUDE,
                startLongitude = INVALID_LOW_LONGITUDE,
                endLatitude = VALID_LATITUDE,
                endLongitude = VALID_LONGITUDE
            )
        }

        assertThat(result.exceptionOrNull())
            .isInstanceOf(InvalidLongitudeException::class.java)
    }


    @Test
    fun `should reject NaN latitude`() {
        val result = runCatching {
            calculator.calculateDistance(
                startLatitude = NON_FINITE_NAN,
                startLongitude = VALID_LONGITUDE,
                endLatitude = VALID_LATITUDE,
                endLongitude = VALID_LONGITUDE
            )
        }

        assertThat(result.exceptionOrNull())
            .isInstanceOf(InvalidLatitudeException::class.java)
    }

    @Test
    fun `should reject infinite latitude`() {
        val result = runCatching {
            calculator.calculateDistance(
                startLatitude = POSITIVE_INFINITY,
                startLongitude = VALID_LONGITUDE,
                endLatitude = VALID_LATITUDE,
                endLongitude = VALID_LONGITUDE
            )
        }

        assertThat(result.exceptionOrNull())
            .isInstanceOf(InvalidLatitudeException::class.java)
    }

    @Test
    fun `should reject negative infinite latitude`() {
        val result = runCatching {
            calculator.calculateDistance(
                startLatitude = NEGATIVE_INFINITY,
                startLongitude = VALID_LONGITUDE,
                endLatitude = VALID_LATITUDE,
                endLongitude = VALID_LONGITUDE
            )
        }

        assertThat(result.exceptionOrNull())
            .isInstanceOf(InvalidLatitudeException::class.java)
    }

    @Test
    fun `should reject NaN longitude`() {
        val result = runCatching {
            calculator.calculateDistance(
                startLatitude = VALID_LATITUDE,
                startLongitude = NON_FINITE_NAN,
                endLatitude = VALID_LATITUDE,
                endLongitude = VALID_LONGITUDE
            )
        }

        assertThat(result.exceptionOrNull())
            .isInstanceOf(InvalidLongitudeException::class.java)
    }

    @Test
    fun `should reject infinite longitude`() {
        val result = runCatching {
            calculator.calculateDistance(
                startLatitude = VALID_LATITUDE,
                startLongitude = POSITIVE_INFINITY,
                endLatitude = VALID_LATITUDE,
                endLongitude = VALID_LONGITUDE
            )
        }

        assertThat(result.exceptionOrNull())
            .isInstanceOf(InvalidLongitudeException::class.java)
    }

    @Test
    fun `should reject negative infinite longitude`() {
        val result = runCatching {
            calculator.calculateDistance(
                startLatitude = VALID_LATITUDE,
                startLongitude = NEGATIVE_INFINITY,
                endLatitude = VALID_LATITUDE,
                endLongitude = VALID_LONGITUDE
            )
        }

        assertThat(result.exceptionOrNull())
            .isInstanceOf(InvalidLongitudeException::class.java)
    }

    @Test
    fun `should accept minimum latitude`() {
        val distance = calculator.calculateDistance(
            startLatitude = MIN_LATITUDE,
            startLongitude = VALID_LONGITUDE,
            endLatitude = VALID_LATITUDE,
            endLongitude = VALID_LONGITUDE
        )

        assertThat(distance).isAtLeast(ZERO_DISTANCE)
    }

    @Test
    fun `should accept maximum latitude`() {
        val distance = calculator.calculateDistance(
            startLatitude = MAX_LATITUDE,
            startLongitude = VALID_LONGITUDE,
            endLatitude = VALID_LATITUDE,
            endLongitude = VALID_LONGITUDE
        )

        assertThat(distance).isAtLeast(ZERO_DISTANCE)
    }

    @Test
    fun `should accept minimum longitude`() {
        val distance = calculator.calculateDistance(
            startLatitude = VALID_LATITUDE,
            startLongitude = MIN_LONGITUDE,
            endLatitude = VALID_LATITUDE,
            endLongitude = VALID_LONGITUDE
        )

        assertThat(distance).isAtLeast(ZERO_DISTANCE)
    }

    @Test
    fun `should accept maximum longitude`() {
        val distance = calculator.calculateDistance(
            startLatitude = VALID_LATITUDE,
            startLongitude = MAX_LONGITUDE,
            endLatitude = VALID_LATITUDE,
            endLongitude = VALID_LONGITUDE
        )

        assertThat(distance).isAtLeast(ZERO_DISTANCE)
    }

    private companion object {
        const val VALID_LATITUDE = 31.5
        const val VALID_LONGITUDE = 34.5
        const val ZERO_DISTANCE = 0.0
        const val EQUATOR_LATITUDE = 0.0
        const val EQUATOR_LONGITUDE = 0.0
        const val ONE_DEGREE_LONGITUDE = 1.0
        const val DISTANCE_PER_LONGITUDE_DEGREE_KM = 111.2
        const val DISTANCE_TOLERANCE_KM = 0.1
        const val EAST_ANTIMERIDIAN_LONGITUDE = 179.9
        const val WEST_ANTIMERIDIAN_LONGITUDE = -179.9
        const val ANTIMERIDIAN_DISTANCE_KM = 22.2
        const val INVALID_HIGH_LATITUDE = 91.0
        const val INVALID_LOW_LATITUDE = -91.0
        const val INVALID_HIGH_LONGITUDE = 181.0
        const val INVALID_LOW_LONGITUDE = -181.0
        val NON_FINITE_NAN = Double.NaN
        val POSITIVE_INFINITY = Double.POSITIVE_INFINITY
        val NEGATIVE_INFINITY = Double.NEGATIVE_INFINITY
        const val MIN_LATITUDE = -90.0
        const val MAX_LATITUDE = 90.0
        const val MIN_LONGITUDE = -180.0
        const val MAX_LONGITUDE = 180.0
    }
}

package org.example.domain.algorithm.geohash.encoding

import org.example.domain.algorithm.geohash.core.CoordinateRange
import org.example.domain.algorithm.geohash.core.GeohashPrecision
import org.example.domain.model.exception.InvalidGeohashPrecisionException
import org.example.domain.model.exception.InvalidLatitudeException
import org.example.domain.model.exception.InvalidLongitudeException

class GeohashEncoder {

    fun encode(
        latitude: Double,
        longitude: Double,
        precision: Int
    ): String {
        validatePrecision(precision)
        validateLatitude(latitude)
        validateLongitude(longitude)

        val initialState = EncodingState(
            latRange = CoordinateRange(LAT_MIN, LAT_MAX, latitude),
            lonRange = CoordinateRange(LON_MIN, LON_MAX, longitude),
            isEven = true
        )

        return generateSequence(transition(initialState)) { step ->
            transition(step.nextState)
        }
            .map(EncodingStep::bit)
            .take(precision * BITS_PER_CHAR)
            .chunked(BITS_PER_CHAR)
            .map { bits -> bits.toBase32Char() }
            .joinToString(separator = SEPARATOR)
    }

    fun encode(
        latitude: Double,
        longitude: Double,
        precision: GeohashPrecision
    ): String =
        encode(latitude, longitude, precision.value)

    private fun transition(state: EncodingState): EncodingStep {
        val (bit, nextRange) = if (state.isEven) {
            state.lonRange.bisect()
        } else {
            state.latRange.bisect()
        }

        val nextState = if (state.isEven) {
            state.copy(
                lonRange = nextRange,
                isEven = false
            )
        } else {
            state.copy(
                latRange = nextRange,
                isEven = true
            )
        }

        return EncodingStep(
            bit = bit,
            nextState = nextState
        )
    }

    private fun List<Int>.toBase32Char(): Char {
        val index = fold(INITIAL_INDEX) { accumulator, bit ->
            (accumulator shl BITS_TO_SHIFT) or bit
        }

        return BASE32[index]
    }

    private fun validatePrecision(precision: Int) {
        precision.takeIf(GeohashPrecision::isValid)
            ?: throw InvalidGeohashPrecisionException()
    }

    private fun validateLatitude(latitude: Double) {
        if (
            latitude.isNaN() ||
            latitude.isInfinite() ||
            latitude !in LAT_MIN..LAT_MAX
        ) {
            throw InvalidLatitudeException()
        }
    }

    private fun validateLongitude(longitude: Double) {
        if (
            longitude.isNaN() ||
            longitude.isInfinite() ||
            longitude !in LON_MIN..LON_MAX
        ) {
            throw InvalidLongitudeException()
        }
    }

    private companion object {
        const val BASE32 = "0123456789bcdefghjkmnpqrstuvwxyz"
        const val BITS_PER_CHAR = 5
        const val LAT_MIN = -90.0
        const val LAT_MAX = 90.0
        const val LON_MIN = -180.0
        const val LON_MAX = 180.0
        const val SEPARATOR = ""
        const val INITIAL_INDEX = 0
        const val BITS_TO_SHIFT = 1
    }
}

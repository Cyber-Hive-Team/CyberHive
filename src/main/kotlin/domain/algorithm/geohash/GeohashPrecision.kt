package org.example.domain.algorithm.geohash

private const val CITY_PRECISION = 5
private const val STREET_PRECISION = 7

enum class GeohashPrecision(val value: Int) {
    CITY(CITY_PRECISION),
    STREET(STREET_PRECISION);

    companion object {
        fun isValid(precision: Int): Boolean =
            precision in MIN_PRECISION..MAX_PRECISION

        private const val MIN_PRECISION = 1
        private const val MAX_PRECISION = 12
    }
}

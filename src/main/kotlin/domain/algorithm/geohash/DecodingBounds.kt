package org.example.domain.algorithm.geohash

private const val LATITUDE_MIN = -90.0
private const val LATITUDE_MAX = 90.0
private const val LONGITUDE_MIN = -180.0
private const val LONGITUDE_MAX = 180.0
private const val BITS_PER_CHARACTER = 5

data class DecodingBounds(
    val latitudeMin: Double = LATITUDE_MIN,
    val latitudeMax: Double = LATITUDE_MAX,
    val longitudeMin: Double = LONGITUDE_MIN,
    val longitudeMax: Double = LONGITUDE_MAX,
    val isEven: Boolean = true
) {
    fun refine(bit: Int): DecodingBounds =
        if (isEven) refineLongitude(bit) else refineLatitude(bit)

    private fun refineLongitude(bit: Int): DecodingBounds {
        val mid = (longitudeMin + longitudeMax) / 2.0
        return if (bit == 1) copy(longitudeMin = mid, isEven = false)
        else copy(longitudeMax = mid, isEven = false)
    }

    private fun refineLatitude(bit: Int): DecodingBounds {
        val mid = (latitudeMin + latitudeMax) / 2.0
        return if (bit == 1) copy(latitudeMin = mid, isEven = true)
        else copy(latitudeMax = mid, isEven = true)
    }

    companion object {
        fun bitsOf(characterValue: Int): List<Int> =
            (BITS_PER_CHARACTER - 1 downTo 0).map { i ->
                (characterValue shr i) and 1
            }
    }
}

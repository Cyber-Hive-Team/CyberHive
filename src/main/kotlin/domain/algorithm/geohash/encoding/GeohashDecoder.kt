package org.example.domain.algorithm.geohash.encoding

import org.example.domain.algorithm.geohash.core.GeohashCell
import org.example.domain.model.exception.InvalidGeohashException

class GeohashDecoder {

    fun decode(geohash: String): GeohashCell {
        validateGeohash(geohash)
        return geohash
            .map { it.toBase32Value() }
            .flatMap(DecodingBounds::bitsOf)
            .fold(DecodingBounds(), DecodingBounds::refine)
            .toGeohashCell()
    }

    private fun validateGeohash(geohash: String) {
        if (geohash.isEmpty() || geohash.any { it !in BASE32 }) {
            throw InvalidGeohashException()
        }
    }

    private fun Char.toBase32Value(): Int = BASE32.indexOf(this)

    private companion object {
        const val BASE32 = "0123456789bcdefghjkmnpqrstuvwxyz"
    }
}

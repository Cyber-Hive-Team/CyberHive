package org.example.domain.algorithm.geohash.encoding

data class EncodingStep(
    val bit: Int,
    val nextState: EncodingState
)

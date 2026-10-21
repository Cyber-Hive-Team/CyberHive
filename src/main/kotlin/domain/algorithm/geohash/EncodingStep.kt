package org.example.domain.algorithm.geohash

data class EncodingStep(
    val bit: Int,
    val nextState: EncodingState
)

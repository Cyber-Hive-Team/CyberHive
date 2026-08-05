package org.example.domain.routing.calculator

import kotlin.math.abs

class RingSlotCalculator {

    companion object {
        private const val NUMBER_OF_SLOTS = 100
    }

    fun calculateSlot(packageId: String): Int =
        abs(packageId.hashCode() % NUMBER_OF_SLOTS)

}
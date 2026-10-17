package org.example.domain.routing.calculator

import kotlin.math.abs

private const val NUMBER_OF_SLOTS = 100

class RingSlotCalculator {

    fun calculateSlot(packageId: String): Int =
        abs(packageId.hashCode() % NUMBER_OF_SLOTS)

}
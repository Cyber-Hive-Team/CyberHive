package org.example.domain.routing.ring

import org.example.domain.model.Vehicle

class ConsistentHashingRing {

    companion object {
        private const val RING_SIZE = 100
    }

    private val vehiclesBySlot: MutableMap<Int, Vehicle> = mutableMapOf()

    fun addVehicleAtSlot(ringSlot: Int, vehicle: Vehicle) {
        vehiclesBySlot[ringSlot] = vehicle
    }

    fun findNextVehicleClockwise(startSlot: Int): Vehicle? {
        val nextVehicle = findFirstVehicleInRange(startSlot, RING_SIZE)
        if (nextVehicle != null) return nextVehicle

        return findFirstVehicleInRange(0, startSlot)
    }

    private fun findFirstVehicleInRange(
        startSlot: Int,
        endSlotExclusive: Int
    ): Vehicle? {
        for (ringSlot in startSlot until endSlotExclusive) {
            val vehicle = vehiclesBySlot[ringSlot]
            if (vehicle != null) return vehicle
        }
        return null
    }

    fun getVehiclesBySlot(): Map<Int, Vehicle> = vehiclesBySlot
}
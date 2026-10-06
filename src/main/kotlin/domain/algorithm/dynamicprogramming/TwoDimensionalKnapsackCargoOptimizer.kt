package org.example.domain.algorithm.dynamicprogramming

import org.example.domain.model.Package
import org.example.domain.model.Priority
import org.example.domain.model.Vehicle

class TwoDimensionalKnapsackCargoOptimizer {

    companion object {
        private const val LOW_PRIORITY_VALUE = 1
        private const val STANDARD_PRIORITY_VALUE = 2
        private const val URGENT_PRIORITY_VALUE = 3
    }

    fun selectOptimalPackages(
        vehicle: Vehicle,
        packages: List<Package>
    ): List<Package> {
        val validPackages = packages.filter {
            it.weight <= vehicle.maxCapacityKg &&
                    it.volumeM3 <= vehicle.maxVolumeM3
        }

        return findOptimalCombination(
            packages = validPackages,
            maxWeight = vehicle.maxCapacityKg,
            maxVolume = vehicle.maxVolumeM3
        )
    }

    private fun findOptimalCombination(
        packages: List<Package>,
        maxWeight: Double,
        maxVolume: Double
    ): List<Package> {
        val weightScale = 10
        val volumeScale = 10

        val weightCapacity = (maxWeight * weightScale).toInt()
        val volumeCapacity = (maxVolume * volumeScale).toInt()

        val dp = Array(weightCapacity + 1) {
            Array(volumeCapacity + 1) {
                Pair(0, emptyList<Int>())
            }
        }

        for (index in packages.indices) {
            val cargoPackage = packages[index]
            val packageWeight = (cargoPackage.weight * weightScale).toInt()
            val packageVolume = (cargoPackage.volumeM3 * volumeScale).toInt()
            val packagePriority = getPriorityValue(cargoPackage.priority)

            for (weight in weightCapacity downTo packageWeight) {
                for (volume in volumeCapacity downTo packageVolume) {

                    val previous = dp[weight - packageWeight][volume - packageVolume]
                    val newPriority = previous.first + packagePriority

                    if (newPriority > dp[weight][volume].first) {
                        dp[weight][volume] =
                            Pair(
                                newPriority,
                                previous.second + index
                            )
                    }
                }
            }
        }

        val selectedIndexes = dp[weightCapacity][volumeCapacity].second

        return selectedIndexes
            .map { packages[it] }
            .sortedBy { packages.indexOf(it) }
    }

    private fun getPriorityValue(priority: Priority): Int {
        return when (priority) {
            Priority.LOW -> LOW_PRIORITY_VALUE
            Priority.STANDARD -> STANDARD_PRIORITY_VALUE
            Priority.URGENT -> URGENT_PRIORITY_VALUE
        }
    }
}

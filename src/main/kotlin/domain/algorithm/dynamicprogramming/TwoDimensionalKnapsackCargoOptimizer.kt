package org.example.domain.algorithm.dynamicprogramming

import org.example.domain.model.Package
import org.example.domain.model.Priority
import org.example.domain.model.Vehicle

class TwoDimensionalKnapsackCargoOptimizer {

    companion object {
        private const val LOW_PRIORITY_VALUE = 1
        private const val STANDARD_PRIORITY_VALUE = 2
        private const val URGENT_PRIORITY_VALUE = 3
        private const val SCALE_FACTOR = 10
    }

    fun selectOptimalPackages(
        vehicle: Vehicle,
        packages: List<Package>
    ): List<Package> {
        val validPackages = filterValidPackages(vehicle, packages)

        return findOptimalCombination(
            packages = validPackages,
            maxWeight = vehicle.maxCapacityKg,
            maxVolume = vehicle.maxVolumeM3
        )
    }

    private fun filterValidPackages(
        vehicle: Vehicle,
        packages: List<Package>
    ): List<Package> {
        return packages.filter {
            it.weight <= vehicle.maxCapacityKg &&
                    it.volumeM3 <= vehicle.maxVolumeM3
        }
    }

    private fun findOptimalCombination(
        packages: List<Package>,
        maxWeight: Double,
        maxVolume: Double
    ): List<Package> {
        val weightCapacity = scaleCapacity(maxWeight)
        val volumeCapacity = scaleCapacity(maxVolume)

        val dp = createDpTable(weightCapacity, volumeCapacity)

        updateDpTable(
            dp = dp,
            packages = packages,
            weightCapacity = weightCapacity,
            volumeCapacity = volumeCapacity
        )

        return extractSelectedPackages(
            dp = dp,
            packages = packages,
            weightCapacity = weightCapacity,
            volumeCapacity = volumeCapacity
        )
    }

    private fun scaleCapacity(capacity: Double): Int {
        return (capacity * SCALE_FACTOR).toInt()
    }

    private fun createDpTable(
        weightCapacity: Int,
        volumeCapacity: Int
    ): Array<Array<Pair<Int, List<Int>>>> {
        return Array(weightCapacity + 1) {
            Array(volumeCapacity + 1) {
                Pair(0, emptyList())
            }
        }
    }

    private fun updateDpTable(
        dp: Array<Array<Pair<Int, List<Int>>>>,
        packages: List<Package>,
        weightCapacity: Int,
        volumeCapacity: Int
    ) {
        for (index in packages.indices) {
            updatePackage(
                dp = dp,
                cargoPackage = packages[index],
                packageIndex = index,
                weightCapacity = weightCapacity,
                volumeCapacity = volumeCapacity
            )
        }
    }

    private fun updatePackage(
        dp: Array<Array<Pair<Int, List<Int>>>>,
        cargoPackage: Package,
        packageIndex: Int,
        weightCapacity: Int,
        volumeCapacity: Int
    ) {
        val packageWeight = scaleCapacity(cargoPackage.weight)
        val packageVolume = scaleCapacity(cargoPackage.volumeM3)
        val packagePriority = getPriorityValue(cargoPackage.priority)

        for (weight in weightCapacity downTo packageWeight) {
            for (volume in volumeCapacity downTo packageVolume) {
                updateCell(
                    dp = dp,
                    weight = weight,
                    volume = volume,
                    packageWeight = packageWeight,
                    packageVolume = packageVolume,
                    packagePriority = packagePriority,
                    packageIndex = packageIndex
                )
            }
        }
    }

    private fun updateCell(
        dp: Array<Array<Pair<Int, List<Int>>>>,
        weight: Int,
        volume: Int,
        packageWeight: Int,
        packageVolume: Int,
        packagePriority: Int,
        packageIndex: Int
    ) {
        val previous = dp[weight - packageWeight][volume - packageVolume]
        val newPriority = previous.first + packagePriority

        if (newPriority > dp[weight][volume].first) {
            dp[weight][volume] =
                Pair(newPriority, previous.second + packageIndex)
        }
    }

    private fun extractSelectedPackages(
        dp: Array<Array<Pair<Int, List<Int>>>>,
        packages: List<Package>,
        weightCapacity: Int,
        volumeCapacity: Int
    ): List<Package> {
        return dp[weightCapacity][volumeCapacity].second
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

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

    private data class PackageInfo(
        val weight: Int,
        val volume: Int,
        val priority: Int,
        val index: Int
    )

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
        val packageInfo = PackageInfo(
            weight = scaleCapacity(cargoPackage.weight),
            volume = scaleCapacity(cargoPackage.volumeM3),
            priority = getPriorityValue(cargoPackage.priority),
            index = packageIndex
        )

        for (weight in weightCapacity downTo packageInfo.weight) {
            for (volume in volumeCapacity downTo packageInfo.volume) {
                updateCell(
                    dp = dp,
                    weight = weight,
                    volume = volume,
                    packageInfo = packageInfo
                )
            }
        }
    }

    private fun updateCell(
        dp: Array<Array<Pair<Int, List<Int>>>>,
        weight: Int,
        volume: Int,
        packageInfo: PackageInfo
    ) {
        val previous = dp[
            weight - packageInfo.weight
        ][
            volume - packageInfo.volume
        ]

        val newPriority = previous.first + packageInfo.priority

        if (newPriority > dp[weight][volume].first) {
            dp[weight][volume] =
                Pair(
                    newPriority,
                    previous.second + packageInfo.index
                )
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

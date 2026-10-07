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

    private data class Selection(
        val priority: Int,
        val weight: Int,
        val volume: Int,
        val packageIndices: List<Int>
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
    ): Array<Array<Selection>> {
        return Array(weightCapacity + 1) {
            Array(volumeCapacity + 1) {
                Selection(
                    priority = 0,
                    weight = 0,
                    volume = 0,
                    packageIndices = emptyList()
                )
            }
        }
    }

    private fun updateDpTable(
        dp: Array<Array<Selection>>,
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
        dp: Array<Array<Selection>>,
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
        dp: Array<Array<Selection>>,
        weight: Int,
        volume: Int,
        packageInfo: PackageInfo
    ) {
        val previous = dp[
            weight - packageInfo.weight
        ][
            volume - packageInfo.volume
        ]

        val newSelection = Selection(
            priority = previous.priority + packageInfo.priority,
            weight = previous.weight + packageInfo.weight,
            volume = previous.volume + packageInfo.volume,
            packageIndices = previous.packageIndices + packageInfo.index
        )

        if (isBetterSelection(newSelection, dp[weight][volume])) {
            dp[weight][volume] = newSelection
        }
    }

    private fun isBetterSelection(
        newSelection: Selection,
        currentSelection: Selection
    ): Boolean {
        val hasHigherPriority =
            newSelection.priority > currentSelection.priority

        val hasSamePriorityAndLowerWeight =
            newSelection.priority == currentSelection.priority &&
                    newSelection.weight < currentSelection.weight

        val hasSamePriorityAndWeightAndLowerVolume =
            newSelection.priority == currentSelection.priority &&
                    newSelection.weight == currentSelection.weight &&
                    newSelection.volume < currentSelection.volume

        return hasHigherPriority ||
                hasSamePriorityAndLowerWeight ||
                hasSamePriorityAndWeightAndLowerVolume
    }

    private fun extractSelectedPackages(
        dp: Array<Array<Selection>>,
        packages: List<Package>,
        weightCapacity: Int,
        volumeCapacity: Int
    ): List<Package> {
        return dp[weightCapacity][volumeCapacity]
            .packageIndices
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

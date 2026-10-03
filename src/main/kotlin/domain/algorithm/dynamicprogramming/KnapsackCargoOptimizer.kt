package org.example.domain.algorithm.dynamicprogramming

import org.example.domain.model.Package
import org.example.domain.model.Priority
import org.example.domain.model.Vehicle

class KnapsackCargoOptimizer {

    fun selectOptimalPackages(vehicle: Vehicle, packages: List<Package>): List<Package> {
        val maxPriority = calculateMaxPriority(packages)
        val grid = buildGrid(packages = packages, maxPriority = maxPriority)
        val bestPriority = findBestPriority(
            grid = grid, packageCount = packages.size, maxPriority = maxPriority,
            maxCapacityKg = vehicle.maxCapacityKg
        )
        return findSelectedPackages(packages = packages, grid = grid, bestPriority = bestPriority)
    }

    private fun calculateMaxPriority(packages: List<Package>): Int {
        return packages.sumOf { cargoPackage -> getPriorityValue(cargoPackage.priority) }
    }

    private fun buildGrid(packages: List<Package>, maxPriority: Int): Array<DoubleArray> {
        val packageCount = packages.size
        val grid = Array(packageCount + 1) {
            DoubleArray(maxPriority + 1) {
                Double.POSITIVE_INFINITY
            }
        }
        grid[0][0] = 0.0

        for (packageIndex in 1..packageCount) {
            val cargoPackage = packages[packageIndex - 1]
            val packagePriority = getPriorityValue(cargoPackage.priority)
            for (totalPriority in 0..maxPriority) {
                val withoutPackage = grid[packageIndex - 1][totalPriority]
                val withPackage = calculateWeightWithPackage(
                    grid = grid,
                    packageIndex = packageIndex,
                    totalPriority = totalPriority,
                    packagePriority = packagePriority,
                    packageWeight = cargoPackage.weight
                )

                grid[packageIndex][totalPriority] = minOf(withoutPackage, withPackage)
            }
        }
        return grid
    }

    private fun calculateWeightWithPackage(
        grid: Array<DoubleArray>, packageIndex: Int, totalPriority: Int,
        packagePriority: Int, packageWeight: Double
    ): Double {
        if (totalPriority < packagePriority) {
            return Double.POSITIVE_INFINITY
        }
        val previousWeight = grid[packageIndex - 1][totalPriority - packagePriority]
        return if (previousWeight.isFinite()) {
            previousWeight + packageWeight
        } else {
            Double.POSITIVE_INFINITY
        }
    }

    private fun findBestPriority(
        grid: Array<DoubleArray>,
        packageCount: Int,
        maxPriority: Int,
        maxCapacityKg: Double
    ): Int {
        for (totalPriority in maxPriority downTo 0) {
            if (grid[packageCount][totalPriority] <= maxCapacityKg) {
                return totalPriority
            }
        }
        return 0
    }

    private fun findSelectedPackages(
        packages: List<Package>,
        grid: Array<DoubleArray>,
        bestPriority: Int
    ): List<Package> {
        val selectedPackages = mutableListOf<Package>()
        var currentPriority = bestPriority
        for (packageIndex in packages.size downTo 1) {
            val cargoPackage = packages[packageIndex - 1]
            val packagePriority = getPriorityValue(cargoPackage.priority)
            if (currentPriority >= packagePriority) {
                val previousWeight = grid[packageIndex - 1][currentPriority - packagePriority]
                val weightWithPackage =
                    if (previousWeight.isFinite()) {
                        previousWeight + cargoPackage.weight
                    } else {
                        Double.POSITIVE_INFINITY
                    }
                if (weightWithPackage == grid[packageIndex][currentPriority]) {
                    selectedPackages.add(cargoPackage)
                    currentPriority -= packagePriority
                }
            }
        }
        return selectedPackages.reversed()
    }

    private fun getPriorityValue(priority: Priority): Int {
        return when (priority) {
            Priority.LOW -> 1
            Priority.STANDARD -> 2
            Priority.URGENT -> 3
        }
    }
}

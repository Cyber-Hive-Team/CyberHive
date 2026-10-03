package org.example.domain.algorithm.dynamicprogramming

import org.example.domain.model.Package
import org.example.domain.model.Priority
import org.example.domain.model.Vehicle

class KnapsackCargoOptimizer {

    fun selectOptimalPackages(vehicle: Vehicle, packages: List<Package>): List<Package> {
        val packageCount = packages.size
        val maxPriority = packages.sumOf { cargoPackage -> getPriorityValue(cargoPackage.priority) }
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
                val withPackage = if (totalPriority >= packagePriority) {
                    val previousWeight = grid[packageIndex - 1][totalPriority - packagePriority]
                    if (previousWeight.isFinite()) {
                        previousWeight + cargoPackage.weight
                    } else {
                        Double.POSITIVE_INFINITY
                    }
                } else {
                    Double.POSITIVE_INFINITY
                }
                grid[packageIndex][totalPriority] = minOf(withoutPackage, withPackage)
            }
        }
        var bestPriority = 0
        for (totalPriority in maxPriority downTo 0) {
            if (
                grid[packageCount][totalPriority] <= vehicle.maxCapacityKg
            ) {
                bestPriority = totalPriority
                break
            }
        }

        val selectedPackages = mutableListOf<Package>()
        var currentPriority = bestPriority
        for (packageIndex in packageCount downTo 1) {
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

                if (
                    weightWithPackage == grid[packageIndex][currentPriority]
                ) {
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


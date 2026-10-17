package org.example.domain.algorithm.sorting

import org.example.domain.model.Package
import org.example.domain.model.Priority

private const val LOW_PRIORITY_RANK = 1
private const val STANDARD_PRIORITY_RANK = 2
private const val URGENT_PRIORITY_RANK = 3

private const val START_POSITION = 0
private const val POSITION_STEP = 1

private fun getPriorityRank(priority: Priority): Int = when (priority) {
    Priority.LOW -> LOW_PRIORITY_RANK
    Priority.STANDARD -> STANDARD_PRIORITY_RANK
    Priority.URGENT -> URGENT_PRIORITY_RANK
}

private fun isHigherPriority(first: Package, second: Package): Boolean =
    getPriorityRank(first.priority) > getPriorityRank(second.priority)

private fun hasGreaterWeightThan(first: Package, second: Package): Boolean =
    first.weight > second.weight

private fun selectBetterByPriority(
    packages: MutableList<Package>,
    currentPos: Int,
    bestPos: Int
): Int {
    val current = packages[currentPos]
    val best = packages[bestPos]
    return if (isHigherPriority(current, best)) currentPos else bestPos
}

private fun selectBetterByWeight(
    packages: MutableList<Package>,
    currentPos: Int,
    bestPos: Int
): Int {
    val current = packages[currentPos]
    val best = packages[bestPos]
    return if (current.priority == best.priority && hasGreaterWeightThan(current, best)) {
        currentPos
    } else {
        bestPos
    }
}

fun sortPackagesByPriorityThenWeight(packages: MutableList<Package>) {
    if (packages.isEmpty()) return

    for (currentPos in START_POSITION until packages.lastIndex) {
        var bestPos = currentPos
        for (candidatePos in currentPos + POSITION_STEP until packages.size) {
            bestPos = selectBetterByPriority(packages, candidatePos, bestPos)
            bestPos = selectBetterByWeight(packages, candidatePos, bestPos)
        }
        // Swap the best found package with the one at the current position
        val packageToSwap = packages[currentPos]
        packages[currentPos] = packages[bestPos]
        packages[bestPos] = packageToSwap
    }
}
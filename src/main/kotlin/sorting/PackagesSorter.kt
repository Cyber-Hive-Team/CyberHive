package org.example.sorting

import org.example.data.dataholder.PackageRaw
import org.example.domain.model.Priority

private const val LOW_PRIORITY_RANK = 1
private const val STANDARD_PRIORITY_RANK = 2
private const val URGENT_PRIORITY_RANK = 3

private const val FIRST_PACKAGE_INDEX = 0
private const val NEXT_INDEX_OFFSET = 1

fun getPriorityRank(priority: Priority): Int {
    return when (priority) {
        Priority.LOW -> LOW_PRIORITY_RANK
        Priority.STANDARD -> STANDARD_PRIORITY_RANK
        Priority.URGENT -> URGENT_PRIORITY_RANK
    }
}

fun hasHigherPriority(firstPackage: PackageRaw, secondPackage: PackageRaw): Boolean {
    val firstPriorityRank = getPriorityRank(firstPackage.priority)
    val secondPriorityRank = getPriorityRank(secondPackage.priority)

    return firstPriorityRank > secondPriorityRank
}

fun hasHigherWeight(firstPackage: PackageRaw, secondPackage: PackageRaw): Boolean {
    return firstPackage.weight > secondPackage.weight
}

fun selectByPriority(
    packages: List<PackageRaw>,
    currentPackageIndex: Int,
    bestPackageIndex: Int
): Int {
    val currentPackage = packages[currentPackageIndex]
    val selectedPackage = packages[bestPackageIndex]

    return if (hasHigherPriority(currentPackage, selectedPackage)) {
        currentPackageIndex
    } else {
        bestPackageIndex
    }
}

fun selectByWeight(
    packages: List<PackageRaw>,
    currentPackageIndex: Int,
    bestPackageIndex: Int
): Int {
    val currentPackage = packages[currentPackageIndex]
    val selectedPackage = packages[bestPackageIndex]

    return if (
        currentPackage.priority == selectedPackage.priority &&
        hasHigherWeight(currentPackage, selectedPackage)
    ) {
        currentPackageIndex
    } else {
        bestPackageIndex
    }
}

fun selectionSort(packages: List<PackageRaw>): List<PackageRaw> {
    val unsortedPackages = packages.toMutableList()
    val sortedPackages = mutableListOf<PackageRaw>()

    while (unsortedPackages.isNotEmpty()) {
        var bestPackageIndex = FIRST_PACKAGE_INDEX
        val nextPackageIndex = FIRST_PACKAGE_INDEX + NEXT_INDEX_OFFSET

        for (currentPackageIndex in nextPackageIndex until unsortedPackages.size) {
            bestPackageIndex = selectByPriority(
                unsortedPackages,
                currentPackageIndex,
                bestPackageIndex
            )

            bestPackageIndex = selectByWeight(
                unsortedPackages,
                currentPackageIndex,
                bestPackageIndex
            )
        }

        sortedPackages.add(unsortedPackages[bestPackageIndex])
        unsortedPackages.removeAt(bestPackageIndex)
    }

    return sortedPackages
}

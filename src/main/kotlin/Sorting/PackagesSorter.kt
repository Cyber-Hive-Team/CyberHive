package org.example.Sorting
import org.example.Data.PackageRaw
import org.example.Data.Priority



fun getPriorityRank(priority: Priority): Int {
    return when (priority) {
        Priority.URGENT -> 3
        Priority.STANDARD -> 2
        Priority.LOW -> 1
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
fun selectByPriority(packages: List<PackageRaw>, currentPackageIndex: Int, bestPackageIndex: Int): Int {
    val currentPackage = packages[currentPackageIndex]
    val selectedPackage = packages[bestPackageIndex]
    if (hasHigherPriority(currentPackage, selectedPackage)) {
        return currentPackageIndex
    }
    return bestPackageIndex
}
fun selectByWeight(packages: List<PackageRaw>, currentPackageIndex: Int, bestPackageIndex: Int): Int {
    val currentPackage = packages[currentPackageIndex]
    val selectedPackage = packages[bestPackageIndex]
    if (currentPackage.priority == selectedPackage.priority && hasHigherWeight(currentPackage, selectedPackage)) {
        return currentPackageIndex
    }
    return bestPackageIndex
}

fun selectionSort(packages: List<PackageRaw>): List<PackageRaw> {
    val unsortedPackages = packages.toMutableList()
    val sortedPackages = mutableListOf<PackageRaw>()
    val firstPackageIndex = 0
    val nextPackageIndex = firstPackageIndex + 1
    while (unsortedPackages.isNotEmpty()) {
        var bestPackageIndex = firstPackageIndex
        for (currentPackageIndex in nextPackageIndex until unsortedPackages.size) {
            bestPackageIndex = selectByPriority(unsortedPackages, currentPackageIndex, bestPackageIndex)
            bestPackageIndex = selectByWeight(unsortedPackages, currentPackageIndex, bestPackageIndex)
        }
        sortedPackages.add(unsortedPackages[bestPackageIndex])
        unsortedPackages.removeAt(bestPackageIndex)
    }
    return sortedPackages
}


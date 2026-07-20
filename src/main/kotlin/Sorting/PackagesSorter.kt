package org.example.Sorting
import org.example.PackageRaw
import org.example.Priority
fun getPriorityRank(priority: Priority): Int {
    var priorityRank = 0
    when (priority) {
        Priority.URGENT -> priorityRank = 3
        Priority.STANDARD -> priorityRank = 2
        Priority.LOW -> priorityRank = 1
    }
    return priorityRank
}
fun comparePackagePriority(firstPackage: PackageRaw, secondPackage: PackageRaw): Boolean {
    val firstPriorityRank = getPriorityRank(firstPackage.priority)
    val secondPriorityRank = getPriorityRank(secondPackage.priority)

    return firstPriorityRank > secondPriorityRank
}
fun comparePackageWeight(firstPackage: PackageRaw, secondPackage: PackageRaw): Boolean {
    return firstPackage.weight > secondPackage.weight
}
fun selectionSortPackages(packages: List<PackageRaw>): List<PackageRaw> {
    val unsortedPackages = packages.toMutableList()
    val sortedPackages = mutableListOf<PackageRaw>()
    while (unsortedPackages.isNotEmpty()) {
        var bestPackageIndex = 0 // Assume the first package is the best initially
        // Start from index 1 because index 0 is already selected as the initial best package
        for (currentPackageIndex in 1 until unsortedPackages.size) {
            val currentPackage = unsortedPackages[currentPackageIndex]
            val selectedPackage = unsortedPackages[bestPackageIndex]
            if (comparePackagePriority(currentPackage, selectedPackage)) {
                bestPackageIndex = currentPackageIndex
            } else if (currentPackage.priority == selectedPackage.priority && comparePackageWeight(currentPackage, selectedPackage)) {
                bestPackageIndex = currentPackageIndex
            }
        }
        sortedPackages.add(unsortedPackages[bestPackageIndex])
        unsortedPackages.removeAt(bestPackageIndex)
    }
    return sortedPackages
}
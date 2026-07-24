package org.example.Sorting
import org.example.data.dataHolder.PackageRaw
import org.example.data.dataHolder.Priority


fun getPriorityRank(priority: Priority): Int {
    return when (priority) {
        Priority.URGENT -> 3
        Priority.STANDARD -> 2
        Priority.LOW -> 1
    }
}
fun comparePackagePriority(firstPackage: PackageRaw, secondPackage: PackageRaw): Boolean {
    val firstPriorityRank = getPriorityRank(firstPackage.priority)
    val secondPriorityRank = getPriorityRank(secondPackage.priority)
    return firstPriorityRank > secondPriorityRank
}
fun comparePackageWeight(firstPackage: PackageRaw, secondPackage: PackageRaw): Boolean {
    return firstPackage.weight > secondPackage.weight
}
fun sortPackagesByPriorityAndWeight(packages: List<PackageRaw>): List<PackageRaw> {
    val unsortedPackages = packages.toMutableList()
    val sortedPackages = mutableListOf<PackageRaw>()
    val firstPackageIndex = 0
    val nextPackageIndex = firstPackageIndex + 1
    while (unsortedPackages.isNotEmpty()) {
        var bestPackageIndex = firstPackageIndex
        for (currentPackageIndex in nextPackageIndex until unsortedPackages.size) {
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


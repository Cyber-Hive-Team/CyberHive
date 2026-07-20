package org.example

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
fun selectionSortPackages(packages: List<PackageRaw>): List<PackageRaw> {
    val sortedPackages = packages.toMutableList()
    for (currentIndex in 0 until sortedPackages.size - 1) {
        var bestPackageIndex = currentIndex
        val nextPackageIndex = currentIndex + 1
        for (currentPackageIndex in nextPackageIndex until sortedPackages.size) {
            val currentPackage = sortedPackages[currentPackageIndex]
            val selectedPackage = sortedPackages[bestPackageIndex]
            if (comparePackagePriority(currentPackage, selectedPackage)) {
                bestPackageIndex = currentPackageIndex
            } else if (
                currentPackage.priority == selectedPackage.priority &&
                comparePackageWeight(currentPackage, selectedPackage)
            ) {
                bestPackageIndex = currentPackageIndex
            }
        }
        val temporaryPackage = sortedPackages[currentIndex]
        sortedPackages[currentIndex] = sortedPackages[bestPackageIndex]
        sortedPackages[bestPackageIndex] = temporaryPackage
    }
    return sortedPackages
}

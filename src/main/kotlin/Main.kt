package org.example
import org.example.selectionSortPackages

const val TOP_PACKAGES_COUNT = 3
fun main() {
    val parsedPackages = parsePackages()
    val sortedPackages = selectionSortPackages(parsedPackages)

    println("Successfully parsed records: ${parsedPackages.size}")
    printTopPriorityPackages(sortedPackages)
}
fun printTopPriorityPackages(sortedPackages: List<PackageRaw>) {
    var packageIndex = 0
    while (packageIndex < TOP_PACKAGES_COUNT && packageIndex < sortedPackages.size) {
        printPackageDetails(sortedPackages[packageIndex])
        packageIndex++
    } }
fun printPackageDetails(packageItem: PackageRaw) {
    println("ID: ${packageItem.id}")
    println("Weight: ${packageItem.weight}")
    println("Destination Hub ID: ${packageItem.destinationHubId}")
    println("Priority: ${packageItem.priority}")
    println()
}
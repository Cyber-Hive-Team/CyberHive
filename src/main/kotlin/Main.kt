package org.example
import org.example.Data.PackageRaw
import org.example.DataParsing.parsePackages
import org.example.Sorting.sortPackagesByPriorityAndWeight
const val TOP_PACKAGES_COUNT = 3
fun main() {
    val parsedPackages = parsePackages()
    val sortedPackages = sortPackagesByPriorityAndWeight(parsedPackages)

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
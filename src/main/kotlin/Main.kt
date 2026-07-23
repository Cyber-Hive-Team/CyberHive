package org.example
import org.example.Data.PackageRaw
import org.example.DataParsing.parsePackages
import org.example.Sorting.selectionSort
const val TOP_PACKAGES_COUNT = 3
fun main() {
    val parsedPackages = parsePackages()
    val sortedPackages = selectionSort(parsedPackages)

    println("Successfully parsed records: ${parsedPackages.size}")
    printTopPriorityPackages(sortedPackages)
}
fun printTopPriorityPackages(sortedPackages: List<PackageRaw>) {
    var packageIndex = 0
    while (packageIndex < TOP_PACKAGES_COUNT && packageIndex < sortedPackages.size) {
        println("The Top 3 Priority packages")
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
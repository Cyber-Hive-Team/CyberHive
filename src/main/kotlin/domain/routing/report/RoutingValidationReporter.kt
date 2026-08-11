package org.example.domain.routing.report

import org.example.domain.model.Package
import org.example.domain.model.Vehicle

class RoutingValidationReporter {

    fun createReport(
        before: Map<Vehicle, List<Package>>,
        after: Map<Vehicle, List<Package>>,
        failedVehicleId: String
    ): RoutingValidationReport {
        val messages = mutableListOf<String>()
        var stablePackageCount = 0
        var reroutedPackageCount = 0
        var allPassed = true
        before.forEach { (vehicle, originalPackages) ->
            if (vehicle.id == failedVehicleId) {
                reroutedPackageCount += originalPackages.size
            } else {
                val packagesAfterFailure = after[vehicle].orEmpty()
                val packagesStayed =
                    packagesAfterFailure.containsAll(originalPackages)
                if (packagesStayed) {
                    stablePackageCount += originalPackages.size
                    messages.add("PASS: Packages on ${vehicle.id} did not move.")
                } else {
                    allPassed = false
                    messages.add("FAIL: Packages on ${vehicle.id} were moved.")
                }
            }
        }

        return RoutingValidationReport(
            allPassed = allPassed, messages = messages,
            stablePackageCount = stablePackageCount,
            reroutedPackageCount = reroutedPackageCount
        )
    }
}
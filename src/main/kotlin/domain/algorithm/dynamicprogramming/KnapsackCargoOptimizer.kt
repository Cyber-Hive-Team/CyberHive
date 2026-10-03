package org.example.domain.algorithm.dynamicprogramming

import org.example.domain.model.Package
import org.example.domain.model.Vehicle

class KnapsackCargoOptimizer {

    fun selectOptimalPackages(vehicle: Vehicle, packages: List<Package>): List<Package> {
        return packages.filter { cargoPackage -> cargoPackage.weight <= vehicle.maxCapacityKg }
    }
}

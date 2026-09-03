package org.example.domain.usecase

import org.example.domain.algorithm.search.Router
import org.example.domain.model.Package
import org.example.domain.model.Route
import org.example.domain.model.result.RoutingResult
import org.example.domain.model.Warehouse
import org.example.domain.pricing.RoutePricingEngine
import org.example.domain.repository.PackageRepository
import org.example.domain.repository.WarehouseRepository
import org.example.domain.model.input.ReroutePackageInput

class ReroutePackageUseCase(
    private val packageRepository: PackageRepository,
    private val warehouseRepository: WarehouseRepository,
    private val router: Router,
    private val pricingEngine: RoutePricingEngine
) {
    operator fun invoke(
        input : ReroutePackageInput
    ): RoutingResult? {

        val cargoPackage = fetchPackage(input.packageId)
            ?: throw NoSuchElementException("Package or Warehouse not found with provided IDs")

        val newDestination = fetchWarehouse(input.newDestinationWarehouseId)
            ?: throw NoSuchElementException("Package or Warehouse not found with provided IDs")

        val routingResult = calculateNewRoute(cargoPackage.originWarehouse, newDestination)
            ?: throw IllegalStateException("Failed to calculate new route")

        val updatedPackage = createUpdatedPackage(cargoPackage, newDestination, routingResult)

        updateCargoQueue(newDestination.id, updatedPackage)

        return routingResult
    }

    private fun fetchPackage(packageId: String): Package? {
        return packageRepository.getAllPackages().data
            .firstOrNull { it.id == packageId }
    }

    private fun fetchWarehouse(warehouseId: String): Warehouse? {
        return warehouseRepository.getWarehouseById(warehouseId)
    }

    private fun calculateNewRoute(
        originWarehouse: Warehouse,
        destinationWarehouse: Warehouse
    ): RoutingResult? {
        val routingResult = router.findPath(
            start = originWarehouse,
            destination = destinationWarehouse
        )
        return if (routingResult.path.isNotEmpty()) routingResult else null
    }

    private fun createUpdatedPackage(
        originalPackage: Package,
        newDestination: Warehouse,
        routingResult: RoutingResult
    ): Package {
        val newRoute = Route(
            id = originalPackage.id,
            originWarehouse = originalPackage.originWarehouse,
            destinationWarehouse = newDestination,
            distanceKm = routingResult.distanceKm,
            typicalDelayMin = 0
        )
        val newPrice = pricingEngine.calculatePrice(originalPackage, newRoute)

        return originalPackage.copy(
            destinationWarehouse = newDestination,
            baseRate = newPrice
        )
    }

    private fun updateCargoQueue(
        warehouseId: String,
        updatedPackage: Package
    ) {
        val isAdded = warehouseRepository.addPackageToCargoQueue(
            warehouseId,
            updatedPackage
        )
        if (isAdded) {
            warehouseRepository.sortCargoQueue(warehouseId)
        }
    }
}

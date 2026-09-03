package org.example.domain.usecase

import org.example.domain.model.input.CalculatePricingInput
import org.example.domain.pricing.RoutePricingEngine
import org.example.domain.repository.PackageRepository
import org.example.domain.repository.RouteRepository
import org.example.domain.model.result.PricingPackage

class CalculatePricingPackageUseCase(
    private val packageRepository: PackageRepository,
    private val routeRepository: RouteRepository,
    private val pricingEngine: RoutePricingEngine
) {
    operator fun invoke(
        input : CalculatePricingInput
    ): PricingPackage? {

        val cargoPackage = packageRepository.getAllPackages().data
            .firstOrNull { it.id == input.packageId }
            ?: throw NoSuchElementException("Package not found with ID: ${input.packageId}")

        val route = routeRepository.getAllRoutes().data
            .firstOrNull { it.id == input.routeId }
            ?: throw NoSuchElementException("Route not found with ID: ${input.routeId}")

        input.customStrategy?.let { strategy ->
            pricingEngine.setStrategy(strategy)
        }

        return PricingPackage(pricingEngine.calculatePrice(cargoPackage, route))

    }
}
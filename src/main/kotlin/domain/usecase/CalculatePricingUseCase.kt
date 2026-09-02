package org.example.domain.usecase

import org.example.domain.pricing.DispatchStrategy
import org.example.domain.pricing.RoutePricingEngine
import org.example.domain.repository.PackageRepository
import org.example.domain.repository.RouteRepository

class CalculatePricingUseCase(
    private val packageRepository: PackageRepository,
    private val routeRepository: RouteRepository,
    private val pricingEngine: RoutePricingEngine
) {
    operator fun invoke(
        packageId: String,
        routeId: String,
        customStrategy: DispatchStrategy? = null
    ): Double? {

        val cargoPackage = packageRepository.getAllPackages().data
            .firstOrNull { it.id == packageId } ?: return null

        val route = routeRepository.getAllRoutes().data
            .firstOrNull { it.id == routeId } ?: return null

        customStrategy?.let { strategy ->
            pricingEngine.setStrategy(strategy)
        }

        return pricingEngine.calculatePrice(cargoPackage, route)

    }
}
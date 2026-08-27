package org.example.domain.usecase

import org.example.domain.model.Package
import org.example.domain.model.PackageComponent
import org.example.domain.model.Route
import org.example.domain.pricing.DispatchStrategy
import org.example.domain.pricing.RoutePricingEngine
import org.example.domain.pricing.StrategyToDecoratorBridge

class CalculatePricingUseCase(
    private val pricingEngine: RoutePricingEngine
) {

    operator fun invoke(
        cargoPackage: Package,
        route: Route,
        strategy: DispatchStrategy,
        decorators: List<(PackageComponent) -> PackageComponent> = emptyList()
    ): Double {

        pricingEngine.setStrategy(strategy)

        val basePrice =
            pricingEngine.calculatePrice(
                cargoPackage,
                route
            )

        val baseComponent: PackageComponent =
            StrategyToDecoratorBridge(basePrice)

        val finalComponent =
            decorators.fold(baseComponent) { component, decorator ->
                decorator(component)
            }

        return finalComponent.calculateTransitRate()
    }
}
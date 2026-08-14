package org.example.domain.pricing

import org.example.domain.model.BaseCost
import org.example.domain.model.PackageComponent

class StrategyToDecoratorBridge(
    basePrice: Double
) : PackageComponent {

    private val baseCost = BaseCost(
        cost = basePrice,
        description = "Base Cost from Strategy"
    )

    override fun calculateTransitRate(): Double {
        return baseCost.calculateTransitRate()
    }

    override fun getDescription(): String {
        return baseCost.getDescription()
    }
}
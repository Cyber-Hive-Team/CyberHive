package org.example.domain.decorator

import org.example.domain.model.PackageComponent

private const val COLD_CHAIN_MULTIPLIER = 1.5

class ColdChainDecorator(decoratedPackage: PackageComponent) : PackageDecorator(decoratedPackage) {
    override fun calculateTransitRate(): Double {
        return decoratedPackage.calculateTransitRate() * COLD_CHAIN_MULTIPLIER
    }

    override fun getDescription(): String {
        return decoratedPackage.getDescription() + ", Cold Chain"
    }
}
package org.example.domain.decorator

import org.example.domain.model.PackageComponent

class FragileHandlingDecorator(
    component: PackageComponent
) : PackageDecorator(component) {

    private val protectiveFee = 15.0

    override fun calculateTransitRate(): Double {
        return super.calculateTransitRate() + protectiveFee
    }

    override fun getDescription(): String {
        return super.getDescription() + " + Fragile Handling"
    }
}
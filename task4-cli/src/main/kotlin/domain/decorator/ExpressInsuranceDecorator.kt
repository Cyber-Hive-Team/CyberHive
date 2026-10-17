package org.example.domain.decorator

import org.example.domain.model.PackageComponent

class ExpressInsuranceDecorator(
    component: PackageComponent
) : PackageDecorator(component) {

    private val insuranceFee = 10.0

    override fun calculateTransitRate(): Double {
        return super.calculateTransitRate() + insuranceFee
    }

    override fun getDescription(): String {
        return super.getDescription() + " + ExpressInsuranceDecorator"
    }
}
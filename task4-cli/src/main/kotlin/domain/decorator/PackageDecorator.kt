package org.example.domain.decorator

import org.example.domain.model.PackageComponent

abstract class PackageDecorator(val decoratedPackage: PackageComponent) : PackageComponent {
    override fun calculateTransitRate(): Double {
        return decoratedPackage.calculateTransitRate()
    }
    override fun getDescription(): String {
        return decoratedPackage.getDescription()
    }
}
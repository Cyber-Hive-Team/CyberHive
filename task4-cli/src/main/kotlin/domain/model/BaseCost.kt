package org.example.domain.model

class BaseCost(
    private val cost: Double,
    private val description: String
) : PackageComponent {

    override fun calculateTransitRate(): Double {
        return cost
    }

    override fun getDescription(): String {
        return description
    }
}
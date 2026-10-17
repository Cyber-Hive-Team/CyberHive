package org.example.domain.model.input

import org.example.domain.pricing.DispatchStrategy

data class CalculatePricingInput(
    val packageId: String,
    val routeId: String,
    val customStrategy: DispatchStrategy? = null
)

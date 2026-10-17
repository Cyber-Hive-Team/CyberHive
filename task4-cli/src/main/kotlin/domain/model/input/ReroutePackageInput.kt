package org.example.domain.model.input

data class ReroutePackageInput(
    val packageId: String,
    val newDestinationWarehouseId: String
)

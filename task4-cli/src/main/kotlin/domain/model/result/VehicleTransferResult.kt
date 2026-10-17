package org.example.domain.model.result

data class VehicleTransferResult(
    val vehicleId: String,
    val fromWarehouseId: String,
    val toWarehouseId: String,
    val capacityKg: Double
)

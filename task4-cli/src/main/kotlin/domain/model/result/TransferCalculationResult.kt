package org.example.domain.model.result

data class TransferCalculationResult(
    val transfers: List<VehicleTransferResult>,
    val remainingShortage: Double
)

package org.example.domain.usecase

import org.example.domain.model.result.FleetShortageResult
import org.example.domain.model.result.FleetSurplusResult
import org.example.domain.model.result.TransferCalculationResult
import org.example.domain.model.result.VehicleTransferResult
import org.example.domain.repository.VehicleRepository

private const val ZERO_VALUE = 0.0

class RedistributeFleetUseCase(
    private val findFleetShortageUseCase: FindFleetShortageUseCase,
    private val findFleetSurplusUseCase: FindFleetSurplusUseCase,
    private val vehicleRepository: VehicleRepository
) {

    operator fun invoke(): List<VehicleTransferResult> {
        val shortages = findFleetShortageUseCase()
        val surpluses = findFleetSurplusUseCase()
        return distributeVehicles(
            shortages = shortages,
            surpluses = surpluses
        )
    }

    private fun distributeVehicles(
        shortages: List<FleetShortageResult>,
        surpluses: List<FleetSurplusResult>
    ): List<VehicleTransferResult> {

        val remainingSurplus = surpluses
                .associate { surplus ->
                    surplus.warehouseId to surplus.surplusKg
                }
                .toMutableMap()

        return shortages.flatMap { shortage ->
            distributeForShortage(
                shortage = shortage,
                surpluses = surpluses,
                remainingSurplus = remainingSurplus
            )
        }
    }

    private fun distributeForShortage(
        shortage: FleetShortageResult,
        surpluses: List<FleetSurplusResult>,
        remainingSurplus: MutableMap<String, Double>
    ): List<VehicleTransferResult> {
        var remainingShortage = shortage.shortageKg

        return surpluses.flatMap { surplus ->
            if (remainingShortage <= ZERO_VALUE) {
                emptyList()
            } else {
                val result = transferFromSurplus(
                    shortage = shortage,
                    surplus = surplus,
                    remainingShortage = remainingShortage,
                    remainingSurplus = remainingSurplus
                )
                remainingShortage = result.remainingShortage
                result.transfers
            }
        }
    }

    private fun transferFromSurplus(
        shortage: FleetShortageResult, surplus: FleetSurplusResult,
        remainingShortage: Double, remainingSurplus: MutableMap<String, Double>
    ): TransferCalculationResult {
        var shortageLeft = remainingShortage
        var surplusLeft = remainingSurplus[surplus.warehouseId] ?: ZERO_VALUE
        if (surplusLeft <= ZERO_VALUE) {
            return TransferCalculationResult(transfers = emptyList(), remainingShortage = shortageLeft)
        }
        val vehicles = vehicleRepository
            .getVehiclesByWarehouseId(surplus.warehouseId)
            .data
            .sortedByDescending { vehicle ->
                vehicle.maxCapacityKg
            }
        val transfers = vehicles.mapNotNull { vehicle ->
            val canTransfer = shortageLeft > ZERO_VALUE && surplusLeft >= vehicle.maxCapacityKg
            if (!canTransfer) {
                return@mapNotNull null
            }
            val transfer = transferVehicle(
                vehicleId = vehicle.id, vehicleCapacity = vehicle.maxCapacityKg,
                fromWarehouseId = surplus.warehouseId, toWarehouseId = shortage.warehouseId
            )
            shortageLeft -= vehicle.maxCapacityKg
            surplusLeft -= vehicle.maxCapacityKg
            transfer
            }
        remainingSurplus[surplus.warehouseId] = surplusLeft
        return TransferCalculationResult(transfers = transfers, remainingShortage = shortageLeft)
    }

    private fun transferVehicle(
        vehicleId: String,
        vehicleCapacity: Double,
        fromWarehouseId: String,
        toWarehouseId: String
    ): VehicleTransferResult? {
        val reassigned = vehicleRepository.reassignVehicle(vehicleId = vehicleId, warehouseId = toWarehouseId)
        if (!reassigned) {
            return null
        }
        return VehicleTransferResult(
            vehicleId = vehicleId,
            fromWarehouseId = fromWarehouseId,
            toWarehouseId = toWarehouseId,
            capacityKg = vehicleCapacity
        )
    }
}




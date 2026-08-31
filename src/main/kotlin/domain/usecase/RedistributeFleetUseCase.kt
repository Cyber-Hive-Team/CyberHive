package org.example.domain.usecase

import org.example.domain.repository.VehicleRepository

private const val ZERO_VALUE = 0.0

class RedistributeFleetUseCase(
    private val findFleetShortageUseCase: FindFleetShortageUseCase,
    private val findFleetSurplusUseCase: FindFleetSurplusUseCase,
    private val vehicleRepository: VehicleRepository
) {

    operator fun invoke(): List<VehicleTransfer> {

        val shortages =
            findFleetShortageUseCase()

        val surpluses =
            findFleetSurplusUseCase()

        return distributeVehicles(
            shortages = shortages,
            surpluses = surpluses
        )
    }

    private fun distributeVehicles(
        shortages: List<FleetShortageResult>,
        surpluses: List<FleetSurplusResult>
    ): List<VehicleTransfer> {

        val transfers =
            mutableListOf<VehicleTransfer>()

        val remainingSurplus =
            surpluses
                .associate { surplus ->
                    surplus.warehouseId to surplus.surplusKg
                }
                .toMutableMap()

        shortages.forEach { shortage ->

            var remainingShortage =
                shortage.shortageKg

            surpluses.forEach { surplus ->

                if (remainingShortage <= ZERO_VALUE) {
                    return@forEach
                }

                var sourceSurplus =
                    remainingSurplus[surplus.warehouseId]
                        ?: ZERO_VALUE

                if (sourceSurplus <= ZERO_VALUE) {
                    return@forEach
                }

                val vehicles =
                    vehicleRepository
                        .getVehiclesByWarehouseId(
                            surplus.warehouseId
                        )
                        .data
                        .sortedByDescending { vehicle ->
                            vehicle.maxCapacityKg
                        }

                vehicles.forEach { vehicle ->

                    val canTransfer =
                        remainingShortage > ZERO_VALUE &&
                                sourceSurplus >=
                                vehicle.maxCapacityKg

                    if (canTransfer) {

                        val reassigned =
                            vehicleRepository
                                .reassignVehicle(
                                    vehicleId = vehicle.id,
                                    warehouseId =
                                        shortage.warehouseId
                                )

                        if (reassigned) {

                            transfers.add(
                                VehicleTransfer(
                                    vehicleId = vehicle.id,
                                    fromWarehouseId =
                                        surplus.warehouseId,
                                    toWarehouseId =
                                        shortage.warehouseId,
                                    capacityKg =
                                        vehicle.maxCapacityKg
                                )
                            )

                            remainingShortage -=
                                vehicle.maxCapacityKg

                            sourceSurplus -=
                                vehicle.maxCapacityKg

                            remainingSurplus[
                                surplus.warehouseId
                            ] = sourceSurplus
                        }
                    }
                }
            }
        }

        return transfers
    }
}

data class VehicleTransfer(
    val vehicleId: String,
    val fromWarehouseId: String,
    val toWarehouseId: String,
    val capacityKg: Double
)
package org.example.data.validation

import org.example.data.dataholder.VehicleRaw
import org.example.data.exception.CurrentHubNotFoundDataException
import org.example.data.exception.InvalidCostPerKmDataException
import org.example.data.exception.InvalidCurrentHubDataException
import org.example.data.exception.InvalidVehicleCapacityDataException
import org.example.data.exception.InvalidVehicleIdDataException
import org.example.domain.model.Warehouse

class VehicleValidator {

    fun validate(
        raw: VehicleRaw,
        currentWarehouse: Warehouse?
    ): DataValidationResult {

        val violations = mutableListOf<DataFieldViolation>()

        validateId(raw)?.let { violations.add(it) }
        validateCurrentHubId(raw)?.let { violations.add(it) }
        validateCurrentWarehouse(currentWarehouse)?.let { violations.add(it) }
        validateCapacity(raw)?.let { violations.add(it) }
        validateCost(raw)?.let { violations.add(it) }

        return violations.toResult()
    }

    private fun validateId(raw: VehicleRaw): DataFieldViolation? =
        if (raw.id.isBlank()) {
            DataFieldViolation(
                "id",
                InvalidVehicleIdDataException().message.orEmpty())
        }else null

    private fun validateCurrentHubId(raw: VehicleRaw): DataFieldViolation? =
        if (raw.currentHubId.isBlank()){
            DataFieldViolation(
                "currentHubId",
                InvalidCurrentHubDataException().message.orEmpty())
        }else null

    private fun validateCurrentWarehouse(warehouse: Warehouse?): DataFieldViolation? =
        if (warehouse == null){
            DataFieldViolation(
                "currentHubId",
                CurrentHubNotFoundDataException().message.orEmpty())
        }else null

    private fun validateCapacity(raw: VehicleRaw): DataFieldViolation? =
        if (raw.maxCapacityKg <= 0){
            DataFieldViolation(
                "maxCapacityKg",
                InvalidVehicleCapacityDataException().message.orEmpty())
        }else null

    private fun validateCost(raw: VehicleRaw): DataFieldViolation? =
        if (raw.costPerKm < 0){
            DataFieldViolation(
                "costPerKm",
                InvalidCostPerKmDataException().message.orEmpty())
        }else null
}

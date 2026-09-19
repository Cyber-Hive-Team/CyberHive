package org.example.data.validation

import org.example.data.dataholder.RouteRaw
import org.example.data.exception.InvalidDelayDataException
import org.example.data.exception.InvalidDistanceDataException
import org.example.data.exception.InvalidRouteIdDataException
import org.example.domain.model.Warehouse

class RouteValidator(
    private val warehouseReferenceValidator: WarehouseReferenceValidator
) {

    fun validate(
        raw: RouteRaw,
        originWarehouse: Warehouse?,
        destinationWarehouse: Warehouse?
    ): DataValidationResult {

        val violations = mutableListOf<DataFieldViolation>()

        validateId(raw)?.let { violations.add(it) }
        validateDistance(raw)?.let { violations.add(it) }
        validateDelay(raw)?.let { violations.add(it) }

        violations.addAll(
            warehouseReferenceValidator.validate(
                originHubId = raw.originHubId,
                destinationHubId = raw.destinationHubId,
                originWarehouse = originWarehouse,
                destinationWarehouse = destinationWarehouse
            )
        )

        return violations.toResult()
    }

    private fun validateId(raw: RouteRaw): DataFieldViolation? =
        if (raw.id.isBlank()) {
            DataFieldViolation(
                "id",
                InvalidRouteIdDataException().message.orEmpty())
        }else null

    private fun validateDistance(raw: RouteRaw): DataFieldViolation? =
        if (raw.distanceKm <= 0){
            DataFieldViolation(
                "distanceKm",
                InvalidDistanceDataException().message.orEmpty())
        } else null

    private fun validateDelay(raw: RouteRaw): DataFieldViolation? =
        if (raw.typicalDelayMin < 0) {
            DataFieldViolation(
                "typicalDelayMin",
                InvalidDelayDataException().message.orEmpty()
            )
        }else null
}

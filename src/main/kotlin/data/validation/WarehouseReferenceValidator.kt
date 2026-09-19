package org.example.data.validation

import org.example.data.exception.DestinationWarehouseNotFoundDataException
import org.example.data.exception.InvalidDestinationWarehouseIdDataException
import org.example.data.exception.InvalidOriginWarehouseIdDataException
import org.example.data.exception.OriginWarehouseNotFoundDataException
import org.example.data.exception.SameWarehouseDataException
import org.example.domain.model.Warehouse

class WarehouseReferenceValidator {

    fun validate(
        originHubId: String,
        destinationHubId: String,
        originWarehouse: Warehouse?,
        destinationWarehouse: Warehouse?
    ): List<DataFieldViolation> {

        val violations = mutableListOf<DataFieldViolation>()

        validateOriginId(originHubId)?.let { violations.add(it) }
        validateDestinationId(destinationHubId)?.let { violations.add(it) }
        validateOriginWarehouse(originWarehouse)?.let { violations.add(it) }
        validateDestinationWarehouse(destinationWarehouse)?.let { violations.add(it) }
        validateDifferentWarehouses(originWarehouse, destinationWarehouse)?.let { violations.add(it) }

        return violations
    }

    private fun validateOriginId(originHubId: String): DataFieldViolation? =
        if (originHubId.isBlank()) {
            DataFieldViolation(
                "originHubId",
                InvalidOriginWarehouseIdDataException().message.orEmpty())
        } else null

    private fun validateDestinationId(destinationHubId: String): DataFieldViolation? =
        if (destinationHubId.isBlank()) {
            DataFieldViolation(
                "destinationHubId",
                InvalidDestinationWarehouseIdDataException().message.orEmpty())
        } else null

    private fun validateOriginWarehouse(warehouse: Warehouse?): DataFieldViolation? =
        if (warehouse == null) {
            DataFieldViolation(
                "originHubId",
                OriginWarehouseNotFoundDataException().message.orEmpty())
        } else null

    private fun validateDestinationWarehouse(warehouse: Warehouse?): DataFieldViolation? =
        if (warehouse == null) {
            DataFieldViolation(
                "destinationHubId",
                DestinationWarehouseNotFoundDataException().message.orEmpty())
        } else null

    private fun validateDifferentWarehouses(
        originWarehouse: Warehouse?,
        destinationWarehouse: Warehouse?
    ): DataFieldViolation? =
        if (
            originWarehouse != null &&
            destinationWarehouse != null &&
            originWarehouse.id == destinationWarehouse.id
        ){
            DataFieldViolation(
                "originHubId/destinationHubId",
                SameWarehouseDataException().message.orEmpty()
            )
        } else null

}

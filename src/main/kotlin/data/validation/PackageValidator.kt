package org.example.data.validation

import org.example.data.dataholder.PackageRaw
import org.example.data.exception.InvalidPackageIdDataException
import org.example.data.exception.InvalidPackageWeightDataException
import org.example.domain.model.Warehouse

class PackageValidator(
    private val warehouseReferenceValidator: WarehouseReferenceValidator
) {

    fun validate(
        raw: PackageRaw,
        originWarehouse: Warehouse?,
        destinationWarehouse: Warehouse?
    ): DataValidationResult {

        val violations = mutableListOf<DataFieldViolation>()

        validateId(raw)?.let { violations.add(it) }
        validateWeight(raw)?.let { violations.add(it) }

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

    private fun validateId(raw: PackageRaw): DataFieldViolation? =
        if (raw.id.isBlank()) {
            DataFieldViolation(
                "id",
                InvalidPackageIdDataException().message.orEmpty())
        }else null

    private fun validateWeight(raw: PackageRaw): DataFieldViolation? =
        if (raw.weight <= 0)
        {
            DataFieldViolation(
                "weight",
                InvalidPackageWeightDataException().message.orEmpty())
        } else null
}

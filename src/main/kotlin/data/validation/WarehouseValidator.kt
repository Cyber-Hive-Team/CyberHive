package org.example.data.validation

import org.example.data.dataholder.WareHouseRaw
import org.example.data.exception.InvalidLatitudeDataException
import org.example.data.exception.InvalidLongitudeDataException
import org.example.data.exception.InvalidWarehouseIdDataException
import org.example.data.exception.InvalidWarehouseNameDataException
import org.example.data.exception.MissingLatitudeDataException
import org.example.data.exception.MissingLongitudeDataException

class WarehouseValidator {

    fun validate(raw: WareHouseRaw): DataValidationResult {

        val violations = mutableListOf<DataFieldViolation>()

        validateId(raw)?.let { violations.add(it) }
        validateName(raw)?.let { violations.add(it) }
        validateLatitude(raw)?.let { violations.add(it) }
        validateLongitude(raw)?.let { violations.add(it) }

        return violations.toResult()
    }

    private fun validateId(raw: WareHouseRaw): DataFieldViolation? =
        if (raw.id.isBlank()) {
            DataFieldViolation(
                "id",
                InvalidWarehouseIdDataException().message.orEmpty())
        }else null

    private fun validateName(raw: WareHouseRaw): DataFieldViolation? =
        if (raw.name.isBlank()) {
            DataFieldViolation(
                "name",
                InvalidWarehouseNameDataException().message.orEmpty())
        }else null

    private fun validateLatitude(raw: WareHouseRaw): DataFieldViolation? =
        when {
            raw.latitude == null -> {
                DataFieldViolation(
                    "latitude",
                    MissingLatitudeDataException().message.orEmpty()
                )
            }
            raw.latitude !in -90.0..90.0 -> {
                DataFieldViolation(
                    "latitude",
                    InvalidLatitudeDataException().message.orEmpty()
                )

            }else -> null
        }

    private fun validateLongitude(raw: WareHouseRaw): DataFieldViolation? =
        when {
            raw.longitude == null -> {
                DataFieldViolation(
                    "longitude",
                    MissingLongitudeDataException().message.orEmpty()
                )
            }

            raw.longitude !in -180.0..180.0 -> {
                DataFieldViolation(
                    "longitude",
                    InvalidLongitudeDataException().message.orEmpty()
                )

            }else -> null
        }
}

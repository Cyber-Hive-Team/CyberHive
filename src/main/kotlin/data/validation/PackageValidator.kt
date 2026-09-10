package org.example.data.validation

import org.example.data.dataholder.PackageRaw
import org.example.domain.model.Warehouse

class PackageValidator {

    fun validate(
        raw: PackageRaw,
        origin: Warehouse?,
        destination: Warehouse?
    ): List<String> {
        val warnings = mutableListOf<String>()
        if (raw.id.isBlank()) {
            warnings.add("Warning: Package skipped - missing id")
        }
        if (origin == null) {
            warnings.add(
                "Warning: Package ${raw.id} skipped - " +
                        "origin warehouse not found: ${raw.originHubId}"
            )
        }
        if (destination == null) {
            warnings.add(
                "Warning: Package ${raw.id} skipped - " +
                        "destination warehouse not found: ${raw.destinationHubId}"
            )
        }
        return warnings

    }
}

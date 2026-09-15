package org.example.data.validation

import org.example.data.dataholder.RouteRaw
import org.example.domain.model.Warehouse

class RouteValidator {

    fun validate(
        raw: RouteRaw,
        origin: Warehouse?,
        destination: Warehouse?
    ): List<String> {
        val warnings = mutableListOf<String>()

        if (raw.id.isBlank()) {
            warnings.add("Warning: Route skipped - missing id")
        }

        if (origin == null) {
            warnings.add(
                "Warning: Route ${raw.id} skipped - " +
                        "origin warehouse not found: ${raw.originHubId}"
            )
        }

        if (destination == null) {
            warnings.add(
                "Warning: Route ${raw.id} skipped - " +
                        "destination warehouse not found: ${raw.destinationHubId}"
            )
        }

        if (raw.distanceKm <= 0) {
            warnings.add(
                "Warning: Route ${raw.id} skipped - invalid distance"
            )
        }

        if (raw.typicalDelayMin < 0) {
            warnings.add(
                "Warning: Route ${raw.id} skipped - invalid delay"
            )
        }

        return warnings
    }
}

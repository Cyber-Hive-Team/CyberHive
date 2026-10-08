package org.example.data.mapper.sync

import org.example.data.dataholder.PackageRaw
import org.example.data.dataholder.RouteRaw
import org.example.data.dataholder.VehicleRaw
import org.example.data.dataholder.WarehouseRaw
import org.example.data.remote.dto.response.PackageResponseDto
import org.example.data.remote.dto.response.RouteResponseDto
import org.example.data.remote.dto.response.VehicleResponseDto
import org.example.data.remote.dto.response.WarehouseResponseDto
import org.example.domain.model.Priority
import org.example.domain.model.RegionalZone

fun PackageResponseDto.toRaw(): PackageRaw {
    return PackageRaw(
        id = id,
        weight = weight,
        originHubId = originHubId,
        destinationHubId = destinationHubId,
        priority = priority
            ?.let { Priority.valueOf(it) }
            ?: Priority.LOW
    )
}

fun RouteResponseDto.toRaw(): RouteRaw {
    return RouteRaw(
        id = routeId,
        originHubId = originHubId,
        destinationHubId = destinationHubId,
        distanceKm = distanceKm,
        typicalDelayMin = typicalDelayMin.requiredField(
            "Route '$routeId' has null typicalDelayMin."
        )
    )
}

fun VehicleResponseDto.toRaw(): VehicleRaw {
    return VehicleRaw(
        id = vehicleId,
        currentHubId = currentHubId,
        maxCapacityKg = maxCapacityKg,
        costPerKm = costPerKm.requiredField(
            "Vehicle '$vehicleId' has null costPerKm."
        )
    )
}

fun WarehouseResponseDto.toRaw(): WarehouseRaw {
    return WarehouseRaw(
        id = id,
        name = name,
        regionalZone = RegionalZone.valueOf(
            regionalZone.requiredField(
                "Warehouse '$id' has null regionalZone."
            )
        ),
        latitude = latitude.requiredField(
            "Warehouse '$id' has null latitude."
        ),
        longitude = longitude.requiredField(
            "Warehouse '$id' has null longitude."
        )
    )
}

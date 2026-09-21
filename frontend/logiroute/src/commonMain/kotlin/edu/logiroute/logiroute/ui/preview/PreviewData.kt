package edu.logiroute.logiroute.ui.preview

import org.example.domain.model.Package
import org.example.domain.model.Priority
import org.example.domain.model.RegionalZone
import org.example.domain.model.Warehouse

val previewOriginWarehouse = Warehouse(
    id = "WH-001",
    name = "North Hub",
    regionalZone = RegionalZone.NORTH,
    latitude = 33.5,
    longitude = 36.3
)

val previewDestinationWarehouse = Warehouse(
    id = "WH-002",
    name = "Central Hub",
    regionalZone = RegionalZone.CENTRAL,
    latitude = 34.0,
    longitude = 36.8
)

val previewWestWarehouse = Warehouse(
    id = "WH-003",
    name = "West Distribution Center",
    regionalZone = RegionalZone.WEST,
    latitude = 33.8,
    longitude = 35.9
)

val previewUrgentPackage = Package(
    id = "PKG-000001",
    weight = 8.5,
    priority = Priority.URGENT,
    originWarehouse = previewOriginWarehouse,
    destinationWarehouse = previewDestinationWarehouse
)

val previewStandardPackage = Package(
    id = "PKG-000002",
    weight = 3.2,
    priority = Priority.STANDARD,
    originWarehouse = previewOriginWarehouse,
    destinationWarehouse = previewDestinationWarehouse
)

val previewLowPackage = Package(
    id = "PKG-000003",
    weight = 15.0,
    priority = Priority.LOW,
    originWarehouse = previewOriginWarehouse,
    destinationWarehouse = previewDestinationWarehouse
)

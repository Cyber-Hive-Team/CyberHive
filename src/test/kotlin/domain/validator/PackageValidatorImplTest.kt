package org.example.test.domain.validator

import org.example.domain.model.Package
import org.example.domain.model.Priority
import org.example.domain.model.RegionalZone
import org.example.domain.model.Warehouse
import org.example.domain.validator.ValidationResult
import org.example.domain.validator.impl.PackageValidatorImpl
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.Test

class PackageValidatorImplTest {

    private val validator = PackageValidatorImpl()

    @Test
    fun `validateCreate returns success when package data is valid`() {
        // Given
        val origin = Warehouse(
            id = "WH-001", name = "Origin Warehouse", regionalZone = RegionalZone.NORTH,
            latitude = 31.5, longitude = 34.5
        )

        val destination = Warehouse(
            id = "WH-002", name = "Destination Warehouse", regionalZone = RegionalZone.SOUTH,
            latitude = 31.3, longitude = 34.3
        )

        val validPackage = Package(
            id = "PKG-000001", weight = 5.0, priority = Priority.STANDARD,
            originWarehouse = origin, destinationWarehouse = destination, baseRate = 10.0
        )

        // When
        val result = validator.validateCreate(validPackage)

        // Then
        Assertions.assertEquals(ValidationResult.Success, result)
    }
}

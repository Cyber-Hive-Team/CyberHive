package org.example.test.domain.validator

import org.example.domain.model.Package
import org.example.domain.model.Priority
import org.example.domain.model.RegionalZone
import org.example.domain.model.Warehouse
import org.example.domain.model.input.UpdatePackageInput
import org.example.domain.validator.FieldError
import org.example.domain.validator.ValidationResult
import org.example.domain.validator.impl.PackageValidatorImpl
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.Test


class PackageValidatorImplTest {

    private val validator = PackageValidatorImpl()

    @Test
    fun `validateCreate returns success when package data is valid`() {
        // Given
        val validPackage = createPackage()

        // When
        val result = validator.validateCreate(validPackage)

        // Then
        Assertions.assertEquals(ValidationResult.Success, result)
    }

    @Test
    fun `validateCreate returns invalid base rate when base rate is negative`() {
        // Given
        val invalidPackage = createPackage(baseRate = -1.0)

        // When
        val result = validator.validateCreate(invalidPackage)

        // Then
        assertFailureFields(result, FieldError.InvalidBaseRate)
    }

    @Test
    fun `validateCreate returns success when base rate is zero`() {
        // Given
        val validPackage = createPackage(baseRate = 0.0)

        // When
        val result = validator.validateCreate(validPackage)

        // Then
        Assertions.assertEquals(ValidationResult.Success, result)
    }

    @Test
    fun `validateCreate returns same warehouse when warehouse ids match`() {
        // Given
        val invalidPackage = createPackage(
            origin = createWarehouse(id = "WH-001"),
            destination = createWarehouse(id = "WH-001")
        )

        // When
        val result = validator.validateCreate(invalidPackage)

        // Then
        assertFailureFields(result, FieldError.SameWarehouse)
    }

    @Test
    fun `validateCreate collects base rate and warehouse violations`() {
        // Given
        val invalidPackage = createPackage(
            baseRate = -1.0,
            origin = createWarehouse(id = "WH-001"),
            destination = createWarehouse(id = "WH-001")
        )

        // When
        val result = validator.validateCreate(invalidPackage)

        // Then
        assertFailureFields(
            result,
            FieldError.InvalidBaseRate,
            FieldError.SameWarehouse
        )
    }

    @Test
    fun `validateUpdate returns success when weight is positive`() {
        // Given
        val input = createUpdateInput(weight = 5.0)

        // When
        val result = validator.validateUpdate(input)

        // Then
        Assertions.assertEquals(ValidationResult.Success, result)
    }

    @Test
    fun `validateUpdate returns success when only priority is updated`() {
        // Given
        val input = createUpdateInput(
            weight = null,
            priority = Priority.URGENT
        )

        // When
        val result = validator.validateUpdate(input)

        // Then
        Assertions.assertEquals(ValidationResult.Success, result)
    }

    @Test
    fun `validateUpdate returns success when weight and priority are updated`() {
        // Given
        val input = createUpdateInput(
            weight = 8.0,
            priority = Priority.URGENT
        )

        // When
        val result = validator.validateUpdate(input)

        // Then
        Assertions.assertEquals(ValidationResult.Success, result)
    }

    @Test
    fun `validateUpdate returns invalid weight when weight is zero`() {
        // Given
        val input = createUpdateInput(weight = 0.0)

        // When
        val result = validator.validateUpdate(input)

        // Then
        assertFailureFields(result, FieldError.InvalidWeight)
    }

    @Test
    fun `validateUpdate returns invalid weight when weight is negative`() {
        // Given
        val input = createUpdateInput(weight = -1.0)

        // When
        val result = validator.validateUpdate(input)

        // Then
        assertFailureFields(result, FieldError.InvalidWeight)
    }

    @Test
    fun `validateUpdate returns success when weight is a positive fraction`() {
        // Given
        val input = createUpdateInput(weight = 0.1)

        // When
        val result = validator.validateUpdate(input)

        // Then
        Assertions.assertEquals(ValidationResult.Success, result)
    }

    @Test
    fun `validateUpdate returns no update fields when weight and priority are null`() {
        // Given
        val input = createUpdateInput(
            weight = null,
            priority = null
        )

        // When
        val result = validator.validateUpdate(input)

        // Then
        assertFailureFields(result, FieldError.NoUpdateFields)
    }

    @Test
    fun `validateUpdate returns same warehouse when warehouse ids match`() {
        // Given
        val input = createUpdateInput(
            weight = 5.0,
            origin = createWarehouse(id = "WH-001"),
            destination = createWarehouse(id = "WH-001")
        )

        // When
        val result = validator.validateUpdate(input)

        // Then
        assertFailureFields(result, FieldError.SameWarehouse)
    }

    @Test
    fun `validateUpdate rejects invalid weight even when priority is provided`() {
        // Given
        val input = createUpdateInput(
            weight = -1.0,
            priority = Priority.URGENT
        )

        // When
        val result = validator.validateUpdate(input)

        // Then
        assertFailureFields(result, FieldError.InvalidWeight)
    }

    @Test
    fun `validateUpdate collects weight and warehouse violations`() {
        // Given
        val input = createUpdateInput(
            weight = -1.0,
            origin = createWarehouse(id = "WH-001"),
            destination = createWarehouse(id = "WH-001")
        )

        // When
        val result = validator.validateUpdate(input)

        // Then
        assertFailureFields(
            result,
            FieldError.InvalidWeight,
            FieldError.SameWarehouse
        )
    }

    @Test
    fun `validateUpdate collects missing update fields and warehouse violations`() {
        // Given
        val input = createUpdateInput(
            weight = null,
            priority = null,
            origin = createWarehouse(id = "WH-001"),
            destination = createWarehouse(id = "WH-001")
        )

        // When
        val result = validator.validateUpdate(input)

        // Then
        assertFailureFields(
            result,
            FieldError.NoUpdateFields,
            FieldError.SameWarehouse
        )
    }

    private fun createWarehouse(
        id: String
    ): Warehouse {
        return Warehouse(
            id = id,
            name = "Test Warehouse",
            regionalZone = RegionalZone.NORTH,
            latitude = 31.5,
            longitude = 34.5
        )
    }

    private fun createPackage(
        baseRate: Double = 10.0,
        origin: Warehouse = createWarehouse("WH-001"),
        destination: Warehouse = createWarehouse("WH-002")
    ): Package {
        return Package(
            id = "PKG-000001",
            weight = 5.0,
            priority = Priority.STANDARD,
            originWarehouse = origin,
            destinationWarehouse = destination,
            baseRate = baseRate
        )
    }

    private fun createUpdateInput(
        weight: Double? = null,
        priority: Priority? = null,
        origin: Warehouse = createWarehouse("WH-001"),
        destination: Warehouse = createWarehouse("WH-002")
    ): UpdatePackageInput {
        return UpdatePackageInput(
            id = "PKG-000001",
            weight = weight,
            priority = priority,
            originWarehouse = origin,
            destinationWarehouse = destination
        )
    }

    private fun assertFailureFields(
        result: ValidationResult,
        vararg expectedFields: FieldError
    ) {
        val failure = Assertions.assertInstanceOf(
            ValidationResult.Failure::class.java,
            result
        )

        val actualFields = failure.violations.map { it.field }

        Assertions.assertEquals(
            expectedFields.size,
            actualFields.size
        )

        Assertions.assertEquals(
            expectedFields.toSet(),
            actualFields.toSet()
        )
    }
}

package org.example.test.domain.validator.impl

import org.example.domain.model.Package
import org.example.domain.model.Priority
import org.example.domain.model.RegionalZone
import org.example.domain.model.Warehouse
import org.example.domain.model.exception.DomainException
import org.example.domain.model.input.UpdatePackageInput
import org.example.domain.validator.result.ValidationResult
import org.example.domain.validator.result.FieldViolation
import org.example.domain.validator.result.FieldError
import org.example.domain.validator.PackageValidator
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.Test

class PackageValidatorImplTest {

    private val validator = PackageValidator()

    @Test
    fun `accepts valid package creation`() {
        // Given
        val cargoPackage = createPackage()

        // When
        val result = validator.validateCreate(cargoPackage)

        // Then
        Assertions.assertEquals(ValidationResult.Success, result)
    }

    @Test
    fun `accepts zero base rate`() {
        // Given
        val cargoPackage = createPackage(baseRate = 0.0)

        // When
        val result = validator.validateCreate(cargoPackage)

        // Then
        Assertions.assertEquals(ValidationResult.Success, result)
    }

    @Test
    fun `rejects negative base rate`() {
        // Given
        val cargoPackage = createPackage(baseRate = -1.0)

        // When
        val result = validator.validateCreate(cargoPackage)

        // Then
        Assertions.assertEquals(
            ValidationResult.Failure(
                listOf(
                    FieldViolation(
                        FieldError.InvalidBaseRate,
                        DomainException.INVALID_BASE_RATE
                    )
                )
            ),
            result
        )
    }

    @Test
    fun `rejects creation with identical warehouses`() {
        // Given
        val warehouse = createWarehouse("WH-001")
        val cargoPackage = createPackage(
            origin = warehouse,
            destination = warehouse
        )

        // When
        val result = validator.validateCreate(cargoPackage)

        // Then
        Assertions.assertEquals(
            ValidationResult.Failure(
                listOf(
                    FieldViolation(
                        FieldError.SameWarehouse,
                        DomainException.SAME_WAREHOUSE
                    )
                )
            ),
            result
        )
    }

    @Test
    fun `accepts valid weight update`() {
        // Given
        val input = createUpdateInput(weight = 8.0)

        // When
        val result = validator.validateUpdate(input)

        // Then
        Assertions.assertEquals(ValidationResult.Success, result)
    }

    @Test
    fun `accepts priority-only update`() {
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
    fun `rejects update with no weight or priority`() {
        // Given
        val input = createUpdateInput(weight = null)

        // When
        val result = validator.validateUpdate(input)

        // Then
        Assertions.assertEquals(
            ValidationResult.Failure(
                listOf(
                    FieldViolation(
                        FieldError.NoUpdateFields,
                        DomainException.NO_UPDATE_FIELDS
                    )
                )
            ),
            result
        )
    }

    @Test
    fun `rejects zero weight on update`() {
        // Given
        val input = createUpdateInput(weight = 0.0)

        // When
        val result = validator.validateUpdate(input)

        // Then
        Assertions.assertEquals(
            ValidationResult.Failure(
                listOf(
                    FieldViolation(
                        FieldError.InvalidWeight,
                        DomainException.INVALID_PACKAGE_WEIGHT
                    )
                )
            ),
            result
        )
    }

    @Test
    fun `rejects negative weight on update`() {
        // Given
        val input = createUpdateInput(weight = -1.0)

        // When
        val result = validator.validateUpdate(input)

        // Then
        Assertions.assertEquals(
            ValidationResult.Failure(
                listOf(
                    FieldViolation(
                        FieldError.InvalidWeight,
                        DomainException.INVALID_PACKAGE_WEIGHT
                    )
                )
            ),
            result
        )
    }

    @Test
    fun `reports weight and warehouse violations together`() {
        // Given
        val warehouse = createWarehouse("WH-001")
        val input = UpdatePackageInput(
            id = "PKG-000001",
            weight = -1.0,
            originWarehouse = warehouse,
            destinationWarehouse = warehouse
        )

        // When
        val result = validator.validateUpdate(input)

        // Then
        Assertions.assertEquals(
            ValidationResult.Failure(
                listOf(
                    FieldViolation(
                        FieldError.InvalidWeight,
                        DomainException.INVALID_PACKAGE_WEIGHT
                    ),
                    FieldViolation(
                        FieldError.SameWarehouse,
                        DomainException.SAME_WAREHOUSE
                    )
                )
            ),
            result
        )
    }

    private fun createUpdateInput(
        weight: Double?,
        priority: Priority? = null
    ): UpdatePackageInput = UpdatePackageInput(
        id = "PKG-000001",
        weight = weight,
        priority = priority,
        originWarehouse = createWarehouse("WH-001"),
        destinationWarehouse = createWarehouse("WH-002")
    )

    private fun createPackage(
        baseRate: Double = 10.0,
        origin: Warehouse = createWarehouse("WH-001"),
        destination: Warehouse = createWarehouse("WH-002")
    ): Package = Package(
        id = "PKG-000001",
        weight = 5.0,
        priority = Priority.STANDARD,
        originWarehouse = origin,
        destinationWarehouse = destination,
        baseRate = baseRate
    )

    private fun createWarehouse(id: String): Warehouse = Warehouse(
        id = id,
        name = "Warehouse $id",
        regionalZone = RegionalZone.NORTH,
        latitude = 31.5,
        longitude = 34.5
    )
}

package org.example.test.domain.algorithm.knn

import org.example.domain.algorithm.knn.KnnSectorClassifier
import org.example.domain.model.Package
import org.example.domain.model.Priority
import org.example.domain.model.RegionalZone
import org.example.domain.model.Warehouse
import org.example.domain.model.PackageSectorInput
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertThrows
import org.junit.jupiter.api.Test

class KnnSectorClassifierTest {

    private val classifier = KnnSectorClassifier()

    private fun warehouse(
        id: String,
        zone: RegionalZone,
        latitude: Double,
        longitude: Double
    ) = Warehouse(
        id = id,
        name = "Warehouse $id",
        regionalZone = zone,
        latitude = latitude,
        longitude = longitude
    )

    private fun cargoPackage(
        id: String,
        zone: RegionalZone,
        latitude: Double,
        longitude: Double,
        weight: Double
    ) = Package(
        id = id,
        weight = weight,
        priority = Priority.STANDARD,
        originWarehouse = warehouse(
            "WH-001",
            RegionalZone.CENTRAL,
            31.0,
            34.0
        ),
        destinationWarehouse = warehouse(
            id = warehouseId(id),
            zone = zone,
            latitude = latitude,
            longitude = longitude
        )
    )

    private fun warehouseId(packageId: String): String {
        val number = packageId.removePrefix("PKG-").toInt()
        return "WH-${number.toString().padStart(3, '0')}"
    }

    private fun trainingPackages() = listOf(
        cargoPackage(
            "PKG-000001", RegionalZone.NORTH,
            32.0, 35.0, 10.0
        ),
        cargoPackage(
            "PKG-000002", RegionalZone.NORTH,
            32.1, 35.1, 11.0
        ),
        cargoPackage(
            "PKG-000003", RegionalZone.NORTH,
            32.2, 35.2, 12.0
        ),
        cargoPackage(
            "PKG-000004", RegionalZone.NORTH,
            32.3, 35.3, 13.0
        ),
        cargoPackage(
            "PKG-000005", RegionalZone.NORTH,
            32.4, 35.4, 14.0
        ),
        cargoPackage(
            "PKG-000006", RegionalZone.SOUTH,
            28.0, 34.0, 50.0
        )
    )

    private fun input() = PackageSectorInput(
        packageId = "PKG-000099",
        latitude = 32.15,
        longitude = 35.15,
        weight = 11.5
    )

    @Test
    fun `predicts majority zone of five nearest neighbors`() {
        // Given
        val records = trainingPackages()

        // When
        val result = classifier.classify(input(), records)

        // Then
        assertEquals(RegionalZone.NORTH, result)
    }

    @Test
    fun `uses all records when fewer than five exist`() {
        // Given
        val records = trainingPackages().take(3)

        // When
        val result = classifier.classify(input(), records)

        // Then
        assertEquals(RegionalZone.NORTH, result)
    }

    @Test
    fun `throws when historical data is empty`() {
        // Given
        val historicalData = emptyList<Package>()

        // When
        val action = {
            classifier.classify(input(), historicalData)
        }

        // Then
        assertThrows(IllegalArgumentException::class.java) {
            action()
        }
    }

    @Test
    fun `handles identical feature ranges without division by zero`() {
        // Given
        val records = listOf(
            cargoPackage(
                "PKG-000011", RegionalZone.WEST,
                31.0, 34.0, 10.0
            ),
            cargoPackage(
                "PKG-000012", RegionalZone.WEST,
                31.0, 34.0, 10.0
            )
        )
        val query = PackageSectorInput(
            packageId = "PKG-000099",
            latitude = 31.0,
            longitude = 34.0,
            weight = 10.0
        )

        // When
        val result = classifier.classify(query, records)

        // Then
        assertEquals(RegionalZone.WEST, result)
    }
}

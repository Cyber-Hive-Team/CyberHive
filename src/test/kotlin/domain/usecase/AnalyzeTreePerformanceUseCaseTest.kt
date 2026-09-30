package org.example.domain.usecase

import org.example.domain.model.exception.InvalidPackageCountException
import org.example.domain.model.input.AnalyzeTreePerformanceInput
import org.junit.jupiter.api.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertTrue

class AnalyzeTreePerformanceUseCaseTest {

    private val useCase = AnalyzeTreePerformanceUseCase()

    @Test
    fun `single package requires one search step in both trees`() {
        // Given
        val input = AnalyzeTreePerformanceInput(
            firstPackageNumber = 1,
            packageCount = 1,
            trackingIdWidth = 6
        )

        // When
        val result = useCase(input)

        // Then
        assertEquals("PKG-000001", result.targetTrackingId)
        assertEquals(1, result.packageCount)
        assertEquals(1, result.unbalancedSearchSteps)
        assertEquals(1, result.avlSearchSteps)
    }

    @Test
    fun `target is last generated tracking id`() {
        // Given
        val input = AnalyzeTreePerformanceInput(
            firstPackageNumber = 10,
            packageCount = 3,
            trackingIdWidth = 6
        )

        // When
        val result = useCase(input)

        // Then
        assertEquals("PKG-000012", result.targetTrackingId)
    }

    @Test
    fun `sequential insertion requires fewer search steps in AVL tree`() {
        // Given
        val input = AnalyzeTreePerformanceInput(
            firstPackageNumber = 1,
            packageCount = 7,
            trackingIdWidth = 6
        )

        // When
        val result = useCase(input)

        // Then
        assertEquals(7, result.unbalancedSearchSteps)
        assertTrue(result.avlSearchSteps > 0)
        assertTrue(
            result.avlSearchSteps < result.unbalancedSearchSteps
        )
    }

    @Test
    fun `tracking id uses requested width`() {
        // Given
        val input = AnalyzeTreePerformanceInput(
            firstPackageNumber = 5,
            packageCount = 1,
            trackingIdWidth = 4
        )

        // When
        val result = useCase(input)

        // Then
        assertEquals("PKG-0005", result.targetTrackingId)
    }

    @Test
    fun `zero package count throws InvalidPackageCountException`() {
        // Given
        val input = AnalyzeTreePerformanceInput(
            firstPackageNumber = 1,
            packageCount = 0,
            trackingIdWidth = 6
        )

        // When / Then
        assertFailsWith<InvalidPackageCountException> {
            useCase(input)
        }
    }

    @Test
    fun `negative package count throws InvalidPackageCountException`() {
        // Given
        val input = AnalyzeTreePerformanceInput(
            firstPackageNumber = 1,
            packageCount = -1,
            trackingIdWidth = 6
        )

        // When / Then
        assertFailsWith<InvalidPackageCountException> {
            useCase(input)
        }
    }
}

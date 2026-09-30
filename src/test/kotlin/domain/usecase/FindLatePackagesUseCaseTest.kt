package org.example.test.domain.usecase

import io.mockk.coEvery
import io.mockk.mockk
import kotlin.time.Duration.Companion.minutes
import kotlin.time.Instant
import kotlinx.coroutines.runBlocking
import org.example.domain.model.input.PackageDeliveryTime
import org.example.domain.model.result.LatePackageResult
import org.example.domain.repository.PackageRepository
import org.example.domain.usecase.FindLatePackagesUseCase
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertSame
import org.junit.jupiter.api.Test

class FindLatePackagesUseCaseTest {

    private val repository = mockk<PackageRepository>()
    private val useCase = FindLatePackagesUseCase(repository)

    private val expectedArrival =
        Instant.parse("2026-09-30T10:00:00Z")

    @Test
    fun `returns late packages ordered by delay minutes`() = runBlocking {
        // Given
        val deliveries = listOf(
            createDelivery("PKG-000001", 30),
            createDelivery("PKG-000002", 0),
            createDelivery("PKG-000003", 90),
            createDelivery("PKG-000004", -15)
        )

        coEvery {
            repository.getAllDeliveryTimes()
        } returns Result.success(deliveries)

        val expected = listOf(
            LatePackageResult("PKG-000003", 90L),
            LatePackageResult("PKG-000001", 30L)
        )

        // When
        val result = useCase()

        // Then
        assertEquals(expected, result.getOrThrow())
    }

    @Test
    fun `excludes deliveries on time or early`() = runBlocking {
        // Given
        val deliveries = listOf(
            createDelivery("PKG-000001", 0),
            createDelivery("PKG-000002", -30)
        )

        coEvery {
            repository.getAllDeliveryTimes()
        } returns Result.success(deliveries)

        // When
        val result = useCase()

        // Then
        assertEquals(
            emptyList<LatePackageResult>(),
            result.getOrThrow()
        )
    }

    @Test
    fun `preserves repository failure`() = runBlocking {
        // Given
        val error = IllegalStateException("Read failed")

        coEvery {
            repository.getAllDeliveryTimes()
        } returns Result.failure(error)

        // When
        val result = useCase()

        // Then
        assertSame(error, result.exceptionOrNull())
    }

    private fun createDelivery(
        packageId: String,
        delayMinutes: Int
    ): PackageDeliveryTime = PackageDeliveryTime(
        packageId = packageId,
        expectedArrivalTime = expectedArrival,
        actualArrivalTime = expectedArrival + delayMinutes.minutes
    )
}

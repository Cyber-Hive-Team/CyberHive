package org.example.test.domain.usecase

import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import io.mockk.mockkStatic
import io.mockk.unmockkStatic
import java.time.LocalDateTime
import kotlinx.coroutines.runBlocking
import org.example.domain.model.PackageWarehouseStay
import org.example.domain.model.exception.InvalidWaitingHoursException
import org.example.domain.model.result.WaitingPackageResult
import org.example.domain.repository.PackageRepository
import org.example.domain.usecase.FindPackagesWaitingTooLongInWarehouseUseCase
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertInstanceOf
import org.junit.jupiter.api.Assertions.assertSame
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test

class FindPackagesWaitingTooLongInWarehouseUseCaseTest {

    private val repository = mockk<PackageRepository>()
    private val useCase =
        FindPackagesWaitingTooLongInWarehouseUseCase(repository)

    private val now = LocalDateTime.of(2026, 9, 30, 12, 0)

    @BeforeEach
    fun fixCurrentTime() {
        mockkStatic(LocalDateTime::class)
        every { LocalDateTime.now() } returns now
    }

    @AfterEach
    fun restoreCurrentTime() {
        unmockkStatic(LocalDateTime::class)
    }

    @Test
    fun `returns overdue packages ordered by waiting hours`() = runBlocking {
        // Given
        val stays = listOf(
            PackageWarehouseStay("PKG-000001", now.minusHours(30)),
            PackageWarehouseStay("PKG-000002", now.minusHours(10)),
            PackageWarehouseStay("PKG-000003", now.minusHours(50))
        )

        coEvery {
            repository.getAllWarehouseStays()
        } returns Result.success(stays)

        val expected = listOf(
            WaitingPackageResult("PKG-000003", 50L),
            WaitingPackageResult("PKG-000001", 30L)
        )

        // When
        val result = useCase(maxWaitingHours = 24L)

        // Then
        assertEquals(expected, result.getOrThrow())
    }

    @Test
    fun `excludes packages at or below waiting limit`() = runBlocking {
        // Given
        val stays = listOf(
            PackageWarehouseStay("PKG-000001", now.minusHours(24)),
            PackageWarehouseStay("PKG-000002", now.minusHours(5))
        )

        coEvery {
            repository.getAllWarehouseStays()
        } returns Result.success(stays)

        // When
        val result = useCase(maxWaitingHours = 24L)

        // Then
        assertEquals(
            emptyList<WaitingPackageResult>(),
            result.getOrThrow()
        )
    }

    @Test
    fun `rejects negative limit before reading repository`() = runBlocking {
        // Given
        val invalidLimit = -1L

        // When
        val result = useCase(maxWaitingHours = invalidLimit)

        // Then
        assertInstanceOf(
            InvalidWaitingHoursException::class.java,
            result.exceptionOrNull()
        )

        coVerify(exactly = 0) {
            repository.getAllWarehouseStays()
        }
    }

    @Test
    fun `preserves repository failure`() = runBlocking {
        // Given
        val error = IllegalStateException("Read failed")

        coEvery {
            repository.getAllWarehouseStays()
        } returns Result.failure(error)

        // When
        val result = useCase(maxWaitingHours = 24L)

        // Then
        assertSame(error, result.exceptionOrNull())
    }
}

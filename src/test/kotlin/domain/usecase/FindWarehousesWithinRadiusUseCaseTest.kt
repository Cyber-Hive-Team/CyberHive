package org.example.test.domain.usecase

import com.google.common.truth.Truth.assertThat
import io.mockk.coEvery
import io.mockk.every
import io.mockk.justRun
import io.mockk.mockk
import io.mockk.verify
import kotlinx.coroutines.test.runTest
import org.example.domain.algorithm.geohash.spatial.GeographicDistanceCalculator
import org.example.domain.algorithm.geohash.spatial.WarehouseSpatialIndex
import org.example.domain.model.RegionalZone
import org.example.domain.model.Warehouse
import org.example.domain.model.exception.InvalidLatitudeException
import org.example.domain.model.input.FindWarehouseWithinRadiusInput
import org.example.domain.repository.WarehouseRepository
import org.example.domain.usecase.FindWarehousesWithinRadiusUseCase
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test

class FindWarehousesWithinRadiusUseCaseTest {

    private val warehouseRepository = mockk<WarehouseRepository>()
    private val warehouseSpatialIndex = mockk<WarehouseSpatialIndex>()
    private val geographicDistanceCalculator =
        mockk<GeographicDistanceCalculator>()

    private val useCase = FindWarehousesWithinRadiusUseCase(
        warehouseRepository = warehouseRepository,
        warehouseSpatialIndex = warehouseSpatialIndex,
        geographicDistanceCalculator = geographicDistanceCalculator
    )

    private val input = FindWarehouseWithinRadiusInput(
        latitude = SEARCH_LATITUDE,
        longitude = SEARCH_LONGITUDE,
        radiusKm = SEARCH_RADIUS_KM
    )

    private lateinit var nearbyWarehouse: Warehouse
    private lateinit var distantWarehouse: Warehouse

    @BeforeEach
    fun setUp() {
        nearbyWarehouse = warehouseAt(
            id = NEARBY_WAREHOUSE_ID,
            warehouseLatitude = NEARBY_LATITUDE,
            warehouseLongitude = NEARBY_LONGITUDE
        )

        distantWarehouse = warehouseAt(
            id = DISTANT_WAREHOUSE_ID,
            warehouseLatitude = DISTANT_LATITUDE,
            warehouseLongitude = DISTANT_LONGITUDE
        )

        justRun { warehouseSpatialIndex.clear() }
        justRun { warehouseSpatialIndex.add(any()) }
    }

    @Test
    fun `returns warehouses within the specified radius`() = runTest {
        // Given
        givenWarehouses(listOf(nearbyWarehouse, distantWarehouse))
        givenCandidates(listOf(nearbyWarehouse, distantWarehouse))
        stubDistance(nearbyWarehouse, NEARBY_DISTANCE_KM)
        stubDistance(distantWarehouse, DISTANT_DISTANCE_KM)

        // When
        val result = useCase(input)

        // Then
        assertThat(result.getOrThrow())
            .containsExactly(nearbyWarehouse)
    }

    @Test
    fun `returns empty list when no warehouses are within radius`() = runTest {
        // Given
        givenWarehouses(listOf(distantWarehouse))
        givenCandidates(listOf(distantWarehouse))
        stubDistance(distantWarehouse, DISTANT_DISTANCE_KM)

        // When
        val result = useCase(input)

        // Then
        assertThat(result.getOrThrow()).isEmpty()
    }

    @Test
    fun `returns empty list when there are no warehouses`() = runTest {
        // Given
        givenWarehouses(emptyList())
        givenCandidates(emptyList())

        // When
        val result = useCase(input)

        // Then
        assertThat(result.getOrThrow()).isEmpty()
    }

    @Test
    fun `propagates repository failure`() = runTest {
        // Given
        val exception = IllegalStateException(REPOSITORY_ERROR)
        coEvery {
            warehouseRepository.getAllWarehouses()
        } returns Result.failure(exception)

        // When
        val result = useCase(input)

        // Then
        assertThat(result.exceptionOrNull()).isSameInstanceAs(exception)
    }

    @Test
    fun `adds repository warehouses to spatial index`() = runTest {
        // Given
        givenWarehouses(listOf(nearbyWarehouse, distantWarehouse))
        givenCandidates(emptyList())

        // When
        useCase(input)

        // Then
        verify(exactly = 1) { warehouseSpatialIndex.add(nearbyWarehouse) }
        verify(exactly = 1) { warehouseSpatialIndex.add(distantWarehouse) }
    }

    @Test
    fun `includes warehouse when distance equals search radius`() = runTest {
        // Given
        givenWarehouses(listOf(nearbyWarehouse))
        givenCandidates(listOf(nearbyWarehouse))
        stubDistance(nearbyWarehouse, SEARCH_RADIUS_KM)

        // When
        val result = useCase(input)

        // Then
        assertThat(result.getOrThrow())
            .containsExactly(nearbyWarehouse)
    }

    @Test
    fun `does not calculate distances when there are no candidates`() = runTest {
        // Given
        givenWarehouses(emptyList())
        givenCandidates(emptyList())

        // When
        useCase(input)

        // Then
        verify(exactly = 0) {
            geographicDistanceCalculator.calculateDistance(
                startLatitude = SEARCH_LATITUDE,
                startLongitude = SEARCH_LONGITUDE,
                endLatitude = any(),
                endLongitude = any()
            )
        }
    }

    @Test
    fun `returns failure when distance calculation throws an exception`() = runTest {
        // Given
        givenWarehouses(listOf(nearbyWarehouse))
        givenCandidates(listOf(nearbyWarehouse))
        val exception = InvalidLatitudeException()
        every {
            geographicDistanceCalculator.calculateDistance(
                startLatitude = SEARCH_LATITUDE,
                startLongitude = SEARCH_LONGITUDE,
                endLatitude = nearbyWarehouse.latitude,
                endLongitude = nearbyWarehouse.longitude
            )
        } throws exception

        // When
        val result = useCase(input)

        // Then
        assertThat(result.exceptionOrNull()).isSameInstanceAs(exception)
    }

    private fun givenWarehouses(warehouses: List<Warehouse>) {
        coEvery {
            warehouseRepository.getAllWarehouses()
        } returns Result.success(warehouses)
    }

    private fun givenCandidates(warehouses: List<Warehouse>) {
        every {
            warehouseSpatialIndex.findCandidatesWithinRadius(
                latitude = SEARCH_LATITUDE,
                longitude = SEARCH_LONGITUDE,
                radiusKm = SEARCH_RADIUS_KM
            )
        } returns warehouses
    }

    private fun stubDistance(warehouse: Warehouse, distanceKm: Double) {
        every {
            geographicDistanceCalculator.calculateDistance(
                startLatitude = SEARCH_LATITUDE,
                startLongitude = SEARCH_LONGITUDE,
                endLatitude = warehouse.latitude,
                endLongitude = warehouse.longitude
            )
        } returns distanceKm
    }

    private fun warehouseAt(
        id: String,
        warehouseLatitude: Double,
        warehouseLongitude: Double
    ): Warehouse {
        return Warehouse(
            id = id,
            name = WAREHOUSE_NAME,
            regionalZone = RegionalZone.NORTH,
            latitude = warehouseLatitude,
            longitude = warehouseLongitude
        )
    }

    private companion object {
        const val SEARCH_LATITUDE = 31.5
        const val SEARCH_LONGITUDE = 34.5
        const val SEARCH_RADIUS_KM = 5.0
        const val NEARBY_LATITUDE = 31.51
        const val NEARBY_LONGITUDE = 34.51
        const val NEARBY_DISTANCE_KM = 2.0
        const val DISTANT_LATITUDE = 31.8
        const val DISTANT_LONGITUDE = 34.8
        const val DISTANT_DISTANCE_KM = 10.0
        const val NEARBY_WAREHOUSE_ID = "WH-001"
        const val DISTANT_WAREHOUSE_ID = "WH-002"
        const val WAREHOUSE_NAME = "Test Warehouse"
        const val REPOSITORY_ERROR = "Unable to retrieve warehouses"
    }
}

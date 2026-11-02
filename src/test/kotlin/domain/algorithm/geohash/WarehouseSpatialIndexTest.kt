package org.example.test.domain.algorithm.geohash

import com.google.common.truth.Truth.assertThat
import org.example.domain.algorithm.geohash.encoding.GeohashDecoder
import org.example.domain.algorithm.geohash.core.GeohashDirection
import org.example.domain.algorithm.geohash.encoding.GeohashEncoder
import org.example.domain.algorithm.geohash.spatial.GeohashNeighborCalculator
import org.example.domain.algorithm.geohash.core.GeohashPrecision
import org.example.domain.algorithm.geohash.search.GeohashSearchBoundsCalculator
import org.example.domain.algorithm.geohash.spatial.WarehouseSpatialIndex
import org.example.domain.model.RegionalZone
import org.example.domain.model.Warehouse
import org.example.domain.model.exception.InvalidSearchRadiusException
import org.junit.jupiter.api.Test

class WarehouseSpatialIndexTest {

    private val index = WarehouseSpatialIndex(
        precision = GeohashPrecision.CITY,
        neighborCalculator = GeohashNeighborCalculator(),
        boundsCalculator = GeohashSearchBoundsCalculator()
    )

    @Test
    fun `should find warehouse after adding it`() {
        val warehouse = createWarehouse(WAREHOUSE_ID)

        index.add(warehouse)

        assertThat(index.findByPrefix(ORIGINAL_GEOHASH))
            .containsExactly(warehouse)
    }

    @Test
    fun `should not return warehouse for unmatched prefix`() {
        index.add(createWarehouse(WAREHOUSE_ID))

        assertThat(index.findByPrefix(UNMATCHED_PREFIX)).isEmpty()
    }

    @Test
    fun `should not duplicate warehouse when adding same id again`() {
        val warehouse = createWarehouse(WAREHOUSE_ID)

        index.add(warehouse)
        index.add(warehouse)

        assertThat(index.findByPrefix(ORIGINAL_GEOHASH))
            .containsExactly(warehouse)
    }

    @Test
    fun `should remove warehouse from index`() {
        index.add(createWarehouse(WAREHOUSE_ID))

        assertThat(index.remove(WAREHOUSE_ID)).isTrue()
    }

    @Test
    fun `should return false when removing unknown warehouse`() {
        assertThat(index.remove(UNKNOWN_WAREHOUSE_ID)).isFalse()
    }

    @Test
    fun `should remove warehouse from prefix after removal`() {
        index.add(createWarehouse(WAREHOUSE_ID))
        index.remove(WAREHOUSE_ID)

        assertThat(index.findByPrefix(ORIGINAL_GEOHASH)).isEmpty()
    }

    @Test
    fun `should remove warehouse from its previous prefix when updated`() {
        val original = createWarehouse(WAREHOUSE_ID)
        val updated = createWarehouse(
            id = WAREHOUSE_ID,
            latitude = UPDATED_LATITUDE,
            longitude = UPDATED_LONGITUDE
        )

        index.add(original)
        index.update(updated)

        assertThat(index.findByPrefix(ORIGINAL_GEOHASH)).isEmpty()
    }

    @Test
    fun `should find updated warehouse under its new prefix`() {
        val updated = createWarehouse(
            id = WAREHOUSE_ID,
            latitude = UPDATED_LATITUDE,
            longitude = UPDATED_LONGITUDE
        )
        val updatedGeohash = GeohashEncoder().encode(
            latitude = UPDATED_LATITUDE,
            longitude = UPDATED_LONGITUDE,
            precision = GeohashPrecision.CITY.value
        )

        index.add(createWarehouse(WAREHOUSE_ID))
        index.update(updated)

        assertThat(index.findByPrefix(updatedGeohash))
            .containsExactly(updated)
    }

    @Test
    fun `should find warehouse in the query cell`() {
        val warehouse = createWarehouse(WAREHOUSE_ID)
        index.add(warehouse)

        val results = index.findInNeighborhood(
            latitude = warehouse.latitude,
            longitude = warehouse.longitude
        )

        assertThat(results).containsExactly(warehouse)
    }

    @Test
    fun `should find warehouse in neighboring cell`() {
        val encoder = GeohashEncoder()
        val decoder = GeohashDecoder()
        val neighborCalculator = GeohashNeighborCalculator(
            encoder,
            decoder
        )
        val queryGeohash = encoder.encode(
            QUERY_LATITUDE,
            QUERY_LONGITUDE,
            GeohashPrecision.CITY.value
        )
        val eastNeighbor = neighborCalculator
            .calculateNeighbors(queryGeohash)
            .getValue(GeohashDirection.EAST)
        val neighborCell = decoder.decode(eastNeighbor)

        val warehouse = createWarehouse(
            id = NEIGHBOR_WAREHOUSE_ID,
            latitude = neighborCell.centerLatitude,
            longitude = neighborCell.centerLongitude
        )
        index.add(warehouse)

        val results = index.findInNeighborhood(
            latitude = QUERY_LATITUDE,
            longitude = QUERY_LONGITUDE
        )

        assertThat(results).contains(warehouse)
    }

    @Test
    fun `should return each warehouse only once in neighborhood results`() {
        val warehouse = createWarehouse(WAREHOUSE_ID)
        index.add(warehouse)

        val results = index.findInNeighborhood(
            latitude = warehouse.latitude,
            longitude = warehouse.longitude
        )

        assertThat(results.map(Warehouse::id).distinct())
            .hasSize(results.size)
    }

    @Test
    fun `should find warehouses beyond adjacent cells within search radius`() {
        val warehouse = createWarehouse(
            id = DISTANT_WAREHOUSE_ID,
            latitude = RADIUS_TEST_LATITUDE,
            longitude = QUERY_LONGITUDE
        )
        index.add(warehouse)

        val results = index.findCandidatesWithinRadius(
            latitude = QUERY_LATITUDE,
            longitude = QUERY_LONGITUDE,
            radiusKm = SEARCH_RADIUS_KM
        )

        assertThat(results).contains(warehouse)
    }

    @Test
    fun `should return empty results when no warehouse intersects search bounds`() {
        val distantWarehouse = createWarehouse(
            id = DISTANT_WAREHOUSE_ID,
            latitude = DISTANT_LATITUDE,
            longitude = DISTANT_LONGITUDE
        )
        index.add(distantWarehouse)

        val results = index.findCandidatesWithinRadius(
            latitude = QUERY_LATITUDE,
            longitude = QUERY_LONGITUDE,
            radiusKm = SMALL_SEARCH_RADIUS_KM
        )

        assertThat(results).isEmpty()
    }

    @Test
    fun `should return warehouses from both sides of antimeridian`() {
        val easternWarehouse = createWarehouse(
            id = EASTERN_WAREHOUSE_ID,
            latitude = ANTIMERIDIAN_LATITUDE,
            longitude = EAST_ANTIMERIDIAN_LONGITUDE
        )
        val westernWarehouse = createWarehouse(
            id = WESTERN_WAREHOUSE_ID,
            latitude = ANTIMERIDIAN_LATITUDE,
            longitude = WEST_ANTIMERIDIAN_LONGITUDE
        )
        val antimeridianIndex = WarehouseSpatialIndex(
            precision = GeohashPrecision.STREET,
            neighborCalculator = GeohashNeighborCalculator(),
            boundsCalculator = GeohashSearchBoundsCalculator()
        )
        antimeridianIndex.add(easternWarehouse)
        antimeridianIndex.add(westernWarehouse)

        val results = antimeridianIndex.findCandidatesWithinRadius(
            latitude = ANTIMERIDIAN_LATITUDE,
            longitude = QUERY_ANTIMERIDIAN_LONGITUDE,
            radiusKm = ANTIMERIDIAN_SEARCH_RADIUS_KM
        )

        assertThat(results).containsExactly(
            easternWarehouse,
            westernWarehouse
        )
    }

    @Test
    fun `should reject zero search radius`() {
        val result = runCatching {
            index.findCandidatesWithinRadius(
                latitude = QUERY_LATITUDE,
                longitude = QUERY_LONGITUDE,
                radiusKm = ZERO_RADIUS_KM
            )
        }

        assertThat(result.exceptionOrNull())
            .isInstanceOf(InvalidSearchRadiusException::class.java)
    }

    @Test
    fun `should reject negative search radius`() {
        val result = runCatching {
            index.findCandidatesWithinRadius(
                latitude = QUERY_LATITUDE,
                longitude = QUERY_LONGITUDE,
                radiusKm = NEGATIVE_RADIUS_KM
            )
        }

        assertThat(result.exceptionOrNull())
            .isInstanceOf(InvalidSearchRadiusException::class.java)
    }

    @Test
    fun `should reject NaN search radius`() {
        val result = runCatching {
            index.findCandidatesWithinRadius(
                latitude = QUERY_LATITUDE,
                longitude = QUERY_LONGITUDE,
                radiusKm = Double.NaN
            )
        }

        assertThat(result.exceptionOrNull())
            .isInstanceOf(InvalidSearchRadiusException::class.java)
    }

    @Test
    fun `should reject positive infinity search radius`() {
        val result = runCatching {
            index.findCandidatesWithinRadius(
                latitude = QUERY_LATITUDE,
                longitude = QUERY_LONGITUDE,
                radiusKm = Double.POSITIVE_INFINITY
            )
        }

        assertThat(result.exceptionOrNull())
            .isInstanceOf(InvalidSearchRadiusException::class.java)
    }

    private fun createWarehouse(
        id: String,
        latitude: Double = QUERY_LATITUDE,
        longitude: Double = QUERY_LONGITUDE
    ): Warehouse = Warehouse(
        id = id,
        name = "$WAREHOUSE_NAME_PREFIX $id",
        regionalZone = RegionalZone.NORTH,
        latitude = latitude,
        longitude = longitude
    )

    private companion object {
        const val WAREHOUSE_ID = "WH-001"
        const val UNKNOWN_WAREHOUSE_ID = "WH-999"
        const val NEIGHBOR_WAREHOUSE_ID = "WH-002"
        const val DISTANT_WAREHOUSE_ID = "WH-003"
        const val EASTERN_WAREHOUSE_ID = "WH-004"
        const val WESTERN_WAREHOUSE_ID = "WH-005"

        const val WAREHOUSE_NAME_PREFIX = "Warehouse"
        const val ORIGINAL_GEOHASH = "sv8e1"
        const val UNMATCHED_PREFIX = "u"

        const val QUERY_LATITUDE = 31.5
        const val QUERY_LONGITUDE = 34.5

        const val UPDATED_LATITUDE = 40.0
        const val UPDATED_LONGITUDE = 40.0

        const val RADIUS_TEST_LATITUDE = 31.65
        const val SEARCH_RADIUS_KM = 20.0

        const val DISTANT_LATITUDE = 40.0
        const val DISTANT_LONGITUDE = 40.0
        const val SMALL_SEARCH_RADIUS_KM = 5.0

        const val ANTIMERIDIAN_LATITUDE = 0.1
        const val EAST_ANTIMERIDIAN_LONGITUDE = 179.9
        const val WEST_ANTIMERIDIAN_LONGITUDE = -179.9
        const val QUERY_ANTIMERIDIAN_LONGITUDE = 179.95
        const val ANTIMERIDIAN_SEARCH_RADIUS_KM = 30.0

        const val ZERO_RADIUS_KM = 0.0
        const val NEGATIVE_RADIUS_KM = -1.0
    }
}

package org.example.domain.algorithm.geohash

import org.example.domain.model.Warehouse
import org.example.domain.model.exception.InvalidSearchRadiusException
import kotlin.math.PI

class WarehouseSpatialIndex(
    private val encoder: GeohashEncoder = GeohashEncoder(),
    private val precision: GeohashPrecision = GeohashPrecision.CITY,
    private val neighborCalculator: GeohashNeighborCalculator,
    private val boundsCalculator: GeohashSearchBoundsCalculator,
    private val coverageCalculator: GeohashPrefixCoverageCalculator =
        GeohashPrefixCoverageCalculator(precision = precision)
) {

    private val warehousesByPrefix =
        mutableMapOf<String, MutableMap<String, Warehouse>>()

    private val prefixByWarehouseId = mutableMapOf<String, String>()

    fun add(warehouse: Warehouse) {
        val geohash = encodeWarehouseLocation(warehouse)

        remove(warehouse.id)

        geohashPrefixes(geohash).forEach { prefix ->
            warehousesByPrefix
                .getOrPut(prefix) { mutableMapOf() }[warehouse.id] = warehouse
        }

        prefixByWarehouseId[warehouse.id] = geohash
    }

    fun remove(warehouseId: String): Boolean {
        val geohash = prefixByWarehouseId.remove(warehouseId)
            ?: return false

        geohashPrefixes(geohash).forEach { prefix ->
            val warehouses = warehousesByPrefix[prefix]
                ?: return@forEach

            warehouses.remove(warehouseId)

            if (warehouses.isEmpty()) {
                warehousesByPrefix.remove(prefix)
            }
        }

        return true
    }

    fun update(warehouse: Warehouse) {
        add(warehouse)
    }

    fun clear() {
        warehousesByPrefix.clear()
        prefixByWarehouseId.clear()
    }

    fun findByPrefix(prefix: String): List<Warehouse> =
        warehousesByPrefix[prefix]?.values?.toList().orEmpty()

    fun findInNeighborhood(
        latitude: Double,
        longitude: Double
    ): List<Warehouse> {
        val geohash = encoder.encode(
            latitude,
            longitude,
            precision.value
        )

        val neighboringGeohashes = neighborCalculator
            .calculateNeighbors(geohash)
            .values

        return (neighboringGeohashes + geohash)
            .flatMap(::findByPrefix)
            .distinctBy(Warehouse::id)
    }

    fun findCandidatesWithinRadius(
        latitude: Double,
        longitude: Double,
        radiusKm: Double
    ): List<Warehouse> {
        requireValidRadius(radiusKm)

        encoder.encode(latitude, longitude, precision.value)

        if (radiusKm >= HALF_EARTH_CIRCUMFERENCE_KM) {
            return allIndexedWarehouses()
        }

        val bounds = boundsCalculator.calculate(
            latitude = latitude,
            longitude = longitude,
            radiusKm = radiusKm
        )

        val prefixes = coverageCalculator.calculate(bounds)

        return prefixes
            .flatMap(::findByPrefix)
            .distinctBy(Warehouse::id)
    }

    private fun allIndexedWarehouses(): List<Warehouse> =
        prefixByWarehouseId.keys
            .mapNotNull { warehouseId ->
                findWarehouseById(warehouseId)
            }

    private fun findWarehouseById(warehouseId: String): Warehouse? =
        prefixByWarehouseId[warehouseId]?.let { geohash ->
            warehousesByPrefix[geohash]?.get(warehouseId)
        }

    private fun requireValidRadius(radiusKm: Double) {
        if (!radiusKm.isFinite() || radiusKm <= 0.0) {
            throw InvalidSearchRadiusException()
        }
    }

    private fun encodeWarehouseLocation(warehouse: Warehouse): String =
        encoder.encode(
            latitude = warehouse.latitude,
            longitude = warehouse.longitude,
            precision = precision.value
        )

    private fun geohashPrefixes(geohash: String): List<String> =
        (MIN_PREFIX_LENGTH..geohash.length).map(geohash::take)

    private companion object {
        const val MIN_PREFIX_LENGTH = 1
        const val HALF_EARTH_CIRCUMFERENCE_KM =
            PI * EARTH_RADIUS_KILOMETERS
    }
}

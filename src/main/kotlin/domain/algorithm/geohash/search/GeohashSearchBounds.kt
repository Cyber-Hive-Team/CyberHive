package org.example.domain.algorithm.geohash.search

import org.example.domain.algorithm.geohash.core.CoordinateBounds
import org.example.domain.algorithm.geohash.core.GeohashCell

data class GeohashSearchBounds(
    val latitudeBounds: CoordinateBounds,
    val longitudeBounds: List<CoordinateBounds>
) {

    fun intersects(cell: GeohashCell): Boolean =
        latitudeIntersects(cell) && longitudeIntersects(cell)

    fun contains(cell: GeohashCell): Boolean =
        latitudeContains(cell) && longitudeContains(cell)

    private fun latitudeIntersects(cell: GeohashCell): Boolean =
        cell.latitudeBounds.max >= latitudeBounds.min &&
                cell.latitudeBounds.min <= latitudeBounds.max

    private fun longitudeIntersects(cell: GeohashCell): Boolean =
        longitudeBounds.any { bounds ->
            cell.longitudeBounds.max >= bounds.min &&
                    cell.longitudeBounds.min <= bounds.max
        }

    private fun latitudeContains(cell: GeohashCell): Boolean =
        cell.latitudeBounds.min >= latitudeBounds.min &&
                cell.latitudeBounds.max <= latitudeBounds.max

    private fun longitudeContains(cell: GeohashCell): Boolean =
        longitudeBounds.any { bounds ->
            cell.longitudeBounds.min >= bounds.min &&
                    cell.longitudeBounds.max <= bounds.max
        }
}

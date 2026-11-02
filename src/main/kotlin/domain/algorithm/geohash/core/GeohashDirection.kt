package org.example.domain.algorithm.geohash.core

private const val NORTH_OFFSET = 1
private const val SOUTH_OFFSET = -1
private const val EAST_OFFSET = 1
private const val WEST_OFFSET = -1
private const val NO_OFFSET = 0

enum class GeohashDirection(
    val latitudeOffset: Int,
    val longitudeOffset: Int
) {
    NORTH(NORTH_OFFSET, NO_OFFSET),
    SOUTH(SOUTH_OFFSET, NO_OFFSET),
    EAST(NO_OFFSET, EAST_OFFSET),
    WEST(NO_OFFSET, WEST_OFFSET),
    NORTH_EAST(NORTH_OFFSET, EAST_OFFSET),
    NORTH_WEST(NORTH_OFFSET, WEST_OFFSET),
    SOUTH_EAST(SOUTH_OFFSET, EAST_OFFSET),
    SOUTH_WEST(SOUTH_OFFSET, WEST_OFFSET);
}


package org.example.domain.algorithm.knn

import org.example.domain.model.Package
import org.example.domain.model.RegionalZone
import org.example.domain.model.PackageSectorInput
import kotlin.math.pow
import kotlin.math.sqrt

private const val K_NEIGHBORS = 5

class KnnSectorClassifier {

    fun classify(
        input: PackageSectorInput,
        historicalPackages: List<Package>
    ): RegionalZone {
        val records = historicalPackages.map(::toTrainingRecord)
        require(records.isNotEmpty()) {
            "Historical training data must not be empty"
        }

        val query = Features(
            input.latitude,
            input.longitude,
            input.weight
        )
        val ranges = buildRanges(records.map { it.features } + query)
        val normalizedQuery = normalize(query, ranges)
        val neighbors = findNearestNeighbors(
            normalizedQuery,
            records,
            ranges
        )

        return predictZone(neighbors)
    }

    private fun toTrainingRecord(
        cargoPackage: Package
    ): TrainingRecord {
        val destination = cargoPackage.destinationWarehouse
        return TrainingRecord(
            features = Features(
                destination.latitude,
                destination.longitude,
                cargoPackage.weight
            ),
            zone = destination.regionalZone
        )
    }

    private fun findNearestNeighbors(
        query: Features,
        records: List<TrainingRecord>,
        ranges: FeatureRanges
    ): List<Neighbor> {
        return records
            .map { record ->
                val normalized = normalize(record.features, ranges)
                Neighbor(
                    distance = calculateDistance(query, normalized),
                    zone = record.zone
                )
            }
            .sortedBy { it.distance }
            .take(minOf(K_NEIGHBORS, records.size))
    }

    private fun calculateDistance(
        first: Features,
        second: Features
    ): Double {
        val latitudeDifference = first.latitude - second.latitude
        val longitudeDifference = first.longitude - second.longitude
        val weightDifference = first.weight - second.weight

        return sqrt(
            latitudeDifference.pow(2) +
                    longitudeDifference.pow(2) +
                    weightDifference.pow(2)
        )
    }

    private fun predictZone(
        neighbors: List<Neighbor>
    ): RegionalZone {
        return neighbors
            .groupingBy { it.zone }
            .eachCount()
            .entries
            .maxWithOrNull(
                compareBy<Map.Entry<RegionalZone, Int>> { it.value }
                    .thenBy { -it.key.ordinal }
            )
            ?.key
            ?: throw IllegalStateException("No neighbors available")
    }

    private fun buildRanges(
        features: List<Features>
    ): FeatureRanges {
        return FeatureRanges(
            latitude = buildRange(features.map { it.latitude }),
            longitude = buildRange(features.map { it.longitude }),
            weight = buildRange(features.map { it.weight })
        )
    }

    private fun buildRange(values: List<Double>): Range {
        return Range(
            minimum = values.minOrNull() ?: 0.0,
            maximum = values.maxOrNull() ?: 0.0
        )
    }

    private fun normalize(
        features: Features,
        ranges: FeatureRanges
    ): Features {
        return Features(
            latitude = normalizeValue(features.latitude, ranges.latitude),
            longitude = normalizeValue(features.longitude, ranges.longitude),
            weight = normalizeValue(features.weight, ranges.weight)
        )
    }

    private fun normalizeValue(
        value: Double,
        range: Range
    ): Double {
        val difference = range.maximum - range.minimum
        return if (difference == 0.0) {
            0.0
        } else {
            (value - range.minimum) / difference
        }
    }

    private data class Features(
        val latitude: Double,
        val longitude: Double,
        val weight: Double
    )

    private data class TrainingRecord(
        val features: Features,
        val zone: RegionalZone
    )

    private data class Neighbor(
        val distance: Double,
        val zone: RegionalZone
    )

    private data class Range(
        val minimum: Double,
        val maximum: Double
    )

    private data class FeatureRanges(
        val latitude: Range,
        val longitude: Range,
        val weight: Range
    )
}

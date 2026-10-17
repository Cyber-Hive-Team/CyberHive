package org.example.presentation.cli

import kotlinx.coroutines.flow.Flow

data class PackageFeatures(val latitude: Double, val longitude: Double, val weightKg: Double)

/** Member 3 supplies the suspend adapter to ClassifyPackageSectorUseCase. */
fun interface PackageSectorClassifier {
    suspend fun classify(features: PackageFeatures): String
}

/** Member 2 maps TrackActiveFleetUseCase events to printable CLI messages. */
fun interface FleetTelemetry {
    fun updates(): Flow<String>
}

fun interface RouteCalculator {
    suspend fun calculate(startId: String, destinationId: String): String
}

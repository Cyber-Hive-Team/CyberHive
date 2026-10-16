package org.example.presentation.cli

interface CliCommand {
    data class CalculateRoute(val startId: String, val destinationId: String) : CliCommand
    data class ClassifyPackage(val latitude: Double, val longitude: Double, val weight: Double) : CliCommand
    data object StreamTelemetry : CliCommand
    data object StopTelemetry : CliCommand
    data object Help : CliCommand
    data object Exit : CliCommand
    data class Unknown(val input: String) : CliCommand
    }


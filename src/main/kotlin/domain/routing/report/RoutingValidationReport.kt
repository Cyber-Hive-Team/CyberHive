package org.example.domain.routing.report

data class RoutingValidationReport(
    val allPassed: Boolean,
    val messages: List<String>,
    val stablePackageCount: Int,
    val reroutedPackageCount: Int
)
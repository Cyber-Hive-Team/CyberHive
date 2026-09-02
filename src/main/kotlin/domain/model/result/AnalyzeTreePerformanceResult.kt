package org.example.domain.model.result

data class AnalyzeTreePerformanceResult(
    val targetTrackingId: String,
    val packageCount: Int,
    val unbalancedSearchSteps: Int,
    val avlSearchSteps: Int
)

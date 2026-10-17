package org.example.presentation

import org.example.domain.model.input.AnalyzeTreePerformanceInput
import org.example.domain.usecase.AnalyzeTreePerformanceUseCase

class TreePerformanceDemoRunner(
    private val analyzeTreePerformanceUseCase: AnalyzeTreePerformanceUseCase
) {

    fun run() {
        val result = analyzeTreePerformanceUseCase(
            AnalyzeTreePerformanceInput(
                firstPackageNumber = 1,
                packageCount = 1_000,
                trackingIdWidth = 6
            )
        )

        println("\n=== Balanced Index Simulator ===")
        println("Target Tracking ID: ${result.targetTrackingId}")
        println("Package Count: ${result.packageCount}")
        println(
            "Unbalanced BST Search Steps: " +
                    result.unbalancedSearchSteps
        )
        println(
            "AVL Tree Search Steps: " +
                    result.avlSearchSteps
        )
        println("Unbalanced BST Complexity: O(N)")
        println("AVL Tree Complexity: O(log N)")
    }
}

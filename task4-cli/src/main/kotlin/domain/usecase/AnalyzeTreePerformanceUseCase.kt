package org.example.domain.usecase

import org.example.domain.algorithm.performance.AvlPackageTrackingTree
import org.example.domain.algorithm.performance.UnbalancedPackageTrackingBST
import org.example.domain.model.result.AnalyzeTreePerformanceResult
import org.example.domain.model.input.AnalyzeTreePerformanceInput
import org.example.domain.model.exception.InvalidPackageCountException

class AnalyzeTreePerformanceUseCase {

    operator fun invoke(
        input: AnalyzeTreePerformanceInput
    ): AnalyzeTreePerformanceResult {
        input.packageCount.takeIf { it > 0 } ?: throw InvalidPackageCountException()

        val trackingIds = generateSequentialTrackingIds(input)

        val unbalancedTree = UnbalancedPackageTrackingBST()
        val avlTree = AvlPackageTrackingTree()

        trackingIds.forEach(unbalancedTree::insert)
        trackingIds.forEach(avlTree::insert)

        val targetTrackingId = trackingIds.last()

        val unbalancedSearchSteps =
            unbalancedTree.countSearchSteps(targetTrackingId)

        val avlSearchSteps =
            avlTree.countSearchSteps(targetTrackingId)

        return AnalyzeTreePerformanceResult(
            targetTrackingId = targetTrackingId,
            packageCount = input.packageCount,
            unbalancedSearchSteps = unbalancedSearchSteps,
            avlSearchSteps = avlSearchSteps
        )

    }

    private fun generateSequentialTrackingIds(
        input: AnalyzeTreePerformanceInput
    ): List<String> {
        val lastPackageNumber =
            input.firstPackageNumber + input.packageCount - 1

        return (input.firstPackageNumber..lastPackageNumber)
            .map { packageNumber ->
                "PKG-${
                    packageNumber
                        .toString()
                        .padStart(input.trackingIdWidth, '0')
                }"
            }
    }

}

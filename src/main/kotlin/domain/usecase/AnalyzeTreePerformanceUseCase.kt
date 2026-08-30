package org.example.domain.usecase

import org.example.domain.algorithm.performance.AvlPackageTrackingTree
import org.example.domain.algorithm.performance.UnbalancedPackageTrackingBST

private const val FIRST_PACKAGE_NUMBER = 1
private const val PACKAGE_COUNT = 1_000
private const val TRACKING_ID_WIDTH = 6

class AnalyzeTreePerformanceUseCase {

    operator fun invoke() {
        val trackingIds = generateSequentialTrackingIds()

        val unbalancedTree = UnbalancedPackageTrackingBST()
        val avlTree = AvlPackageTrackingTree()

        trackingIds.forEach(unbalancedTree::insert)
        trackingIds.forEach(avlTree::insert)

        val targetTrackingId = trackingIds.last()

        val unbalancedSearchSteps =
            unbalancedTree.countSearchSteps(targetTrackingId)

        val avlSearchSteps =
            avlTree.countSearchSteps(targetTrackingId)

        printPerformanceResults(
            targetTrackingId,
            unbalancedSearchSteps,
            avlSearchSteps
        )
    }

    private fun generateSequentialTrackingIds(): List<String> {
        return (FIRST_PACKAGE_NUMBER..PACKAGE_COUNT)
            .map { packageNumber ->
                "PKG-${
                    packageNumber
                        .toString()
                        .padStart(TRACKING_ID_WIDTH, '0')
                }"
            }
    }

    private fun printPerformanceResults(
        targetTrackingId: String,
        unbalancedSearchSteps: Int,
        avlSearchSteps: Int
    ) {
        println("=== Tree Performance Analysis ===")
        println("Target Tracking ID: $targetTrackingId")
        println("Package Count: $PACKAGE_COUNT")
        println(
            "Unbalanced BST Search Steps: " +
                    unbalancedSearchSteps
        )
        println(
            "AVL Tree Search Steps: " +
                    avlSearchSteps
        )
        println("Unbalanced BST Complexity: O(N)")
        println("AVL Tree Complexity: O(log N)")
    }
}
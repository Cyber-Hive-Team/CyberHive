package org.example.domain.algorithm.performance

class UnbalancedPackageTrackingBST {

    private var root: PackageTrackingTreeNode? = null

    fun insert(trackingId: String) {
        root = insertTrackingId(root, trackingId)
    }

    fun countSearchSteps(trackingId: String): Int {
        return countStepsFromNode(root, trackingId)
    }

    private fun insertTrackingId(
        node: PackageTrackingTreeNode?,
        trackingId: String
    ): PackageTrackingTreeNode {
        if (node == null) {
            return PackageTrackingTreeNode(trackingId)
        }

        when {
            trackingId < node.trackingId -> {
                node.left = insertTrackingId(
                    node.left,
                    trackingId
                )
            }

            trackingId > node.trackingId -> {
                node.right = insertTrackingId(
                    node.right,
                    trackingId
                )
            }
        }

        return node
    }

    private fun countStepsFromNode(
        node: PackageTrackingTreeNode?,
        trackingId: String
    ): Int {
        if (node == null) {
            return 0
        }

        return when {
            trackingId == node.trackingId -> 1

            trackingId < node.trackingId ->
                1 + countStepsFromNode(
                    node.left,
                    trackingId
                )

            else ->
                1 + countStepsFromNode(
                    node.right,
                    trackingId
                )
        }
    }
}
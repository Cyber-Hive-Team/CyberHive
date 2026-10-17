package org.example.domain.algorithm.performance

private const val MAX_ALLOWED_IMBALANCE = 1
private const val MIN_ALLOWED_IMBALANCE = -1
private const val HEIGHT_INCREMENT = 1
private const val EMPTY_TREE_HEIGHT = 0

class AvlPackageTrackingTree {

    private var root: PackageTrackingTreeNode? = null

    fun insert(trackingId: String) {
        root = insertAndBalance(root, trackingId)
    }

    fun countSearchSteps(trackingId: String): Int {
        return countStepsFromNode(root, trackingId)
    }

    private fun insertAndBalance(
        node: PackageTrackingTreeNode?,
        trackingId: String
    ): PackageTrackingTreeNode {
        if (node == null) {
            return PackageTrackingTreeNode(trackingId)
        }

        when {
            trackingId < node.trackingId -> {
                node.left = insertAndBalance(
                    node.left,
                    trackingId
                )
            }

            trackingId > node.trackingId -> {
                node.right = insertAndBalance(
                    node.right,
                    trackingId
                )
            }

            else -> return node
        }

        updateHeight(node)

        return rebalanceNode(node, trackingId)
    }

    private fun rebalanceNode(
        node: PackageTrackingTreeNode,
        trackingId: String
    ): PackageTrackingTreeNode {
        val balanceFactor = calculateBalanceFactor(node)

        return when {
            balanceFactor > MAX_ALLOWED_IMBALANCE ->
                rebalanceLeftHeavyNode(node, trackingId)

            balanceFactor < MIN_ALLOWED_IMBALANCE ->
                rebalanceRightHeavyNode(node, trackingId)

            else -> node
        }
    }

    private fun rebalanceLeftHeavyNode(
        node: PackageTrackingTreeNode,
        trackingId: String
    ): PackageTrackingTreeNode {
        val leftChild = node.left ?: return node

        return if (trackingId < leftChild.trackingId) {
            rotateRight(node)
        } else {
            node.left = rotateLeft(leftChild)
            rotateRight(node)
        }
    }

    private fun rebalanceRightHeavyNode(
        node: PackageTrackingTreeNode,
        trackingId: String
    ): PackageTrackingTreeNode {
        val rightChild = node.right ?: return node

        return if (trackingId > rightChild.trackingId) {
            rotateLeft(node)
        } else {
            node.right = rotateRight(rightChild)
            rotateLeft(node)
        }
    }

    private fun rotateRight(
        node: PackageTrackingTreeNode
    ): PackageTrackingTreeNode {
        val newRoot = node.left ?: return node
        val transferredSubtree = newRoot.right

        newRoot.right = node
        node.left = transferredSubtree

        updateHeight(node)
        updateHeight(newRoot)

        return newRoot
    }

    private fun rotateLeft(
        node: PackageTrackingTreeNode
    ): PackageTrackingTreeNode {
        val newRoot = node.right ?: return node
        val transferredSubtree = newRoot.left

        newRoot.left = node
        node.right = transferredSubtree

        updateHeight(node)
        updateHeight(newRoot)

        return newRoot
    }

    private fun updateHeight(
        node: PackageTrackingTreeNode
    ) {
        node.height = calculateNodeHeight(node)
    }

    private fun calculateNodeHeight(
        node: PackageTrackingTreeNode
    ): Int {
        return HEIGHT_INCREMENT + maxOf(
            getHeight(node.left),
            getHeight(node.right)
        )
    }

    private fun getHeight(
        node: PackageTrackingTreeNode?
    ): Int {
        return node?.height ?: EMPTY_TREE_HEIGHT
    }

    private fun calculateBalanceFactor(
        node: PackageTrackingTreeNode
    ): Int {
        return getHeight(node.left) - getHeight(node.right)
    }

    private fun countStepsFromNode(
        node: PackageTrackingTreeNode?,
        trackingId: String
    ): Int {
        if (node == null) {
            return EMPTY_TREE_HEIGHT
        }

        return when {
            trackingId == node.trackingId -> HEIGHT_INCREMENT

            trackingId < node.trackingId ->
                HEIGHT_INCREMENT + countStepsFromNode(
                    node.left,
                    trackingId
                )

            else ->
                HEIGHT_INCREMENT + countStepsFromNode(
                    node.right,
                    trackingId
                )
        }
    }
}
package org.example.domain.algorithm.performance

private const val INITIAL_HEIGHT = 1

class PackageTrackingTreeNode(
    val trackingId: String,
    var left: PackageTrackingTreeNode? = null,
    var right: PackageTrackingTreeNode? = null,
    var height: Int = INITIAL_HEIGHT
)
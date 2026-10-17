package org.example.domain.usecase

import org.example.domain.model.WarehouseNode

class TraceHubLineageUseCase(
    private val tree: WarehouseNode
) {

    operator fun invoke(
        warehouseId: String
    ): List<WarehouseNode> {

        val startNode = findNode(
            tree,
            warehouseId
        ) ?: return emptyList()

        return traceToGlobal(startNode)
    }

    private fun findNode(
        node: WarehouseNode,
        warehouseId: String
    ): WarehouseNode? {

        if (node.warehouse.id == warehouseId) {
            return node
        }

        return node.children
            .asSequence()
            .mapNotNull { child ->
                findNode(child, warehouseId)
            }
            .firstOrNull()
    }

    private fun traceToGlobal(
        node: WarehouseNode?
    ): List<WarehouseNode> {

        return if (node == null) {
            emptyList()
        } else {
            listOf(node) + traceToGlobal(node.parent)
        }
    }
}


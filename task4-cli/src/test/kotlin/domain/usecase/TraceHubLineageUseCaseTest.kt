package org.example.domain.usecase

import org.example.domain.model.RegionalZone
import org.example.domain.model.Warehouse
import org.example.domain.model.WarehouseLevel
import org.example.domain.model.WarehouseNode
import org.junit.jupiter.api.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class TraceHubLineageUseCaseTest {

    @Test
    fun `local node returns lineage up to root`() {
        // Given
        val root = tree()
        val useCase = TraceHubLineageUseCase(root)

        // When
        val result = useCase("WH-003")

        // Then
        assertEquals(
            listOf("WH-003", "WH-002", "WH-001"),
            result.map { it.warehouse.id })
    }

    @Test
    fun `regional node returns itself and root`() {
        // Given
        val root = tree()
        val useCase = TraceHubLineageUseCase(root)

        // When
        val result = useCase("WH-002")

        // Then
        assertEquals(
            listOf("WH-002", "WH-001"),
            result.map { it.warehouse.id })
    }

    @Test
    fun `root returns only itself`() {
        // Given
        val root = tree()
        val useCase = TraceHubLineageUseCase(root)

        // When
        val result = useCase("WH-001")

        // Then
        assertEquals(
            listOf("WH-001"),
            result.map { it.warehouse.id })
    }

    @Test
    fun `missing node returns empty list`() {
        // Given
        val root = tree()
        val useCase = TraceHubLineageUseCase(root)

        // When
        val result = useCase("WH-999")

        // Then
        assertTrue(result.isEmpty())
    }

    @Test
    fun `node in second branch is found and traced to root`() {
        // Given
        val root = tree()
        val secondBranch = WarehouseNode(
            warehouse = warehouse("WH-004"),
            level = WarehouseLevel.REGIONAL,
            parent = root
        )
        root.children.add(secondBranch)
        val useCase = TraceHubLineageUseCase(root)

        // When
        val result = useCase("WH-004")

        // Then
        assertEquals(
            listOf("WH-004", "WH-001"),
            result.map { it.warehouse.id })
    }

    private fun tree(): WarehouseNode {
        val root = WarehouseNode(warehouse("WH-001"), WarehouseLevel.GLOBAL, parent = null)
        val regional = WarehouseNode(warehouse("WH-002"), WarehouseLevel.REGIONAL, parent = root)
        val local = WarehouseNode(warehouse("WH-003"), WarehouseLevel.LOCAL, parent = regional)

        root.children.add(regional)
        regional.children.add(local)

        return root
    }

    private fun warehouse(id: String) =
        Warehouse(
            id,
            "Warehouse $id",
            RegionalZone.NORTH,
            31.5,
            34.4
        )
}

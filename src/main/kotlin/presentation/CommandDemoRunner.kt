package org.example.presentation

import org.example.domain.command.AssignPackageToQueueCommand
import org.example.domain.command.CommandInvoker
import org.example.domain.model.Package
import org.example.domain.model.Priority
import org.example.domain.model.Warehouse
import org.example.domain.model.WarehouseServices
import org.example.domain.model.result.Result
import org.example.domain.repository.WarehouseRepository
import org.example.domain.usecase.AssignPackageToCargoQueueUseCase

class InMemoryWarehouseRepository(
    warehouses: List<Warehouse>
) : WarehouseRepository {
    private val byId = warehouses.associateBy { it.id }

    override fun getAllWarehouses(): Result<List<Warehouse>> =
        Result(data = byId.values.toList(), errorMessage = null)

    override fun getWarehouseById(warehouseId: String): Warehouse? =
        byId[warehouseId]

    override fun addPackageToCargoQueue(warehouseId: String, cargoPackage: Package): Boolean {
        val warehouse = byId[warehouseId] ?: return false
        warehouse.addPackages(listOf(cargoPackage))
        return true
    }

    override fun sortCargoQueue(warehouseId: String): Boolean {
        val warehouse = byId[warehouseId] ?: return false
        warehouse.sortCargoQueue()
        return true
    }

    override fun isPackageInCargoQueue(warehouseId: String, packageId: String): Boolean {
        val warehouse = byId[warehouseId] ?: return false
        return warehouse.getCargoQueue().any { it.id == packageId }
    }

    override fun getAllWarehouseServices(): List<WarehouseServices> = emptyList()
}
class CommandInvokerDemoRunner(
    private val warehouses: List<Warehouse>
) {
    companion object {
        private const val DEMO_PACKAGE_1_WEIGHT_KG = 5.0
        private const val DEMO_PACKAGE_2_WEIGHT_KG = 8.0
        private const val DEMO_PACKAGE_3_WEIGHT_KG = 2.5
        private const val UNDO_STEPS_TO_DEMO = 2
        private const val REDO_STEPS_TO_DEMO= 1
    }
    fun run() {
        println("\n=== Time-Machine Dispatch Panel Demo ===")

        if (warehouses.isEmpty()) {
            println("No warehouses loaded, skipping demo.")
            return
        }

        val warehouseRepository = InMemoryWarehouseRepository(warehouses)
        val assignUseCase = AssignPackageToCargoQueueUseCase(warehouseRepository)
        val invoker = CommandInvoker()
        val targetWarehouse = warehouses.first()

        printQueue(targetWarehouse, "BEFORE")
        executeDemoCommands(invoker, targetWarehouse, warehouseRepository, assignUseCase)
        demoUndoRedo(invoker, targetWarehouse)
        printHistorySizes(invoker)
    }

    private fun executeDemoCommands(
        invoker: CommandInvoker,
        targetWarehouse: Warehouse,
        warehouseRepository: WarehouseRepository,
        assignUseCase: AssignPackageToCargoQueueUseCase
    ) {
        val demoPackages = buildDemoPackages(targetWarehouse)
        demoPackages.forEach { pkg ->
            invoker.executeCommand(
                AssignPackageToQueueCommand(targetWarehouse.id, pkg, assignUseCase, warehouseRepository)
            )
        }
        printQueue(targetWarehouse, "AFTER ${demoPackages.size} EXECUTES")
    }

    private fun buildDemoPackages(targetWarehouse: Warehouse): List<Package> = listOf(
        Package("DEMO-1", DEMO_PACKAGE_1_WEIGHT_KG, Priority.STANDARD, targetWarehouse, targetWarehouse),
        Package("DEMO-2", DEMO_PACKAGE_2_WEIGHT_KG, Priority.URGENT, targetWarehouse, targetWarehouse),
        Package("DEMO-3", DEMO_PACKAGE_3_WEIGHT_KG, Priority.LOW, targetWarehouse, targetWarehouse)
    )

    private fun demoUndoRedo(invoker: CommandInvoker, targetWarehouse: Warehouse) {
        println("\n-- Undo $UNDO_STEPS_TO_DEMO steps --")
        invoker.undo(UNDO_STEPS_TO_DEMO)
        printQueue(targetWarehouse, "AFTER UNDO x$UNDO_STEPS_TO_DEMO")

        println("\n-- Redo $REDO_STEPS_TO_DEMO step --")
        invoker.redo(REDO_STEPS_TO_DEMO)
        printQueue(targetWarehouse, "AFTER REDO x$REDO_STEPS_TO_DEMO")
    }

    private fun printHistorySizes(invoker: CommandInvoker) {
        println("\nUndo history size: ${invoker.undoHistorySize}, Redo history size: ${invoker.redoHistorySize}")
    }

    private fun printQueue(warehouse: Warehouse, label: String) {
        val ids = warehouse.getCargoQueue().joinToString { it.id }
        println("[$label] Warehouse ${warehouse.id} cargo queue: [$ids]")
    }
}

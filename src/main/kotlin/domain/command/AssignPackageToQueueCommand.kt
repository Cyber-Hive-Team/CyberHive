package org.example.domain.command

import org.example.domain.model.Package
import org.example.domain.repository.WarehouseRepository
import org.example.domain.usecase.AssignPackageToCargoQueueUseCase

class AssignPackageToQueueCommand(
    private val warehouseId: String,
    private val cargoPackage: Package,
    private val assignPackageToCargoQueueUseCase: AssignPackageToCargoQueueUseCase,
    private val warehouseRepository: WarehouseRepository
) : Command {

    private var addedPackage = false

    override fun execute(): Boolean {
        addedPackage = assignPackageToCargoQueueUseCase(warehouseId, cargoPackage)
        return addedPackage
    }

    override fun undo(): Boolean {
        if (!addedPackage) return false

        val warehouse = warehouseRepository.getWarehouseById(warehouseId) ?: return false
        return warehouse.removePackageFromCargoQueue(cargoPackage.id)
    }
}

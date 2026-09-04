package org.example.presentation

import org.example.data.repository.CsvWarehouseRepository
import org.example.domain.command.AssignPackageToQueueCommand
import org.example.domain.command.CommandInvoker
import org.example.domain.model.Package
import org.example.domain.model.Priority
import org.example.domain.model.Warehouse
import org.example.domain.usecase.AssignPackageToCargoQueueUseCase
class CommandDemoRunner(
    private val warehouses: List<Warehouse> ){


}

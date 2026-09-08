package org.example.domain.model.exception

open class DomainException(message: String) : Exception(message) {

    companion object {
        const val WAREHOUSE_NOT_FOUND = "WAREHOUSE_NOT_FOUND"
        const val PACKAGE_NOT_FOUND = "PACKAGE_NOT_FOUND"
        const val ROUTE_NOT_FOUND = "ROUTE_NOT_FOUND"
        const val Failed_to_execute_or_undo_command ="Failed_to_execute_or_undo_command"
    }
}

class WarehouseNotFoundException(message: String = WAREHOUSE_NOT_FOUND) : DomainException(message)
class PackageNotFoundException(message: String = PACKAGE_NOT_FOUND) : DomainException(message)
class RouteNotFoundException(message: String = ROUTE_NOT_FOUND) : DomainException(message)
class CommandExecutionException(message: String = Failed_to_execute_or_undo_command) : DomainException(message)

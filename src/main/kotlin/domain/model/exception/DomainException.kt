package org.example.domain.model.exception

open class DomainException(message: String) : Exception(message) {

    companion object {
        const val WAREHOUSE_NOT_FOUND = "WAREHOUSE_NOT_FOUND"
    }
}

class WarehouseNotFoundException(message: String = WAREHOUSE_NOT_FOUND) : DomainException(message)

package org.example.domain.algorithm.search

import org.example.domain.model.Warehouse

interface Router {

    fun findPath(
        start: Warehouse,
        destination: Warehouse
    ): List<Warehouse>
}

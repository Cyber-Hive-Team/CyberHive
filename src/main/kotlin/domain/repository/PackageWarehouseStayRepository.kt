package org.example.domain.repository

import org.example.domain.model.PackageWarehouseStay

interface PackageWarehouseStayRepository {
    fun getAllWarehouseStays(): List<PackageWarehouseStay>

}
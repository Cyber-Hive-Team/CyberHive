package org.example.domain.repository

import org.example.domain.model.input.PackageDeliveryTime

interface PackageDeliveryTimeRepository {
    fun getAllDeliveryTimes(): List<PackageDeliveryTime>

}

package org.example.di

import org.example.domain.validator.PackageValidator
import org.example.domain.validator.RouteValidator
import org.example.domain.validator.VehicleValidator
import org.example.domain.validator.WarehouseValidator
import org.koin.dsl.module

val validatorModule = module {

    single {
        PackageValidator()
    }

    single {
        RouteValidator()
    }

    single {
        VehicleValidator()
    }

    single {
        WarehouseValidator()
    }
}

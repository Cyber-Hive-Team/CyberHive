package org.example.domain.validator

sealed interface FieldError {
    data object InvalidWeight : FieldError
    data object InvalidBaseRate : FieldError
    data object InvalidOriginWarehouse : FieldError
    data object InvalidDestinationWarehouse : FieldError
    data object SameWarehouse : FieldError
    data object InvalidDistance : FieldError
    data object InvalidDelay : FieldError
    data object InvalidCapacity : FieldError
    data object InvalidCostPerKm : FieldError
    data object InvalidCurrentHub : FieldError
    data object InvalidWarehouseName : FieldError
    data object InvalidLatitude : FieldError
    data object InvalidLongitude : FieldError
    data object NoUpdateFields : FieldError
}

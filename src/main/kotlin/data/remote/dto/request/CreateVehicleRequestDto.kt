package data.remote.dto.request

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class CreateVehicleRequestDto(
    @SerialName("vehicleId")
    val vehicleId: String,

    @SerialName("currentHubId")
    val currentHubId: String,

    @SerialName("maxCapacityKg")
    val maxCapacityKg: Double,

    @SerialName("costPerKm")
    val costPerKm: Double
)

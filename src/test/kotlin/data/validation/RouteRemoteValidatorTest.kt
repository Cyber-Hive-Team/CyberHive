package org.example.test.data.validation

import org.example.data.exception.NullRequiredFieldException
import org.example.data.remote.dto.response.RouteResponseDto
import org.example.data.validation.RouteRemoteValidator
import org.example.data.remote.dto.response.WarehouseResponseDto
import org.junit.jupiter.api.Test
import kotlin.test.assertFailsWith
import kotlin.test.assertTrue

class RouteRemoteValidatorTest {

    private val validator = RouteRemoteValidator()

    private fun createValidDto(
        distanceKm: Double? = 100.0,
        typicalDelayMin: Int? = 10
    ): RouteResponseDto {
        return RouteResponseDto(
            routeId = "RT-00001",
            originHubId = "WH-001",
            destinationHubId = "WH-002",
            distanceKm = distanceKm,
            typicalDelayMin = typicalDelayMin,
            originHub = WarehouseResponseDto(
                id = "WH-001",
                name = "Origin Warehouse",
                regionalZone = null,
                latitude = null,
                longitude = null
            ),
            destinationHub = WarehouseResponseDto(
                id = "WH-002",
                name = "Destination Warehouse",
                regionalZone = null,
                latitude = null,
                longitude = null
            )
        )
    }

    @Test
    fun `valid route response should pass validation`() {
        val dto = createValidDto()

        validator.validate(dto)
    }

    @Test
    fun `route with null distance should throw exception`() {
        val dto = createValidDto(
            distanceKm = null
        )

        val exception = assertFailsWith<NullRequiredFieldException> {
            validator.validate(dto)
        }

        assertTrue(
            exception.message!!.contains("distanceKm")
        )
    }

    @Test
    fun `route with null typical delay should throw exception`() {
        val dto = createValidDto(
            typicalDelayMin = null
        )

        val exception = assertFailsWith<NullRequiredFieldException> {
            validator.validate(dto)
        }

        assertTrue(
            exception.message!!.contains("typicalDelayMin")
        )
    }

    @Test
    fun `route with zero distance should pass validation`() {
        val dto = createValidDto(
            distanceKm = 0.0
        )

        validator.validate(dto)
    }

    @Test
    fun `route with zero delay should pass validation`() {
        val dto = createValidDto(
            typicalDelayMin = 0
        )

        validator.validate(dto)
    }
}

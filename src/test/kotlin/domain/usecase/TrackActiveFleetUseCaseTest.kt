package org.example.test.domain.usecase

import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.flow.asFlow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.toList
import kotlinx.coroutines.runBlocking
import org.example.domain.model.VehicleTelemetry
import org.example.domain.model.result.FleetTrackingEvent
import org.example.domain.repository.VehicleRepository
import org.example.domain.usecase.TrackActiveFleetUseCase
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test

class TrackActiveFleetUseCaseTest {

    private val vehicleRepository = mockk<VehicleRepository>()
    private val useCase = TrackActiveFleetUseCase(vehicleRepository)

    private val vehicleIds = listOf("TRK-0001")

    @Test
    fun `should emit telemetry updated for valid reading`() = runBlocking {
        val reading = telemetry(speed = 40.0)
        val events = collectEvents(listOf(reading))
        assertEquals(FleetTrackingEvent.TelemetryUpdated(reading), events.single())
    }

    @Test
    fun `should emit update for every valid reading`() = runBlocking {
        val events = collectEvents(
            listOf(
                telemetry(speed = 30.0, timestamp = 0L),
                telemetry(speed = 40.0, timestamp = 2_000L),
                telemetry(speed = 50.0, timestamp = 4_000L)
            )
        )
        assertEquals(3, events.filterIsInstance<FleetTrackingEvent.TelemetryUpdated>().size)
    }

    @Test
    fun `should emit no events for empty flow`() = runBlocking {
        val events = collectEvents(emptyList())
        assertTrue(events.isEmpty())
    }


    @Test
    fun `should filter negative speed`() = runBlocking {
        val events = collectEvents(listOf(telemetry(speed = -10.0)))
        assertTrue(events.isEmpty())
    }

    @Test
    fun `should accept zero speed`() = runBlocking {
        val events = collectEvents(listOf(telemetry(speed = 0.0)))
        assertEquals(1, events.size)
    }

    @Test
    fun `should filter fuel above one hundred`() = runBlocking {
        val events = collectEvents(listOf(telemetry(fuel = 120.0)))
        assertTrue(events.isEmpty())
    }

    @Test
    fun `should filter negative fuel`() = runBlocking {
        val events = collectEvents(listOf(telemetry(fuel = -5.0)))
        assertTrue(events.isEmpty())
    }

    @Test
    fun `should accept zero fuel`() = runBlocking {
        val events = collectEvents(listOf(telemetry(speed = 30.0, fuel = 0.0)))
        assertEquals(1, events.size)
    }

    @Test
    fun `should accept full fuel`() = runBlocking {
        val events = collectEvents(listOf(telemetry(fuel = 100.0)))
        assertEquals(1, events.size)
    }

    @Test
    fun `should filter invalid latitude`() = runBlocking {
        val events = collectEvents(listOf(telemetry(latitude = 95.0)))
        assertTrue(events.isEmpty())
    }

    @Test
    fun `should filter invalid longitude`() = runBlocking {
        val events = collectEvents(listOf(telemetry(longitude = 190.0)))
        assertTrue(events.isEmpty())
    }

    @Test
    fun `should filter NaN speed`() = runBlocking {
        val events = collectEvents(listOf(telemetry(speed = Double.NaN)))
        assertTrue(events.isEmpty())
    }

    @Test
    fun `should filter infinite speed`() = runBlocking {
        val events = collectEvents(
            listOf(telemetry(speed = Double.POSITIVE_INFINITY))
        )

        assertTrue(events.isEmpty())
    }

    @Test
    fun `should filter blank vehicle id`() = runBlocking {
        val events = collectEvents(listOf(telemetry(vehicleId = " ")))
        assertTrue(events.isEmpty())
    }

    @Test
    fun `should filter negative timestamp`() = runBlocking {
        val events = collectEvents(listOf(telemetry(timestamp = -1L)))
        assertTrue(events.isEmpty())
    }

    @Test
    fun `should continue after invalid reading`() = runBlocking {
        val validReading = telemetry(speed = 40.0, timestamp = 2_000L)

        val events = collectEvents(listOf(telemetry(speed = -10.0, timestamp = 0L), validReading))
        assertEquals(listOf(FleetTrackingEvent.TelemetryUpdated(validReading)), events)
    }


    @Test
    fun `should not emit delay before threshold`() = runBlocking {
        val events =
            collectEvents(listOf(telemetry(speed = 0.0, timestamp = 0L), telemetry(speed = 0.0, timestamp = 9_999L)))
        assertFalse(
            events.any {
                it is FleetTrackingEvent.PotentialDelayDetected
            }
        )
    }

    @Test
    fun `should emit delay at exact threshold`() = runBlocking {
        val events =
            collectEvents(listOf(telemetry(speed = 0.0, timestamp = 0L), telemetry(speed = 0.0, timestamp = 10_000L)))
        val delays = delayEvents(events)
        assertEquals(1, delays.size)
    }

    @Test
    fun `should emit delay after threshold`() = runBlocking {
        val events = collectEvents(
            listOf(
                telemetry(speed = 50.0, timestamp = 0L),
                telemetry(speed = 0.0, timestamp = 2_000L),
                telemetry(speed = 0.0, timestamp = 15_000L)
            )
        )
        val delays = delayEvents(events)
        assertEquals(1, delays.size)
    }

    @Test
    fun `should emit correct vehicle id in delay event`() = runBlocking {
        val events =
            collectEvents(listOf(telemetry(speed = 0.0, timestamp = 0L), telemetry(speed = 0.0, timestamp = 10_000L)))
        val delays = delayEvents(events)

        assertEquals("TRK-0001", delays.single().vehicleId)
    }

    @Test
    fun `should emit correct detection timestamp`() = runBlocking {
        val events = collectEvents(
            listOf(
                telemetry(speed = 0.0, timestamp = 2_000L),
                telemetry(speed = 0.0, timestamp = 12_000L)
            )
        )
        val delays = delayEvents(events)
        assertEquals(12_000L, delays.single().detectedAtMillis)
    }

    @Test
    fun `should emit delay only once during continuous stop`() = runBlocking {
        val events = collectEvents(
            listOf(
                telemetry(speed = 0.0, timestamp = 0L),
                telemetry(speed = 0.0, timestamp = 10_000L),
                telemetry(speed = 0.0, timestamp = 15_000L),
                telemetry(speed = 0.0, timestamp = 20_000L)
            )
        )
        assertEquals(1, delayEvents(events).size)
    }

    @Test
    fun `should reset stopping duration after movement`() = runBlocking {
        val events = collectEvents(
            listOf(
                telemetry(speed = 0.0, timestamp = 0L),
                telemetry(speed = 40.0, timestamp = 5_000L),
                telemetry(speed = 0.0, timestamp = 8_000L),
                telemetry(speed = 0.0, timestamp = 12_000L)
            )
        )
        assertTrue(delayEvents(events).isEmpty())
    }

    @Test
    fun `should emit new delay after vehicle stops again`() = runBlocking {
        val events = collectEvents(
            listOf(
                telemetry(speed = 0.0, timestamp = 0L),
                telemetry(speed = 0.0, timestamp = 10_000L),
                telemetry(speed = 50.0, timestamp = 12_000L),
                telemetry(speed = 0.0, timestamp = 14_000L),
                telemetry(speed = 0.0, timestamp = 24_000L)
            )
        )
        assertEquals(2, delayEvents(events).size)
    }

    @Test
    fun `should not emit delay while vehicle is moving`() = runBlocking {
        val events = collectEvents(
            listOf(
                telemetry(speed = 40.0, timestamp = 0L),
                telemetry(speed = 50.0, timestamp = 10_000L),
                telemetry(speed = 60.0, timestamp = 20_000L)
            )
        )
        assertTrue(delayEvents(events).isEmpty())
    }


    @Test
    fun `should track stopping time separately for each vehicle`() = runBlocking {
        val ids = listOf("TRK-0001", "TRK-0002")
        val events = collectEvents(
            updates = listOf(
                telemetry("TRK-0001", 0.0, 0L),
                telemetry("TRK-0002", 0.0, 5_000L),
                telemetry("TRK-0001", 0.0, 10_000L),
                telemetry("TRK-0002", 0.0, 10_000L)
            ),
            ids = ids
        )
        assertEquals(listOf("TRK-0001"), delayEvents(events).map { it.vehicleId })
    }

    @Test
    fun `should detect delays for multiple vehicles`() = runBlocking {
        val ids = listOf("TRK-0001", "TRK-0002")

        val events = collectEvents(
            updates = listOf(
                telemetry("TRK-0001", 0.0, 0L),
                telemetry("TRK-0002", 0.0, 2_000L),
                telemetry("TRK-0001", 0.0, 10_000L),
                telemetry("TRK-0002", 0.0, 12_000L)
            ),
            ids = ids
        )
        assertEquals(setOf("TRK-0001", "TRK-0002"), delayEvents(events).map { it.vehicleId }.toSet())
    }


    @Test
    fun `should maintain independent state for each collection`() = runBlocking {
        val updates = flowOf(telemetry(speed = 0.0, timestamp = 0L), telemetry(speed = 0.0, timestamp = 10_000L))
        every {
            vehicleRepository.streamTelemetry(vehicleIds)
        } returns updates
        val trackingFlow = useCase(vehicleIds)
        val firstCollection = trackingFlow.toList()
        val secondCollection = trackingFlow.toList()
        assertEquals(firstCollection, secondCollection)
    }


    private suspend fun collectEvents(
        updates: List<VehicleTelemetry>,
        ids: List<String> = vehicleIds
    ): List<FleetTrackingEvent> {
        every {
            vehicleRepository.streamTelemetry(ids)
        } returns updates.asFlow()
        return useCase(ids).toList()
    }

    private fun delayEvents(events: List<FleetTrackingEvent>): List<FleetTrackingEvent.PotentialDelayDetected> {
        return events.filterIsInstance<FleetTrackingEvent.PotentialDelayDetected>()
    }

    private fun telemetry(
        vehicleId: String = "TRK-0001",
        speed: Double = 40.0,
        timestamp: Long = 0L,
        fuel: Double = 80.0,
        latitude: Double = 31.5,
        longitude: Double = 34.4
    ): VehicleTelemetry {
        return VehicleTelemetry(
            vehicleId = vehicleId,
            latitude = latitude,
            longitude = longitude,
            speedKmh = speed,
            fuelPercentage = fuel,
            timestampMillis = timestamp
        )
    }
}

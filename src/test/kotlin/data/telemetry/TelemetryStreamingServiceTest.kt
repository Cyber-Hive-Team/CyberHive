package org.example.test.data.telemetry


import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.cancelAndJoin
import kotlinx.coroutines.flow.take
import kotlinx.coroutines.flow.toList
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.test.advanceTimeBy
import kotlinx.coroutines.test.runCurrent
import kotlinx.coroutines.test.runTest
import org.example.data.telemetry.TelemetryStreamingService
import org.example.domain.model.Vehicle
import org.example.domain.model.Warehouse
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows

class TelemetryStreamingServiceTest {
    private val service = TelemetryStreamingService(updateIntervalMillis = 10L)
    private fun vehicle(id: String = "TRK-0001", latitude: Double = 31.5, longitude: Double = 34.4)
            : Vehicle {
        val warehouse = mockk<Warehouse>()
        every { warehouse.latitude } returns latitude
        every { warehouse.longitude } returns longitude

        val vehicle = mockk<Vehicle>()
        every { vehicle.id } returns id
        every { vehicle.currentHub } returns warehouse
        return vehicle
    }

    @Test
    fun `should emit initial location from current hub`() = runBlocking {
        val truck = vehicle(latitude = 31.5, longitude = 34.4)
        val reading = service.stream(listOf(truck)).take(1).toList().single()
        assertEquals(31.5 to 34.4, reading.latitude to reading.longitude)
    }

    @Test
    fun `should start with zero speed`() = runBlocking {
        val reading = service.stream(listOf(vehicle())).take(1).toList().single()
        assertEquals(0.0, reading.speedKmh)
    }

    @Test
    fun `should increase speed after first update`() = runBlocking {
        val readings = service.stream(listOf(vehicle())).take(2).toList()
        assertTrue(readings[1].speedKmh > readings[0].speedKmh)
    }

    @Test
    fun `should update vehicle position`() = runBlocking {
        val readings = service.stream(listOf(vehicle())).take(2).toList()
        val first = readings[0]
        val second = readings[1]
        assertTrue(first.latitude != second.latitude || first.longitude != second.longitude)
    }

    @Test
    fun `should decrease fuel after movement`() = runBlocking {
        val readings = service.stream(listOf(vehicle())).take(2).toList()
        assertTrue(readings[1].fuelPercentage < readings[0].fuelPercentage)
    }

    @Test
    fun `should emit requested vehicle id`() = runBlocking {
        val reading = service.stream(listOf(vehicle(id = "TRK-0002"))).take(1).toList().single()
        assertEquals("TRK-0002", reading.vehicleId)
    }

    @Test
    fun `should emit updates for multiple vehicles`() = runBlocking {
        val trucks = listOf(vehicle(id = "TRK-0001"), vehicle(id = "TRK-0002"))
        val readings = service.stream(trucks).take(2).toList()
        assertEquals(setOf("TRK-0001", "TRK-0002"), readings.map { it.vehicleId }.toSet())
    }

    @Test
    fun `should not duplicate vehicles with same id`() = runBlocking {
        val trucks = listOf(vehicle(id = "TRK-0001"), vehicle(id = "TRK-0001"))
        val readings = service.stream(trucks).take(1).toList()
        assertEquals(1, readings.size)
    }

    @Test
    fun `should emit no updates for empty vehicle list`() = runBlocking {
        val readings = service.stream(emptyList()).toList()
        assertTrue(readings.isEmpty())
    }

    @Test
    fun `should restart simulation for each collection`() = runBlocking {
        val trackingFlow = service.stream(listOf(vehicle()))
        val firstReading = trackingFlow.take(1).toList().single()
        val secondReading = trackingFlow.take(1).toList().single()
        assertEquals(firstReading.copy(timestampMillis = 0L), secondReading.copy(timestampMillis = 0L))
    }

    @Test
    fun `should reject zero update interval`() {
        assertThrows<IllegalArgumentException> {
            TelemetryStreamingService(updateIntervalMillis = 0L)
        }
    }

    @Test
    fun `should reject negative update interval`() {
        assertThrows<IllegalArgumentException> {
            TelemetryStreamingService(updateIntervalMillis = -1L)
        }
    }

    @Test
    fun `should stop emitting updates after cancellation`() = runTest {
        val service = TelemetryStreamingService(
            updateIntervalMillis = 2_000L
        )

        var emissionCount = 0

        val job = backgroundScope.launch {
            service.stream(listOf(vehicle()))
                .collect {
                    emissionCount++
                }
        }

        runCurrent()
        advanceTimeBy(2_000L)
        runCurrent()

        job.cancelAndJoin()

        val countAtCancellation = emissionCount

        advanceTimeBy(10_000L)
        runCurrent()

        assertEquals(
            countAtCancellation,
            emissionCount
        )
    }


    @Test
    fun `should emit updates at configured interval`() = runTest {
        val service = TelemetryStreamingService(
            updateIntervalMillis = 2_000L
        )

        val emissionTimes = mutableListOf<Long>()

        val job = backgroundScope.launch {
            service.stream(listOf(vehicle()))
                .take(3)
                .collect {
                    emissionTimes.add(testScheduler.currentTime)
                }
        }

        runCurrent()
        advanceTimeBy(2_000L)
        runCurrent()
        advanceTimeBy(2_000L)
        runCurrent()

        assertEquals(
            listOf(0L, 2_000L, 4_000L),
            emissionTimes
        )
    }
}


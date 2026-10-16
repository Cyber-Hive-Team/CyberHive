package org.example.test.presentation.cli

import io.mockk.coEvery
import io.mockk.mockk
import kotlinx.coroutines.delay
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.withTimeout
import org.example.domain.model.result.RoutingResult
import org.example.presentation.cli.ConsoleInput
import org.example.presentation.cli.RouteCliCommand
import org.junit.jupiter.api.Test
import kotlin.test.assertEquals

class RouteCliCommandTest {

    @Test
    fun `should invoke route calculation and finish loading`() = runTest {
        // Given
        val input = mockk<ConsoleInput>()
        var receivedRoute: Pair<String, String>? = null

        coEvery {
            input.readLine()
        } returnsMany listOf("WH-065", "WH-087")

        val command = RouteCliCommand(
            input = input,
            computationDispatcher = StandardTestDispatcher(testScheduler),
            calculateRoute = { startId, destinationId ->
                receivedRoute = startId to destinationId
                delay(1_100)

                RoutingResult(
                    path = emptyList(),
                    distanceKm = Double.POSITIVE_INFINITY,
                )
            },
        )

        // When
        withTimeout(2_000) {
            command.run()
        }

        // Then
        assertEquals(
            "WH-065" to "WH-087",
            receivedRoute,
        )
    }
}

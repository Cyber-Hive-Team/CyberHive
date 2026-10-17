package org.example.presentation.cli

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.supervisorScope
import kotlinx.coroutines.withContext
import org.example.domain.usecase.FindOptimalPathUseCase
import org.koin.core.Koin

suspend fun launchDemoShell() = runShell(demoShell(ConsoleTerminal()))

suspend fun launchApplicationShell(koin: Koin) {
    val routeUseCase = koin.get<FindOptimalPathUseCase>()
    runShell(InteractiveShell(
        terminal = ConsoleTerminal(),
        routes = RouteCalculator { start, end ->
            val result = withContext(Dispatchers.Default) { routeUseCase(start, end) }
            if (result.path.isEmpty()) "No route found."
            else "${result.path.joinToString(" -> ") { it.id }} | ${result.distanceKm} km"
        },
        classifier = koin.getOrNull<PackageSectorClassifier>() ?: PackageSectorClassifier {
            error("Member 3 adapter is not registered. Use --demo for synthetic training cargo.")
        },
        telemetry = koin.getOrNull<FleetTelemetry>() ?: FleetTelemetry {
            kotlinx.coroutines.flow.flow { error("Member 2 adapter is not registered. Use --demo.") }
        },
    ))
}

private suspend fun runShell(shell: InteractiveShell) = supervisorScope {
    // Isolate intentional session cancellation from the application entry point.
    launch { shell.run() }.join()
}

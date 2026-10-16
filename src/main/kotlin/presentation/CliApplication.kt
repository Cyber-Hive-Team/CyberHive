package org.example.di

import io.ktor.client.HttpClient
import kotlinx.coroutines.Dispatchers
import org.example.domain.usecase.FindOptimalPathUseCase
import org.example.presentation.cli.ConsoleInput
import org.example.presentation.cli.InteractiveCliShell
import org.example.presentation.cli.RouteCliCommand
import org.koin.dsl.koinApplication

private fun createCliApplication() = koinApplication {
    modules(
        networkModule,
        repositoryModule,
        validatorModule,
        useCaseModule,
        dispatchModule,
        syncModule,
    )
}

suspend fun runCli() {
    val application = createCliApplication()
    var client: HttpClient? = null

    try {
        val input = ConsoleInput(Dispatchers.IO)

        val routeCommand = RouteCliCommand(
            input = input,
            computationDispatcher = Dispatchers.Default,
            calculateRoute = { startId, destinationId ->
                client = application.koin.get<HttpClient>()

                val useCase = application.koin
                    .get<FindOptimalPathUseCase>()

                useCase(startId, destinationId)
            },
        )

        InteractiveCliShell(
            input = input,
            routeCommand = routeCommand,
        ).run()
    } finally {
        client?.close()
        application.close()
    }
}

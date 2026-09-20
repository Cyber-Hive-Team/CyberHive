package org.example.data.repositoryImplementation

import org.example.data.exception.NullRequiredFieldException
import org.example.data.remote.dto.response.RouteResponseDto
import org.example.data.repositoryImplementation.dependencies.RouteRepositoryDependencies
import org.example.domain.model.Route
import org.example.domain.repository.RouteRepository


class RouteRepositoryImpl(
    private val dependencies: RouteRepositoryDependencies
) : RouteRepository {

    private val warnings =
        mutableListOf<String>()
    private val routes =
        mutableListOf<Route>()
    private var isLoaded = false

    override suspend fun getAllRoutes(): Result<List<Route>> {

        return runCatching {

            if (isLoaded) {
                return@runCatching routes.toList()
            }


            val loadedRoutes =
                dependencies.remoteDataSource
                    .getAll()
                    .mapNotNull {
                        mapRouteSafely(it)
                    }


            routes.addAll(
                loadedRoutes
            )


            isLoaded = true


            routes.toList()
        }
    }


    private fun mapRouteSafely(
        dto: RouteResponseDto
    ): Route? {
        return runCatching {
            val originWarehouse =
                findWarehouse(dto.originHubId, dto.routeId, "origin")
            val destinationWarehouse =
                findWarehouse(dto.destinationHubId, dto.routeId, "destination")
            dependencies.remoteValidator.validate(dto)

            dependencies.remoteMapper
                .mapToDomain(
                    raw = dto,
                    originWarehouse = originWarehouse,
                    destinationWarehouse = destinationWarehouse
                )
        }.getOrElse { exception ->
            if (exception is NullRequiredFieldException) {
                warnings.add("Route '${dto.routeId}': ${exception.message}")
                null
            } else {
                throw exception
            }
        }
    }


    private fun findWarehouse(
        warehouseId: String,
        routeId: String,
        type: String
    ) =

        dependencies.warehouseMap[warehouseId]
            ?: throw NullRequiredFieldException(
                "Route '$routeId' $type warehouse not found."
            )



    override suspend fun getById(
        routeId: String
    ): Result<Route> {
        val cachedRoute = routes.firstOrNull {
            it.id == routeId
        }
        if (cachedRoute != null) {
            return Result.success(cachedRoute)
        }
        return runCatching {
        val dto =
            dependencies.remoteDataSource
                .getById(routeId)
                ?: error("Route with id '$routeId' was not found.")
            val route = mapRouteSafely(dto) ?: error("Route mapping failed.")
            routes.add(route)
            route
        }
    }


    override suspend fun save(
        route: Route
    ): Result<Route> {
        return runCatching {
            val request =
                dependencies.remoteMapper
                    .mapToCreateRequest(route)
            val dto =
                dependencies.remoteDataSource
                    .save(request)
            val savedRoute =
                mapRouteSafely(dto)
                    ?: route
            routes.add(savedRoute)
            savedRoute
        }
    }


    override suspend fun update(
        route: Route
    ): Result<Route> {
        return runCatching {
            val request =
                dependencies.remoteMapper
                    .mapToUpdateRequest(route)
            val dto =
                dependencies.remoteDataSource
                    .update(
                        id = route.id,
                        request = request
                    )
            val updatedRoute = mapRouteSafely(dto)
                ?: route

            routes.removeIf {
                it.id == route.id
            }
            routes.add(updatedRoute)
            updatedRoute
        }
    }


    override suspend fun delete(
        id: String
    ): Result<Boolean> {
        return runCatching {
            val deleted = dependencies.remoteDataSource.delete(id)
            if (deleted) {

                routes.removeIf {
                    it.id == id
                }
            }
            deleted
        }
    }
}

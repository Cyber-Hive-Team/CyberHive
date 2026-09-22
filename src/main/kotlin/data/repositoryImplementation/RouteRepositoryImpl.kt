package org.example.data.repositoryImplementation

import org.example.data.exception.NullRequiredFieldException
import org.example.data.mapper.DataExceptionMapper
import org.example.data.mapper.mapFailureToDomain
import org.example.data.remote.dto.response.RouteResponseDto
import org.example.data.repositoryImplementation.dependencies.RouteRepositoryDependencies
import org.example.domain.model.Route
import org.example.domain.model.exception.RouteNotFoundException
import org.example.domain.repository.RouteRepository


class RouteRepositoryImpl(
    private val dependencies: RouteRepositoryDependencies,
    private val dataExceptionMapper: DataExceptionMapper = DataExceptionMapper()
) : BaseRepository(), RouteRepository {

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
                        mapRoute(it)
                    }


            routes.addAll(
                loadedRoutes
            )


            isLoaded = true


            routes.toList()
        }.mapFailureToDomain(dataExceptionMapper)
    }


    private fun mapRoute(
        dto: RouteResponseDto
    ): Route? {

        return mapSafely(dto.routeId) {

            val originWarehouse =
                findWarehouse(
                    dto.originHubId,
                    dto.routeId,
                    "origin"
                )

            val destinationWarehouse =
                findWarehouse(
                    dto.destinationHubId,
                    dto.routeId,
                    "destination"
                )

            dependencies.remoteMapper
                .mapToDomain(
                    raw = dto,
                    originWarehouse = originWarehouse,
                    destinationWarehouse = destinationWarehouse
                )
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
                ?: throw RouteNotFoundException()
            val route = mapRoute(dto) ?: throw NullRequiredFieldException("Route '$routeId' mapping failed.")
            routes.add(route)
            route
        }.mapFailureToDomain(dataExceptionMapper)
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
                mapRoute(dto)
                    ?: throw NullRequiredFieldException("Route '${route.id}' save mapping failed.")
            routes.add(savedRoute)
            savedRoute
        }.mapFailureToDomain(dataExceptionMapper)
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
            val updatedRoute = mapRoute(dto)
                ?: throw NullRequiredFieldException("Route '${route.id}' update mapping failed.")

            routes.removeIf {
                it.id == route.id
            }
            routes.add(updatedRoute)
            updatedRoute
        }.mapFailureToDomain(dataExceptionMapper)
    }


    override suspend fun delete(
        id: String
    ): Result<String> {
        return runCatching {
            val deletedId = dependencies.remoteDataSource.delete(id)
            routes.removeIf {
                it.id == deletedId
            }

            deletedId
        }.mapFailureToDomain(dataExceptionMapper)
    }
}

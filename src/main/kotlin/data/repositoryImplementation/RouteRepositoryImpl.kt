package org.example.data.repositoryImplementation

import org.example.data.datasource.remote.RouteRemoteDatasource
import org.example.data.exception.NullRequiredFieldException
import org.example.data.mapper.DataExceptionMapper
import org.example.data.mapper.mapFailureToDomain
import org.example.data.mapper.remote.toCreateRequest
import org.example.data.mapper.remote.toDomainModel
import org.example.data.mapper.remote.toUpdateRequest
import org.example.data.remote.dto.response.RouteResponseDto
import org.example.domain.model.Route
import org.example.domain.model.Warehouse
import org.example.domain.model.exception.RouteNotFoundException
import org.example.domain.repository.RouteRepository


class RouteRepositoryImpl(
    private val remoteDataSource: RouteRemoteDatasource,
    private val warehouseMap: Map<String, Warehouse>
) : BaseRepository(), RouteRepository {
    private val dataExceptionMapper = DataExceptionMapper()
    private val routes = mutableListOf<Route>()
    private var isLoaded = false

    override suspend fun getAllRoutes(): Result<List<Route>> {

        return runCatching {

            if (isLoaded) {
                return@runCatching routes.toList()
            }


            val loadedRoutes = remoteDataSource
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

            dto.toDomainModel(
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

        warehouseMap[warehouseId]
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
            val dto = remoteDataSource
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
            val request = route.toCreateRequest()
            val dto = remoteDataSource.save(request)
            val savedRoute = mapRoute(dto)
                    ?: throw NullRequiredFieldException("Route '${route.id}' save mapping failed.")
            routes.add(savedRoute)
            savedRoute
        }.mapFailureToDomain(dataExceptionMapper)
    }


    override suspend fun update(
        route: Route
    ): Result<Route> {
        return runCatching {
            val request = route.toUpdateRequest()
            val dto = remoteDataSource.update(id = route.id, request = request)
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
            val deletedId = remoteDataSource.delete(id)
            routes.removeIf {
                it.id == deletedId
            }

            deletedId
        }.mapFailureToDomain(dataExceptionMapper)
    }
}

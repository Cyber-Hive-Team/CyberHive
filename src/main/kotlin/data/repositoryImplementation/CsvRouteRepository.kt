package org.example.data.repository

import org.example.data.datasource.RouteDataSource
import org.example.data.mapper.RouteMapper
import org.example.data.validation.RouteValidator
import org.example.domain.model.Route
import org.example.domain.model.Warehouse
import org.example.domain.model.result.Result
import org.example.domain.repository.RouteRepository

class CsvRouteRepository(
    private val dataSource: RouteDataSource,
    private val mapper: RouteMapper,
    private val warehouseMap: Map<String, Warehouse>,
    private val validator: RouteValidator

) : RouteRepository {

    override fun getAllRoutes(): Result<List<Route>> {
        val rawResults = dataSource.getRoutes()
        val warnings = rawResults.mapNotNull { it.errorMessage }.toMutableList()
        val rawRoutes = rawResults.mapNotNull { it.rawData }
        val routes = mapRoutes(rawRoutes = rawRoutes, warnings = warnings)
        return Result(
            data = routes,
            errorMessage = warnings.takeIf { it.isNotEmpty() }
                ?.joinToString("; ")
        )
    }

    private fun mapRoutes(
        rawRoutes: List<org.example.data.dataholder.RouteRaw>,
        warnings: MutableList<String>
    ): List<Route> =
        rawRoutes.mapNotNull { raw ->
            val origin = warehouseMap[normalizeId(raw.originHubId)]
            val destination = warehouseMap[normalizeId(raw.destinationHubId)]

            val validation = validator.validate(raw, origin, destination)
            if (validation.isNotEmpty()) {
                warnings.addAll(validation)
                null
            } else {
                mapper.map(raw, origin!!, destination!!)
            }
        }


    private fun normalizeId(id: String): String =
        id.trim().uppercase()
}

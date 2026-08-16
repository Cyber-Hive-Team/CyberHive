package org.example.data.repository

import org.example.data.dataparsing.parseRoutes
import org.example.domain.model.Route
import org.example.domain.model.Warehouse
import org.example.domain.repository.Result
import org.example.domain.repository.RouteRepository
//الكلاس هذا لسا بده تعديل لانه لسا البارسينج فيه مشكلة ولانه موضوع الmapping
// لسا بده عمل عملته هيك عشان يرضى يرفع فقط
class CsvRouteRepository(
    private val warehouseMap: Map<String, Warehouse>
) : RouteRepository {

    override fun getAllRoutes(): Result<List<Route>> {
        val rawRoutes = parseRoutes()
        val routes = mutableListOf<Route>()

        for (rawRoute in rawRoutes) {
            val originWarehouse = warehouseMap[rawRoute.originHubId]
            val destinationWarehouse = warehouseMap[rawRoute.destinationHubId]

            if (originWarehouse == null || destinationWarehouse == null) {
                continue
            }

            routes.add(
                Route(
                    id = rawRoute.id,
                    distanceKm = rawRoute.distanceKm,
                    typicalDelayMin = rawRoute.typicalDelayMin,
                    originWarehouse = originWarehouse,
                    destinationWarehouse = destinationWarehouse
                )
            )
        }
        return Result(data = routes)
    }
}
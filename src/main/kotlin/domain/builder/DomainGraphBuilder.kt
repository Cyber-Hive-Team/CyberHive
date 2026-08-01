package org.example.domain.builder
import org.example.data.dataholder.FleetRaw
import org.example.data.dataholder.PackageRaw
import org.example.data.dataholder.RouteRaw
import org.example.data.dataholder.WareHouseRaw
import org.example.data.dataparsing.parseFleet
import org.example.data.dataparsing.parsePackages
import org.example.dataparsing.parseRoutes
import org.example.dataparsing.parseWarehouse
import org.example.domain.model.Package
import org.example.domain.model.Route
import org.example.domain.model.Vehicle
import org.example.domain.model.Warehouse


fun parseCSVFiles (){
    val warehouseList = parseWarehouse("src/main/resources/warehouses.csv")
    val packageList = parsePackages()
    val routeList = parseRoutes()
    val vehicleList = parseFleet()
    buildObjectGraph(warehouseList,packageList,routeList,vehicleList)

}
fun buildObjectGraph(warehouseList: List<WareHouseRaw>, packageList: List<PackageRaw>,
                     routeRawList: List<RouteRaw>, vehicleList: List<FleetRaw>): List<Warehouse> {
    val warehousesById = warehouseList.associateBy(
        keySelector = { warehouseRaw -> warehouseRaw.id },
        valueTransform = { warehouseRaw -> Warehouse(warehouseRaw.id, warehouseRaw.name, warehouseRaw.regionalZone) })
    val vehiclesByHubId = vehicleList.groupBy { vehicleRaw -> vehicleRaw.currentHubId }
    val packagesByOriginId = packageList.groupBy { packageRaw -> packageRaw.originHubId }
    val routesByOriginId = routeRawList.groupBy { routeRaw -> routeRaw.originHubId }
    warehousesById.forEach { (warehouseId, warehouse) ->
        addVehiclesToWarehouse(warehouse, vehiclesByHubId[warehouseId].orEmpty())
        addPackagesToWarehouse(warehouse, packagesByOriginId[warehouseId].orEmpty(), warehousesById)
        addRoutesToWarehouse(warehouse, routesByOriginId[warehouseId].orEmpty(), warehousesById)
    }
    return warehousesById.values.toList()
}

private fun addVehiclesToWarehouse(warehouse: Warehouse, vehicleRawList: List<FleetRaw>){
    vehicleRawList.forEach { vehicleRaw ->
        val vehicle = Vehicle(vehicleRaw.vehicleId, vehicleRaw.maxCapacityKg, vehicleRaw.costPerKm, warehouse)
        warehouse.addVehicle(vehicle)
    }
}

private fun addPackagesToWarehouse(warehouse: Warehouse, packageRawList: List<PackageRaw>,
                                   warehousesById: Map<String, Warehouse>){
    packageRawList.forEach { packageRaw ->
        val destinationWarehouse = warehousesById[packageRaw.destinationHubId]
        if (destinationWarehouse != null) {
            val packageItem = Package(packageRaw.id, packageRaw.weight, packageRaw.priority,
                warehouse, destinationWarehouse)
            warehouse.addPackage(packageItem)
        } else {
            println("Warning: destination warehouse ${packageRaw.destinationHubId}" +
                    " was not found for package ${packageRaw.id}.")
        }
    }
}

private fun addRoutesToWarehouse(warehouse: Warehouse, routeRawList: List<RouteRaw>,
                                 warehousesById: Map<String, Warehouse>){
    routeRawList.forEach { routeRaw ->
        val destinationWarehouse = warehousesById[routeRaw.destinationHubId]
        if (destinationWarehouse != null) {
            val route = Route(routeRaw.id, routeRaw.distanceKm, routeRaw.typicalDelayMin,
                warehouse, destinationWarehouse)
            warehouse.addRoute(route)
        } else {
            println("Warning: destination warehouse ${routeRaw.destinationHubId} " +
                    "was not found for route ${routeRaw.id}.")
        }
    }
}






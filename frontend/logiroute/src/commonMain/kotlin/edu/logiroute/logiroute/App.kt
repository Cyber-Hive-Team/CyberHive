package edu.logiroute.logiroute

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import edu.logiroute.logiroute.ui.components.PackagePriorityBadge
import edu.logiroute.logiroute.ui.components.RouteDetailCard
import edu.logiroute.logiroute.ui.components.VehicleDetailCard
import edu.logiroute.logiroute.ui.components.WarehouseIdentityBadge
import edu.logiroute.logiroute.ui.components.WarehouseSummaryCard
import edu.logiroute.logiroute.ui.preview.previewActiveWarehouse
import edu.logiroute.logiroute.ui.preview.previewDestinationWarehouse
import edu.logiroute.logiroute.ui.preview.previewEmptyWarehouse
import edu.logiroute.logiroute.ui.preview.previewLowPackage
import edu.logiroute.logiroute.ui.preview.previewOriginWarehouse
import edu.logiroute.logiroute.ui.preview.previewRouteLongTransit
import edu.logiroute.logiroute.ui.preview.previewRouteShortTransit
import edu.logiroute.logiroute.ui.preview.previewStandardPackage
import edu.logiroute.logiroute.ui.preview.previewUrgentPackage
import edu.logiroute.logiroute.ui.preview.previewVehicleHeavyLoad
import edu.logiroute.logiroute.ui.preview.previewVehicleOverloaded
import edu.logiroute.logiroute.ui.preview.previewVehicleSafeLoad
import edu.logiroute.logiroute.ui.theme.JetBlack

@Composable
fun App() {
    MaterialTheme {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(JetBlack)
                .padding(24.dp)
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            PackagePriorityBadge(pkg = previewUrgentPackage)
            PackagePriorityBadge(pkg = previewStandardPackage)
            PackagePriorityBadge(pkg = previewLowPackage)
            WarehouseIdentityBadge(warehouse = previewOriginWarehouse)
            WarehouseIdentityBadge(warehouse = previewDestinationWarehouse)
            WarehouseSummaryCard(warehouse = previewActiveWarehouse)
            WarehouseSummaryCard(warehouse = previewEmptyWarehouse)
            VehicleDetailCard(vehicle = previewVehicleSafeLoad, currentLoadKg = 2500.0)
            VehicleDetailCard(vehicle = previewVehicleHeavyLoad, currentLoadKg = 8200.0)
            VehicleDetailCard(vehicle = previewVehicleOverloaded, currentLoadKg = 10500.0)
            RouteDetailCard(route = previewRouteShortTransit)
            RouteDetailCard(route = previewRouteLongTransit)
        }
    }
}

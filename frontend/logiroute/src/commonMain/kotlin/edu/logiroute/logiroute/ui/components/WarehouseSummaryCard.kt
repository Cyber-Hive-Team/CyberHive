package edu.logiroute.logiroute.ui.components

import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import edu.logiroute.logiroute.ui.theme.Border
import edu.logiroute.logiroute.ui.theme.CharcoalBlue
import edu.logiroute.logiroute.ui.theme.TextDisabled
import edu.logiroute.logiroute.ui.theme.TextPrimary
import edu.logiroute.logiroute.ui.theme.TextSecondary
import org.example.domain.model.Warehouse

@Composable
fun WarehouseSummaryCard(
    warehouse: Warehouse,
    modifier: Modifier = Modifier
) {
    val topPackage = warehouse.getCargoQueue().maxByOrNull { it.priority.ordinal }

    Surface(
        modifier = modifier
            .fillMaxWidth()
            .border(width = 1.dp, color = Border, shape = RoundedCornerShape(12.dp)),
        color = CharcoalBlue,
        shape = RoundedCornerShape(12.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            WarehouseIdentityBadge(warehouse = warehouse)
            MetricsRow(
                packageCount = warehouse.getCargoQueue().size,
                vehicleCount = warehouse.getStationedVehicles().size
            )
            if (topPackage != null) {
                PackagePriorityBadge(pkg = topPackage)
            } else {
                Text(
                    text = "No Pending Cargo",
                    color = TextDisabled,
                    fontSize = 13.sp
                )
            }
        }
    }
}

@Composable
private fun MetricsRow(
    packageCount: Int,
    vehicleCount: Int
) {
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Text(
            text = "Warehouse Metrics",
            color = TextSecondary,
            fontSize = 11.sp,
            fontWeight = FontWeight.Bold
        )
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(24.dp)
        ) {
            MetricItem(label = "Packages", value = packageCount.toString())
            MetricItem(label = "Vehicles", value = vehicleCount.toString())
        }
    }
}

@Composable
private fun MetricItem(
    label: String,
    value: String
) {
    Column {
        Text(
            text = value,
            color = TextPrimary,
            fontSize = 16.sp,
            fontWeight = FontWeight.Bold
        )
        Text(
            text = label,
            color = TextSecondary,
            fontSize = 11.sp
        )
    }
}

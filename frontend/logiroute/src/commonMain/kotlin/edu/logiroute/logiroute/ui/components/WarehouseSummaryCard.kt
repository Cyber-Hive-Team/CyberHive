package edu.logiroute.logiroute.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import edu.logiroute.logiroute.ui.preview.previewActiveWarehouse
import edu.logiroute.logiroute.ui.preview.previewEmptyWarehouse
import edu.logiroute.logiroute.ui.theme.Border
import edu.logiroute.logiroute.ui.theme.CharcoalBlue
import edu.logiroute.logiroute.ui.theme.InkBlack
import edu.logiroute.logiroute.ui.theme.TextDisabled
import edu.logiroute.logiroute.ui.theme.TextPrimary
import edu.logiroute.logiroute.ui.theme.TextSecondary
import org.example.domain.model.Warehouse

private val CARD_SHAPE = RoundedCornerShape(12.dp)

@Composable
fun WarehouseSummaryCard(
    warehouse: Warehouse,
    modifier: Modifier = Modifier
) {
    val topPackage = warehouse.getCargoQueue().maxByOrNull { it.priority.ordinal }

    Box(
        modifier = modifier
            .fillMaxWidth()
            .clip(CARD_SHAPE)
            .drawBehind { drawRect(color = CharcoalBlue) }
            .border(width = 1.dp, color = Border, shape = CARD_SHAPE)
            .padding(16.dp)
    ) {
        Column(
            modifier = Modifier.fillMaxWidth(),
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

// ── Previews ──────────────────────────────────────────────────────────────────

@Preview
@Composable
fun PreviewWarehouseSummaryCardActive() {
    Column(
        modifier = Modifier
            .background(InkBlack)
            .padding(16.dp)
    ) {
        WarehouseSummaryCard(warehouse = previewActiveWarehouse)
    }
}

@Preview
@Composable
fun PreviewWarehouseSummaryCardEmpty() {
    Column(
        modifier = Modifier
            .background(InkBlack)
            .padding(16.dp)
    ) {
        WarehouseSummaryCard(warehouse = previewEmptyWarehouse)
    }
}

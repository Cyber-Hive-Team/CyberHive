package edu.logiroute.logiroute.ui.preview

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import edu.logiroute.logiroute.ui.components.PackagePriorityBadge
import edu.logiroute.logiroute.ui.components.WarehouseSummaryCard
import edu.logiroute.logiroute.ui.theme.InkBlack

@Preview
@Composable
fun PreviewPackagePriorityBadgeUrgent() {
    Column(
        modifier = Modifier
            .background(InkBlack)
            .padding(16.dp)
    ) {
        PackagePriorityBadge(pkg = previewUrgentPackage)
    }
}

@Preview
@Composable
fun PreviewPackagePriorityBadgeStandard() {
    Column(
        modifier = Modifier
            .background(InkBlack)
            .padding(16.dp)
    ) {
        PackagePriorityBadge(pkg = previewStandardPackage)
    }
}

@Preview
@Composable
fun PreviewPackagePriorityBadgeLow() {
    Column(
        modifier = Modifier
            .background(InkBlack)
            .padding(16.dp)
    ) {
        PackagePriorityBadge(pkg = previewLowPackage)
    }
}

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

@Preview
@Composable
fun PreviewWarehouseComponentsAllStates() {
    Column(
        modifier = Modifier
            .background(InkBlack)
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        PackagePriorityBadge(pkg = previewUrgentPackage)
        PackagePriorityBadge(pkg = previewStandardPackage)
        PackagePriorityBadge(pkg = previewLowPackage)
        WarehouseSummaryCard(warehouse = previewActiveWarehouse)
        WarehouseSummaryCard(warehouse = previewEmptyWarehouse)
    }
}

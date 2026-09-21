package edu.logiroute.logiroute.ui.preview

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import edu.logiroute.logiroute.ui.components.WarehouseIdentityBadge
import edu.logiroute.logiroute.ui.theme.InkBlack

@Preview
@Composable
fun WarehouseIdentityBadgeNorthPreview() {
    Column(
        modifier = Modifier
            .background(InkBlack)
            .padding(16.dp)
    ) {
        WarehouseIdentityBadge(warehouse = previewOriginWarehouse)
    }
}

@Preview
@Composable
fun WarehouseIdentityBadgeCentralPreview() {
    Column(
        modifier = Modifier
            .background(InkBlack)
            .padding(16.dp)
    ) {
        WarehouseIdentityBadge(warehouse = previewDestinationWarehouse)
    }
}

@Preview
@Composable
fun WarehouseIdentityBadgeAllZonesPreview() {
    Column(
        modifier = Modifier
            .background(InkBlack)
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        WarehouseIdentityBadge(warehouse = previewOriginWarehouse)
        WarehouseIdentityBadge(warehouse = previewDestinationWarehouse)
        WarehouseIdentityBadge(warehouse = previewWestWarehouse)
    }
}

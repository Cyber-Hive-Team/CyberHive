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
import edu.logiroute.logiroute.ui.theme.InkBlack

@Preview
@Composable
fun PackagePriorityBadgeUrgentPreview() {
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
fun PackagePriorityBadgeStandardPreview() {
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
fun PackagePriorityBadgeLowPreview() {
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
fun PackagePriorityBadgeAllStatesPreview() {
    Column(
        modifier = Modifier
            .background(InkBlack)
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        PackagePriorityBadge(pkg = previewUrgentPackage)
        PackagePriorityBadge(pkg = previewStandardPackage)
        PackagePriorityBadge(pkg = previewLowPackage)
    }
}

package edu.logiroute.logiroute

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import edu.logiroute.logiroute.ui.components.PackagePriorityBadge
import edu.logiroute.logiroute.ui.preview.previewLowPackage
import edu.logiroute.logiroute.ui.preview.previewStandardPackage
import edu.logiroute.logiroute.ui.preview.previewUrgentPackage
import edu.logiroute.logiroute.ui.theme.InkBlack

@Composable
fun App() {
    MaterialTheme {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(InkBlack)
                .padding(24.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            PackagePriorityBadge(pkg = previewUrgentPackage)
            PackagePriorityBadge(pkg = previewStandardPackage)
            PackagePriorityBadge(pkg = previewLowPackage)
        }
    }
}
package edu.logiroute.logiroute.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import edu.logiroute.logiroute.ui.theme.CharcoalBlue
import edu.logiroute.logiroute.ui.theme.ErrorRed
import edu.logiroute.logiroute.ui.theme.InkBlack
import edu.logiroute.logiroute.ui.theme.LightGreen
import edu.logiroute.logiroute.ui.theme.TextSecondary
import org.example.domain.model.Package
import org.example.domain.model.Priority

@Composable
fun PackagePriorityBadge(
    pkg: Package,
    modifier: Modifier = Modifier
) {
    val containerColor = resolveContainerColor(pkg.priority)
    val textColor = resolveTextColor(pkg.priority)

    Row(
        modifier = modifier
            .background(color = containerColor, shape = RoundedCornerShape(8.dp))
            .padding(horizontal = 12.dp, vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = pkg.id,
                color = textColor,
                fontSize = 13.sp,
                fontWeight = FontWeight.Bold,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            Text(
                text = pkg.priority.name
                    .lowercase()
                    .replaceFirstChar { it.uppercase() },
                color = textColor,
                fontSize = 11.sp
            )
        }
        Text(
            text = "${pkg.weight} kg",
            color = TextSecondary,
            fontSize = 12.sp
        )
    }
}

private fun resolveContainerColor(priority: Priority): Color = when (priority) {
    Priority.URGENT -> ErrorRed
    Priority.STANDARD -> LightGreen
    Priority.LOW -> CharcoalBlue
}

private fun resolveTextColor(priority: Priority): Color = when (priority) {
    Priority.URGENT -> InkBlack
    Priority.STANDARD -> InkBlack
    Priority.LOW -> TextSecondary
}

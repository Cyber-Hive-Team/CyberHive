package edu.logiroute.logiroute.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import edu.logiroute.logiroute.ui.theme.ErrorRed
import edu.logiroute.logiroute.ui.theme.JetBlack
import edu.logiroute.logiroute.ui.theme.LightGreen
import edu.logiroute.logiroute.ui.theme.TextSecondary
import edu.logiroute.logiroute.ui.theme.WarningAmber

private const val HEAVY_LOAD_THRESHOLD = 0.70f
private const val OVERLOAD_THRESHOLD = 0.90f
private const val TRACK_CORNER_RADIUS = 6f
private const val BAR_HEIGHT_DP = 10

@Composable
fun VehicleCapacityIndicator(
    currentLoadKg: Double,
    maxCapacityKg: Double,
    modifier: Modifier = Modifier
) {
    val ratio = (currentLoadKg / maxCapacityKg).toFloat().coerceIn(0.0f, 1.0f)
    val fillColor = resolveLoadColor(ratio)
    val percentageLabel = "${(ratio * 100).toInt()}%"

    Column(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(BAR_HEIGHT_DP.dp)
                .drawBehind {
                    drawRoundRect(
                        color = JetBlack,
                        size = size,
                        cornerRadius = CornerRadius(TRACK_CORNER_RADIUS)
                    )
                    drawRoundRect(
                        color = fillColor,
                        size = Size(width = size.width * ratio, height = size.height),
                        cornerRadius = CornerRadius(TRACK_CORNER_RADIUS)
                    )
                }
        )

        Text(
            text = percentageLabel,
            color = TextSecondary,
            fontSize = 11.sp,
            fontWeight = FontWeight.Medium,
            modifier = Modifier.align(Alignment.End)
        )
    }
}

private fun resolveLoadColor(ratio: Float): Color = when {
    ratio > OVERLOAD_THRESHOLD -> ErrorRed
    ratio >= HEAVY_LOAD_THRESHOLD -> WarningAmber
    else -> LightGreen
}

@Preview
@Composable
fun PreviewVehicleCapacityIndicatorSafeLoad() {
    Column(
        modifier = Modifier
            .background(JetBlack)
            .padding(16.dp)
    ) {
        VehicleCapacityIndicator(
            currentLoadKg = 2500.0,
            maxCapacityKg = 10000.0
        )
    }
}

@Preview
@Composable
fun PreviewVehicleCapacityIndicatorHeavyLoad() {
    Column(
        modifier = Modifier
            .background(JetBlack)
            .padding(16.dp)
    ) {
        VehicleCapacityIndicator(
            currentLoadKg = 8200.0,
            maxCapacityKg = 10000.0
        )
    }
}

@Preview
@Composable
fun PreviewVehicleCapacityIndicatorOverloaded() {
    Column(
        modifier = Modifier
            .background(JetBlack)
            .padding(16.dp)
    ) {
        VehicleCapacityIndicator(
            currentLoadKg = 10500.0,
            maxCapacityKg = 10000.0
        )
    }
}

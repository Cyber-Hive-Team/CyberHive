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
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import edu.logiroute.logiroute.ui.preview.previewVehicleHeavyLoad
import edu.logiroute.logiroute.ui.preview.previewVehicleOverloaded
import edu.logiroute.logiroute.ui.preview.previewVehicleSafeLoad
import edu.logiroute.logiroute.ui.theme.Border
import edu.logiroute.logiroute.ui.theme.CharcoalBlue
import edu.logiroute.logiroute.ui.theme.CyberSprout
import edu.logiroute.logiroute.ui.theme.JetBlack
import edu.logiroute.logiroute.ui.theme.LightGreen
import edu.logiroute.logiroute.ui.theme.TextPrimary
import edu.logiroute.logiroute.ui.theme.TextSecondary
import org.example.domain.model.Vehicle

private val CARD_SHAPE = RoundedCornerShape(12.dp)

@Composable
fun VehicleDetailCard(
    vehicle: Vehicle,
    modifier: Modifier = Modifier,
    currentLoadKg: Double = 0.0
) {
    Box(
        modifier = modifier
            .fillMaxWidth()
            .clip(CARD_SHAPE)
            .drawBehind { drawRect(color = CharcoalBlue) }
            .border(width = 1.dp, color = Border, shape = CARD_SHAPE)
            .padding(16.dp)
    ) {
        Column(
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            HeaderSection(vehicle = vehicle)

            MetricsSection(vehicle = vehicle)

            VehicleCapacityIndicator(
                currentLoadKg = currentLoadKg,
                maxCapacityKg = vehicle.maxCapacityKg
            )
        }
    }
}

@Composable
private fun HeaderSection(vehicle: Vehicle) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(
            text = vehicle.id,
            color = TextPrimary,
            fontSize = 15.sp,
            fontWeight = FontWeight.Bold
        )
        Box(
            modifier = Modifier
                .border(
                    width = 1.dp,
                    color = CyberSprout,
                    shape = RoundedCornerShape(4.dp)
                )
                .padding(horizontal = 8.dp, vertical = 4.dp)
        ) {
            Text(
                text = vehicle.currentHub.name,
                color = CyberSprout,
                fontSize = 11.sp,
                fontWeight = FontWeight.Medium
            )
        }
    }
}

@Composable
private fun MetricsSection(vehicle: Vehicle) {
    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
        val costText = buildAnnotatedString {
            withStyle(
                style = SpanStyle(
                    color = LightGreen,
                    fontWeight = FontWeight.Bold,
                    fontSize = 14.sp
                )
            ) {
                append("$%.2f".format(vehicle.costPerKm))
            }
            withStyle(
                style = SpanStyle(
                    color = TextSecondary,
                    fontSize = 14.sp
                )
            ) {
                append(" /km")
            }
        }
        Text(text = costText)

        val capacityText = buildAnnotatedString {
            withStyle(
                style = SpanStyle(
                    color = TextPrimary,
                    fontWeight = FontWeight.Bold,
                    fontSize = 14.sp
                )
            ) {
                append("%.0f".format(vehicle.maxCapacityKg))
            }
            withStyle(
                style = SpanStyle(
                    color = TextSecondary,
                    fontSize = 14.sp
                )
            ) {
                append(" kg max")
            }
        }
        Text(text = capacityText)
    }
}

@Preview
@Composable
fun PreviewVehicleDetailCardSafeLoad() {
    Column(
        modifier = Modifier
            .background(JetBlack)
            .padding(16.dp)
    ) {
        VehicleDetailCard(
            vehicle = previewVehicleSafeLoad,
            currentLoadKg = 2500.0
        )
    }
}

@Preview
@Composable
fun PreviewVehicleDetailCardHeavyLoad() {
    Column(
        modifier = Modifier
            .background(JetBlack)
            .padding(16.dp)
    ) {
        VehicleDetailCard(
            vehicle = previewVehicleHeavyLoad,
            currentLoadKg = 8200.0
        )
    }
}

@Preview
@Composable
fun PreviewVehicleDetailCardOverloaded() {
    Column(
        modifier = Modifier
            .background(JetBlack)
            .padding(16.dp)
    ) {
        VehicleDetailCard(
            vehicle = previewVehicleOverloaded,
            currentLoadKg = 10500.0
        )
    }
}

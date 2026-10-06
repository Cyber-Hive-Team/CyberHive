package edu.logiroute.logiroute.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
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
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import edu.logiroute.logiroute.ui.preview.previewRouteLongTransit
import edu.logiroute.logiroute.ui.preview.previewRouteShortTransit
import edu.logiroute.logiroute.ui.theme.Border
import edu.logiroute.logiroute.ui.theme.CharcoalBlue
import edu.logiroute.logiroute.ui.theme.JetBlack
import edu.logiroute.logiroute.ui.theme.SapphireSky
import edu.logiroute.logiroute.ui.theme.TextPrimary
import edu.logiroute.logiroute.ui.theme.TextSecondary
import edu.logiroute.logiroute.ui.theme.WarningAmber
import org.example.domain.model.Route

private val CARD_SHAPE = RoundedCornerShape(12.dp)
private const val LONG_DELAY_THRESHOLD_MIN = 60

@Composable
fun RouteDetailCard(
    route: Route,
    onClick: (Route) -> Unit = {},
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .fillMaxWidth()
            .clip(CARD_SHAPE)
            .drawBehind { drawRect(color = CharcoalBlue) }
            .border(width = 1.dp, color = Border, shape = CARD_SHAPE)
            .clickable { onClick(route) }
            .padding(16.dp)
    ) {
        Column(
            modifier = Modifier.fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            JourneyRow(route = route)
            MetricsRow(route = route)
        }
    }
}

@Composable
private fun JourneyRow(route: Route) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = route.originWarehouse.name,
            color = TextPrimary,
            fontSize = 14.sp,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.weight(1f),
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
            textAlign = TextAlign.Start
        )

        Box(
            modifier = Modifier.width(48.dp),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = "→",
                color = SapphireSky,
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold
            )
        }

        Text(
            text = route.destinationWarehouse.name,
            color = TextPrimary,
            fontSize = 14.sp,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.weight(1f),
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
            textAlign = TextAlign.Start
        )
    }
}

@Composable
private fun MetricsRow(route: Route) {
    val delayColor = if (route.typicalDelayMin > LONG_DELAY_THRESHOLD_MIN) WarningAmber else TextSecondary

    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(24.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        val distanceText = buildAnnotatedString {
            withStyle(
                style = SpanStyle(
                    color = TextPrimary,
                    fontWeight = FontWeight.Bold,
                    fontSize = 13.sp
                )
            ) {
                append("%.0f".format(route.distanceKm))
            }
            withStyle(
                style = SpanStyle(
                    color = TextSecondary,
                    fontSize = 13.sp
                )
            ) {
                append(" km")
            }
        }
        Text(text = distanceText)

        val delayText = buildAnnotatedString {
            withStyle(
                style = SpanStyle(
                    color = delayColor,
                    fontWeight = FontWeight.Bold,
                    fontSize = 13.sp
                )
            ) {
                append("${route.typicalDelayMin}")
            }
            withStyle(
                style = SpanStyle(
                    color = delayColor,
                    fontSize = 13.sp
                )
            ) {
                append(" min delay")
            }
        }
        Text(text = delayText)
    }
}

@Preview
@Composable
fun PreviewRouteDetailCardShortTransit() {
    Column(
        modifier = Modifier
            .background(JetBlack)
            .padding(16.dp)
    ) {
        RouteDetailCard(route = previewRouteShortTransit)
    }
}

@Preview
@Composable
fun PreviewRouteDetailCardLongTransit() {
    Column(
        modifier = Modifier
            .background(JetBlack)
            .padding(16.dp)
    ) {
        RouteDetailCard(route = previewRouteLongTransit)
    }
}

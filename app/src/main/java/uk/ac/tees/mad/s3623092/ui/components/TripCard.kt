package uk.ac.tees.mad.s3623092.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import uk.ac.tees.mad.s3623092.ui.model.TripUiModel
import uk.ac.tees.mad.s3623092.ui.theme.TravelPackTheme
import uk.ac.tees.mad.s3623092.util.TripDateUtils

@Composable
fun TripCard(
    trip: TripUiModel,
    selected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val cardColor =
        if (selected) {
            MaterialTheme.colorScheme.primaryContainer
        } else {
            MaterialTheme.colorScheme.surfaceVariant
        }

    val borderColor =
        if (selected) {
            MaterialTheme.colorScheme.primary
        } else {
            MaterialTheme.colorScheme.outlineVariant
        }

    Card(
        onClick = onClick,
        modifier = modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = cardColor
        ),
        border = BorderStroke(
            width = 1.dp,
            color = borderColor
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement =
                    Arrangement.SpaceBetween
            ) {
                Text(
                    text = trip.name.ifBlank {
                        "Unnamed trip"
                    },
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.weight(1f)
                )

                if (selected) {
                    Text(
                        text = "Selected",
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.primary,
                        fontWeight = FontWeight.Bold
                    )
                }
            }

            Text(
                text = "${trip.fromLocation} → ${trip.toLocation}",
                style = MaterialTheme.typography.bodyMedium,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis
            )

            Text(
                text = formatTripDates(
                    trip.startDate,
                    trip.endDate
                ),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme
                    .onSurfaceVariant
            )

            if (trip.notes.isNotBlank()) {
                Text(
                    text = trip.notes,
                    style = MaterialTheme.typography.bodySmall,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis,
                    color = MaterialTheme.colorScheme
                        .onSurfaceVariant
                )
            }
        }
    }
}

private fun formatTripDates(
    startDate: String,
    endDate: String
): String {
    val formattedStart =
        TripDateUtils.convertStorageDateToUser(
            startDate
        )

    val formattedEnd =
        TripDateUtils.convertStorageDateToUser(
            endDate
        )

    return "$formattedStart – $formattedEnd"
}

@Preview(
    showBackground = true,
    widthDp = 380
)
@Composable
private fun TripCardPreview() {
    TravelPackTheme {
        TripCard(
            trip = TripUiModel(
                id = 1L,
                name = "Scotland Trip",
                fromLocation = "London",
                toLocation = "Edinburgh",
                startDate = "2026-12-10",
                endDate = "2026-12-15",
                notes = "Winter holiday"
            ),
            selected = true,
            onClick = {}
        )
    }
}
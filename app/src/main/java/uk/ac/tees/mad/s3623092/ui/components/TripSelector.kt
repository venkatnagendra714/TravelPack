package uk.ac.tees.mad.s3623092.ui.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import uk.ac.tees.mad.s3623092.ui.model.TripUiModel
import uk.ac.tees.mad.s3623092.ui.theme.TravelPackTheme

@Composable
fun TripSelector(
    trips: List<TripUiModel>,
    selectedTripId: Long?,
    onTripSelected: (TripUiModel) -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Text(
            text = "Select a trip",
            style = MaterialTheme.typography.labelLarge
        )

        if (trips.isEmpty()) {
            Text(
                text = "No trips available",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme
                    .onSurfaceVariant
            )
        } else {
            LazyRow(
                modifier = Modifier.fillMaxWidth(),
                contentPadding = PaddingValues(
                    horizontal = 4.dp
                ),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(
                    items = trips,
                    key = { trip ->
                        trip.id
                    }
                ) { trip ->
                    val isSelected =
                        trip.id == selectedTripId

                    FilterChip(
                        selected = isSelected,
                        onClick = {
                            onTripSelected(trip)
                        },
                        label = {
                            Text(
                                text = trip.name.ifBlank {
                                    "Unnamed trip"
                                },
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                        },
                        modifier = Modifier.padding(
                            vertical = 2.dp
                        ),
                        colors = FilterChipDefaults
                            .filterChipColors(
                                selectedContainerColor =
                                    MaterialTheme.colorScheme.primary,
                                selectedLabelColor =
                                    MaterialTheme.colorScheme.onPrimary
                            )
                    )
                }
            }
        }
    }
}

@Preview(
    showBackground = true,
    widthDp = 400
)
@Composable
private fun TripSelectorPreview() {
    val trips = listOf(
        TripUiModel(
            id = 1L,
            name = "Scotland Trip",
            fromLocation = "London",
            toLocation = "Edinburgh",
            startDate = "2026-12-10",
            endDate = "2026-12-15"
        ),
        TripUiModel(
            id = 2L,
            name = "Paris Trip",
            fromLocation = "London",
            toLocation = "Paris",
            startDate = "2027-01-10",
            endDate = "2027-01-15"
        )
    )

    TravelPackTheme {
        TripSelector(
            trips = trips,
            selectedTripId = 1L,
            onTripSelected = {}
        )
    }
}
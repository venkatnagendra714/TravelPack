package uk.ac.tees.mad.s3623092.ui.tabs

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import uk.ac.tees.mad.s3623092.ui.components.TripCard
import uk.ac.tees.mad.s3623092.ui.components.TripSelector
import uk.ac.tees.mad.s3623092.ui.model.TripUiModel
import uk.ac.tees.mad.s3623092.ui.theme.TravelPackTheme

@Composable
fun OverviewTab(
    trips: List<TripUiModel>,
    selectedTripId: Long?,
    onTripSelected: (TripUiModel) -> Unit,
    onAddTrip: () -> Unit,
    onManageTrips: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(
                rememberScrollState()
            )
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Text(
            text = "Trip overview",
            style = MaterialTheme.typography.headlineSmall,
            fontWeight = FontWeight.Bold
        )

        if (trips.isEmpty()) {
            EmptyTripContent(
                onAddTrip = onAddTrip
            )
        } else {
            TripSelector(
                trips = trips,
                selectedTripId = selectedTripId,
                onTripSelected = onTripSelected
            )

            val selectedTrip =
                trips.firstOrNull {
                    it.id == selectedTripId
                } ?: trips.first()

            TripCard(
                trip = selectedTrip,
                selected = true,
                onClick = {
                    onTripSelected(selectedTrip)
                }
            )

            OutlinedButton(
                onClick = onManageTrips,
                modifier = Modifier.fillMaxSize()
            ) {
                Text("Manage trips")
            }

            Button(
                onClick = onAddTrip,
                modifier = Modifier.fillMaxSize()
            ) {
                Text("Add another trip")
            }
        }
    }
}

@Composable
private fun EmptyTripContent(
    onAddTrip: () -> Unit
) {
    Card(
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme
                .primaryContainer
        )
    ) {
        Column(
            modifier = Modifier.padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Text(
                text = "No trips added yet",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold
            )

            Text(
                text = "Add your first trip to organise your travel details."
            )

            Button(
                onClick = onAddTrip,
                modifier = Modifier.fillMaxSize()
            ) {
                Text("Add your first trip")
            }
        }
    }
}

@Preview(
    showBackground = true,
    showSystemUi = true,
    widthDp = 400,
    heightDp = 800
)
@Composable
private fun OverviewTabPreview() {
    val sampleTrip = TripUiModel(
        id = 1L,
        name = "Scotland Trip",
        fromLocation = "London",
        toLocation = "Edinburgh",
        startDate = "2026-12-10",
        endDate = "2026-12-15",
        notes = "Winter holiday"
    )

    TravelPackTheme {
        OverviewTab(
            trips = listOf(sampleTrip),
            selectedTripId = sampleTrip.id,
            onTripSelected = {},
            onAddTrip = {},
            onManageTrips = {}
        )
    }
}
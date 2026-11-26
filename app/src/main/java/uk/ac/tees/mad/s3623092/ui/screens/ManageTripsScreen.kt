package uk.ac.tees.mad.s3623092.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding

import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import uk.ac.tees.mad.s3623092.ui.components.TripCard
import uk.ac.tees.mad.s3623092.ui.model.TripListTab
import uk.ac.tees.mad.s3623092.ui.model.TripSortOption
import uk.ac.tees.mad.s3623092.ui.model.TripUiModel
import uk.ac.tees.mad.s3623092.ui.theme.TravelPackTheme
import uk.ac.tees.mad.s3623092.util.TripDateUtils

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ManageTripsScreen(
    trips: List<TripUiModel>,
    selectedTab: TripListTab,
    searchQuery: String,
    sortOption: TripSortOption,
    onTabSelected: (TripListTab) -> Unit,
    onSearchQueryChange: (String) -> Unit,
    onSortOptionChange: (TripSortOption) -> Unit,
    onEditTrip: (TripUiModel) -> Unit,
    onDeleteTrip: (TripUiModel) -> Unit,
    onShareTrip: (TripUiModel) -> Unit,
    onBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    var tripPendingDeletion by remember {
        mutableStateOf<TripUiModel?>(null)
    }

    val visibleTrips = remember(
        trips,
        selectedTab,
        searchQuery,
        sortOption
    ) {
        trips
            .filter { trip ->
                val matchesTab =
                    when (selectedTab) {
                        TripListTab.UPCOMING ->
                            !TripDateUtils.isCompleted(
                                trip.endDate
                            )

                        TripListTab.COMPLETED ->
                            TripDateUtils.isCompleted(
                                trip.endDate
                            )
                    }

                val query = searchQuery
                    .trim()
                    .lowercase()

                val matchesSearch =
                    query.isBlank() ||
                            trip.name.lowercase()
                                .contains(query) ||
                            trip.fromLocation.lowercase()
                                .contains(query) ||
                            trip.toLocation.lowercase()
                                .contains(query)

                matchesTab && matchesSearch
            }
            .let { filteredTrips ->
                when (sortOption) {
                    TripSortOption.START_DATE ->
                        filteredTrips.sortedWith { first, second ->
                            TripDateUtils.compareDates(
                                first.startDate,
                                second.startDate
                            )
                        }

                    TripSortOption.END_DATE ->
                        filteredTrips.sortedWith { first, second ->
                            TripDateUtils.compareDates(
                                first.endDate,
                                second.endDate
                            )
                        }

                    TripSortOption.NAME ->
                        filteredTrips.sortedBy {
                            it.name.lowercase()
                        }

                    TripSortOption.FROM_LOCATION ->
                        filteredTrips.sortedBy {
                            it.fromLocation.lowercase()
                        }

                    TripSortOption.TO_LOCATION ->
                        filteredTrips.sortedBy {
                            it.toLocation.lowercase()
                        }
                }
            }
    }

    Scaffold(
        modifier = modifier,
        topBar = {
            TopAppBar(
                title = {
                    Text("Manage trips")
                },
                navigationIcon = {
                    TextButton(
                        onClick = onBack
                    ) {
                        Text("Back")
                    }
                }
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(
                    top = innerPadding.calculateTopPadding(),
                    bottom = innerPadding.calculateBottomPadding(),
                    start = 16.dp,
                    end = 16.dp
                ),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            val tabs = listOf(
                TripListTab.UPCOMING,
                TripListTab.COMPLETED
            )

            TabRow(
                selectedTabIndex = selectedTab.ordinal
            ) {
                tabs.forEach { tab ->
                    Tab(
                        selected = tab == selectedTab,
                        onClick = {
                            onTabSelected(tab)
                        },
                        text = {
                            Text(
                                text = tabTitle(tab)
                            )
                        }
                    )
                }
            }

            OutlinedTextField(
                value = searchQuery,
                onValueChange = onSearchQueryChange,
                label = {
                    Text("Search trips")
                },
                singleLine = true,
                modifier = Modifier.fillMaxWidth()
            )

            SortMenu(
                selectedOption = sortOption,
                onOptionSelected = onSortOptionChange
            )

            if (visibleTrips.isEmpty()) {
                Text(
                    text = "No trips found.",
                    color = MaterialTheme.colorScheme
                        .onSurfaceVariant
                )
            } else {
                LazyColumn(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    items(
                        items = visibleTrips,
                        key = { trip ->
                            trip.id
                        }
                    ) { trip ->
                        Column(
                            modifier = Modifier.fillMaxWidth(),
                            verticalArrangement =
                                Arrangement.spacedBy(8.dp)
                        ) {
                            TripCard(
                                trip = trip,
                                selected = false,
                                onClick = {
                                    onEditTrip(trip)
                                }
                            )

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement =
                                    Arrangement.spacedBy(8.dp)
                            ) {
                                OutlinedButton(
                                    onClick = {
                                        onEditTrip(trip)
                                    },
                                    modifier = Modifier.weight(1f)
                                ) {
                                    Text("Edit")
                                }

                                OutlinedButton(
                                    onClick = {
                                        onShareTrip(trip)
                                    },
                                    modifier = Modifier.weight(1f)
                                ) {
                                    Text("Share")
                                }

                                Button(
                                    onClick = {
                                        tripPendingDeletion = trip
                                    },
                                    modifier = Modifier.weight(1f)
                                ) {
                                    Text("Delete")
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    tripPendingDeletion?.let { trip ->
        AlertDialog(
            onDismissRequest = {
                tripPendingDeletion = null
            },
            title = {
                Text("Delete trip?")
            },
            text = {
                Text(
                    "Are you sure you want to delete \"${trip.name}\"?"
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        onDeleteTrip(trip)
                        tripPendingDeletion = null
                    }
                ) {
                    Text("Delete")
                }
            },
            dismissButton = {
                TextButton(
                    onClick = {
                        tripPendingDeletion = null
                    }
                ) {
                    Text("Cancel")
                }
            }
        )
    }
}

@Composable
private fun SortMenu(
    selectedOption: TripSortOption,
    onOptionSelected: (TripSortOption) -> Unit
) {
    var expanded by remember {
        mutableStateOf(false)
    }

    Column(
        modifier = Modifier.fillMaxWidth()
    ) {
        OutlinedButton(
            onClick = {
                expanded = true
            },
            modifier = Modifier.fillMaxWidth()
        ) {
            Text(
                text = "Sort: ${sortOptionTitle(selectedOption)}"
            )
        }

        DropdownMenu(
            expanded = expanded,
            onDismissRequest = {
                expanded = false
            }
        ) {
            TripSortOption.entries.forEach { option ->
                DropdownMenuItem(
                    text = {
                        Text(
                            text = sortOptionTitle(option)
                        )
                    },
                    onClick = {
                        onOptionSelected(option)
                        expanded = false
                    }
                )
            }
        }
    }
}

private fun tabTitle(
    tab: TripListTab
): String {
    return when (tab) {
        TripListTab.UPCOMING -> "Upcoming"
        TripListTab.COMPLETED -> "Completed"
    }
}

private fun sortOptionTitle(
    option: TripSortOption
): String {
    return when (option) {
        TripSortOption.START_DATE -> "Start date"
        TripSortOption.END_DATE -> "End date"
        TripSortOption.NAME -> "Trip name"
        TripSortOption.FROM_LOCATION -> "From location"
        TripSortOption.TO_LOCATION -> "To location"
    }
}

@Preview(
    showBackground = true,
    showSystemUi = true,
    widthDp = 400,
    heightDp = 800
)
@Composable
private fun ManageTripsScreenPreview() {
    val sampleTrips = listOf(
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
            startDate = "2026-05-10",
            endDate = "2026-05-15"
        )
    )

    TravelPackTheme {
        ManageTripsScreen(
            trips = sampleTrips,
            selectedTab = TripListTab.UPCOMING,
            searchQuery = "",
            sortOption = TripSortOption.START_DATE,
            onTabSelected = {},
            onSearchQueryChange = {},
            onSortOptionChange = {},
            onEditTrip = {},
            onDeleteTrip = {},
            onShareTrip = {},
            onBack = {}
        )
    }
}
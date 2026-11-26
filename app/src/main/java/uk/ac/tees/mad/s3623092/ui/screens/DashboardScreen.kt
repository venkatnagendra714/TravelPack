package uk.ac.tees.mad.s3623092.ui.screens

import android.net.Uri
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import uk.ac.tees.mad.s3623092.ui.model.AttachmentUiModel
import uk.ac.tees.mad.s3623092.ui.model.ChecklistUiModel
import uk.ac.tees.mad.s3623092.ui.model.TripUiModel
import uk.ac.tees.mad.s3623092.ui.tabs.ChecklistTab
import uk.ac.tees.mad.s3623092.ui.tabs.DocumentsTab
import uk.ac.tees.mad.s3623092.ui.tabs.OverviewTab
import uk.ac.tees.mad.s3623092.ui.tabs.WeatherTab
import uk.ac.tees.mad.s3623092.ui.theme.TravelPackTheme
import uk.ac.tees.mad.s3623092.weather.WeatherUiState

enum class DashboardTab {
    OVERVIEW,
    CHECKLIST,
    DOCUMENTS,
    WEATHER
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DashboardScreen(
    trips: List<TripUiModel>,
    selectedTripId: Long?,
    selectedTab: DashboardTab,
    onTripSelected: (TripUiModel) -> Unit,
    onTabSelected: (DashboardTab) -> Unit,
    onAddTrip: () -> Unit,
    onManageTrips: () -> Unit,
    checklistItems: List<ChecklistUiModel>,
    newChecklistItemTitle: String,
    onNewChecklistItemTitleChange: (String) -> Unit,
    onAddChecklistItem: () -> Unit,
    onToggleChecklistItem: (ChecklistUiModel) -> Unit,
    onDeleteChecklistItem: (ChecklistUiModel) -> Unit,
    attachments: List<AttachmentUiModel>,
    onAttachmentsSelected: (List<Uri>) -> Unit,
    onOpenAttachment: (AttachmentUiModel) -> Unit,
    onDeleteAttachment: (AttachmentUiModel) -> Unit,
    weatherState: WeatherUiState,
    onRetryWeather: () -> Unit,
    modifier: Modifier = Modifier
) {
    Scaffold(
        modifier = modifier,
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "Trip Dashboard"
                    )
                },
                actions = {
                    TextButton(
                        onClick = onManageTrips
                    ) {
                        Text(
                            text = "Manage"
                        )
                    }

                    TextButton(
                        onClick = onAddTrip
                    ) {
                        Text(
                            text = "Add trip"
                        )
                    }
                }
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier.padding(
                top = innerPadding.calculateTopPadding(),
                bottom = innerPadding.calculateBottomPadding()
            )
        ) {
            val tabs = listOf(
                DashboardTab.OVERVIEW,
                DashboardTab.CHECKLIST,
                DashboardTab.DOCUMENTS,
                DashboardTab.WEATHER
            )

            TabRow(
                selectedTabIndex = selectedTab.ordinal
            ) {
                tabs.forEach { tab ->
                    Tab(
                        selected = selectedTab == tab,
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

            when (selectedTab) {
                DashboardTab.OVERVIEW -> {
                    OverviewTab(
                        trips = trips,
                        selectedTripId = selectedTripId,
                        onTripSelected = onTripSelected,
                        onAddTrip = onAddTrip,
                        onManageTrips = onManageTrips,
                        modifier = Modifier.weight(1f)
                    )
                }

                DashboardTab.CHECKLIST -> {
                    ChecklistTab(
                        trips = trips,
                        selectedTripId = selectedTripId,
                        onTripSelected = onTripSelected,
                        checklistItems = checklistItems,
                        newItemTitle = newChecklistItemTitle,
                        onNewItemTitleChange =
                            onNewChecklistItemTitleChange,
                        onAddChecklistItem =
                            onAddChecklistItem,
                        onToggleChecklistItem =
                            onToggleChecklistItem,
                        onDeleteChecklistItem =
                            onDeleteChecklistItem,
                        modifier = Modifier.weight(1f)
                    )
                }

                DashboardTab.DOCUMENTS -> {
                    DocumentsTab(
                        trips = trips,
                        selectedTripId = selectedTripId,
                        onTripSelected = onTripSelected,
                        attachments = attachments,
                        onAttachmentsSelected =
                            onAttachmentsSelected,
                        onOpenAttachment =
                            onOpenAttachment,
                        onDeleteAttachment =
                            onDeleteAttachment,
                        modifier = Modifier.weight(1f)
                    )
                }

                DashboardTab.WEATHER -> {
                    WeatherTab(
                        trips = trips,
                        selectedTripId = selectedTripId,
                        onTripSelected = onTripSelected,
                        weatherState = weatherState,
                        onRetry = onRetryWeather,
                        modifier = Modifier.weight(1f)
                    )
                }
            }
        }
    }
}

private fun tabTitle(
    tab: DashboardTab
): String {
    return when (tab) {
        DashboardTab.OVERVIEW -> "Overview"
        DashboardTab.CHECKLIST -> "Checklist"
        DashboardTab.DOCUMENTS -> "Documents"
        DashboardTab.WEATHER -> "Weather"
    }
}

@Preview(
    showBackground = true,
    showSystemUi = true,
    widthDp = 400,
    heightDp = 800
)
@Composable
private fun DashboardScreenPreview() {
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
        DashboardScreen(
            trips = listOf(sampleTrip),
            selectedTripId = sampleTrip.id,
            selectedTab = DashboardTab.OVERVIEW,
            onTripSelected = {},
            onTabSelected = {},
            onAddTrip = {},
            onManageTrips = {},
            checklistItems = listOf(
                ChecklistUiModel(
                    id = 1L,
                    tripId = 1L,
                    title = "Pack passport",
                    isCompleted = false
                )
            ),
            newChecklistItemTitle = "",
            onNewChecklistItemTitleChange = {},
            onAddChecklistItem = {},
            onToggleChecklistItem = {},
            onDeleteChecklistItem = {},
            attachments = emptyList(),
            onAttachmentsSelected = {},
            onOpenAttachment = {},
            onDeleteAttachment = {},
            weatherState = WeatherUiState(),
            onRetryWeather = {}
        )
    }
}
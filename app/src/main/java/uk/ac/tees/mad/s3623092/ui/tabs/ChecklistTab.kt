package uk.ac.tees.mad.s3623092.ui.tabs

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding

import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.Checkbox
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import uk.ac.tees.mad.s3623092.ui.components.TripSelector
import uk.ac.tees.mad.s3623092.ui.model.ChecklistUiModel
import uk.ac.tees.mad.s3623092.ui.model.TripUiModel
import uk.ac.tees.mad.s3623092.ui.theme.TravelPackTheme

@Composable
fun ChecklistTab(
    trips: List<TripUiModel>,
    selectedTripId: Long?,
    onTripSelected: (TripUiModel) -> Unit,
    checklistItems: List<ChecklistUiModel>,
    newItemTitle: String,
    onNewItemTitleChange: (String) -> Unit,
    onAddChecklistItem: () -> Unit,
    onToggleChecklistItem: (ChecklistUiModel) -> Unit,
    onDeleteChecklistItem: (ChecklistUiModel) -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Text(
            text = "Checklist",
            style = MaterialTheme.typography.headlineSmall
        )

        TripSelector(
            trips = trips,
            selectedTripId = selectedTripId,
            onTripSelected = onTripSelected
        )

        if (selectedTripId == null) {
            Text(
                text = "Select a trip to view its checklist.",
                color = MaterialTheme.colorScheme
                    .onSurfaceVariant
            )
        } else {
            AddChecklistItemSection(
                title = newItemTitle,
                onTitleChange = onNewItemTitleChange,
                onAddItem = onAddChecklistItem
            )

            if (checklistItems.isEmpty()) {
                Text(
                    text = "No checklist items added yet.",
                    color = MaterialTheme.colorScheme
                        .onSurfaceVariant
                )
            } else {
                LazyColumn(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(
                        items = checklistItems,
                        key = { item ->
                            item.id
                        }
                    ) { item ->
                        ChecklistItemRow(
                            item = item,
                            onToggle = {
                                onToggleChecklistItem(item)
                            },
                            onDelete = {
                                onDeleteChecklistItem(item)
                            }
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun AddChecklistItemSection(
    title: String,
    onTitleChange: (String) -> Unit,
    onAddItem: () -> Unit
) {
    Column(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        OutlinedTextField(
            value = title,
            onValueChange = onTitleChange,
            label = {
                Text("New checklist item")
            },
            placeholder = {
                Text("For example, pack passport")
            },
            singleLine = true,
            modifier = Modifier.fillMaxWidth()
        )

        Button(
            onClick = onAddItem,
            enabled = title.trim().isNotEmpty(),
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Add checklist item")
        }
    }
}

@Composable
private fun ChecklistItemRow(
    item: ChecklistUiModel,
    onToggle: () -> Unit,
    onDelete: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Checkbox(
                checked = item.isCompleted,
                onCheckedChange = {
                    onToggle()
                }
            )

            Text(
                text = item.title,
                modifier = Modifier.weight(1f),
                textDecoration =
                    if (item.isCompleted) {
                        TextDecoration.LineThrough
                    } else {
                        TextDecoration.None
                    }
            )

            TextButton(
                onClick = onDelete
            ) {
                Text("Delete")
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
private fun ChecklistTabPreview() {
    val sampleTrip = TripUiModel(
        id = 1L,
        name = "Scotland Trip",
        fromLocation = "London",
        toLocation = "Edinburgh",
        startDate = "2026-12-10",
        endDate = "2026-12-15"
    )

    TravelPackTheme {
        ChecklistTab(
            trips = listOf(sampleTrip),
            selectedTripId = sampleTrip.id,
            onTripSelected = {},
            checklistItems = listOf(
                ChecklistUiModel(
                    id = 1L,
                    tripId = 1L,
                    title = "Pack passport",
                    isCompleted = false
                ),
                ChecklistUiModel(
                    id = 2L,
                    tripId = 1L,
                    title = "Book hotel",
                    isCompleted = true
                )
            ),
            newItemTitle = "",
            onNewItemTitleChange = {},
            onAddChecklistItem = {},
            onToggleChecklistItem = {},
            onDeleteChecklistItem = {}
        )
    }
}
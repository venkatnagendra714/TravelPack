package uk.ac.tees.mad.s3623092.ui.screens

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.google.android.libraries.places.api.net.PlacesClient
import uk.ac.tees.mad.s3623092.ui.components.DateInputField
import uk.ac.tees.mad.s3623092.ui.components.LocationAutocompleteField
import uk.ac.tees.mad.s3623092.ui.model.AttachmentUiModel
import uk.ac.tees.mad.s3623092.ui.theme.TravelPackTheme
import uk.ac.tees.mad.s3623092.util.TripDateUtils

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddTripScreen(
    placesClient: PlacesClient?,
    tripName: String,
    fromLocation: String,
    toLocation: String,
    startDate: String,
    endDate: String,
    notes: String,
    attachments: List<AttachmentUiModel>,
    checklistItems: List<String>,
    newChecklistItem: String,
    isSaving: Boolean,
    errorMessage: String?,
    onTripNameChange: (String) -> Unit,
    onFromLocationChange: (String) -> Unit,
    onToLocationChange: (String) -> Unit,
    onStartDateChange: (String) -> Unit,
    onEndDateChange: (String) -> Unit,
    onNotesChange: (String) -> Unit,
    onNewChecklistItemChange: (String) -> Unit,
    onAddChecklistItem: () -> Unit,
    onRemoveChecklistItem: (String) -> Unit,
    onUseCurrentLocationForFrom: () -> Unit,
    onUseCurrentLocationForTo: () -> Unit,
    onCapturePhoto: () -> Unit,
    onDocumentsSelected: (List<Uri>) -> Unit,
    onRemoveAttachment: (AttachmentUiModel) -> Unit,
    onSaveTrip: () -> Unit,
    onCancel: () -> Unit,
    modifier: Modifier = Modifier
) {
    val documentPicker =
        rememberLauncherForActivityResult(
            contract = ActivityResultContracts
                .OpenMultipleDocuments()
        ) { selectedUris ->
            if (selectedUris.isNotEmpty()) {
                onDocumentsSelected(selectedUris)
            }
        }

    val validStartDate =
        TripDateUtils.isValidUserDate(startDate)

    val validEndDate =
        TripDateUtils.isValidUserDate(endDate)

    val validDateRange =
        if (validStartDate && validEndDate) {
            TripDateUtils.isValidDateRange(
                startDate,
                endDate
            )
        } else {
            false
        }

    val canSave =
        tripName.trim().isNotEmpty() &&
                fromLocation.trim().isNotEmpty() &&
                toLocation.trim().isNotEmpty() &&
                validStartDate &&
                validEndDate &&
                validDateRange &&
                !isSaving

    Scaffold(
        modifier = modifier,
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "Add trip"
                    )
                },
                navigationIcon = {
                    TextButton(
                        onClick = onCancel
                    ) {
                        Text(
                            text = "Back"
                        )
                    }
                }
            )
        }
    ) { innerPadding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxWidth()
                .padding(
                    top = innerPadding.calculateTopPadding(),
                    bottom = innerPadding.calculateBottomPadding(),
                    start = 16.dp,
                    end = 16.dp
                ),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            item {
                Text(
                    text = "Trip details",
                    fontWeight = FontWeight.Bold
                )
            }

            item {
                OutlinedTextField(
                    value = tripName,
                    onValueChange = onTripNameChange,
                    label = {
                        Text("Trip name")
                    },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
            }

            item {
                LocationInput(
                    label = "From location",
                    value = fromLocation,
                    placesClient = placesClient,
                    onValueChange = onFromLocationChange
                )
            }

            item {
                OutlinedButton(
                    onClick = onUseCurrentLocationForFrom,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        text = "Use current location for starting point"
                    )
                }
            }

            item {
                LocationInput(
                    label = "To location",
                    value = toLocation,
                    placesClient = placesClient,
                    onValueChange = onToLocationChange
                )
            }

            item {
                OutlinedButton(
                    onClick = onUseCurrentLocationForTo,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        text = "Use current location for destination"
                    )
                }
            }

            item {
                DateInputField(
                    label = "Start date",
                    value = startDate,
                    onValueChange = onStartDateChange
                )
            }

            item {
                DateInputField(
                    label = "End date",
                    value = endDate,
                    onValueChange = onEndDateChange
                )
            }

            if (
                validStartDate &&
                validEndDate &&
                !validDateRange
            ) {
                item {
                    Text(
                        text = "The end date must be on or after the start date.",
                        color = MaterialTheme.colorScheme.error
                    )
                }
            }

            item {
                OutlinedTextField(
                    value = notes,
                    onValueChange = onNotesChange,
                    label = {
                        Text("Notes")
                    },
                    minLines = 3,
                    modifier = Modifier.fillMaxWidth()
                )
            }

            item {
                Text(
                    text = "Photos and documents",
                    fontWeight = FontWeight.Bold
                )
            }

            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    OutlinedButton(
                        onClick = onCapturePhoto,
                        modifier = Modifier.weight(1f)
                    ) {
                        Text("Capture photo")
                    }

                    OutlinedButton(
                        onClick = {
                            documentPicker.launch(
                                arrayOf(
                                    "image/*",
                                    "application/pdf"
                                )
                            )
                        },
                        modifier = Modifier.weight(1f)
                    ) {
                        Text("Add files")
                    }
                }
            }

            items(
                items = attachments,
                key = { attachment ->
                    attachment.id.toString() + attachment.uri
                }
            ) { attachment ->
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = attachment.fileName,
                        modifier = Modifier.weight(1f)
                    )

                    TextButton(
                        onClick = {
                            onRemoveAttachment(attachment)
                        }
                    ) {
                        Text("Remove")
                    }
                }
            }

            item {
                Text(
                    text = "Checklist",
                    fontWeight = FontWeight.Bold
                )
            }

            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    OutlinedTextField(
                        value = newChecklistItem,
                        onValueChange =
                            onNewChecklistItemChange,
                        label = {
                            Text("Checklist item")
                        },
                        singleLine = true,
                        modifier = Modifier.weight(1f)
                    )

                    Button(
                        onClick = onAddChecklistItem,
                        enabled = newChecklistItem
                            .trim()
                            .isNotEmpty()
                    ) {
                        Text("Add")
                    }
                }
            }

            items(
                items = checklistItems,
                key = { item ->
                    item
                }
            ) { item ->
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = item,
                        modifier = Modifier.weight(1f)
                    )

                    TextButton(
                        onClick = {
                            onRemoveChecklistItem(item)
                        }
                    ) {
                        Text("Remove")
                    }
                }
            }

            if (!errorMessage.isNullOrBlank()) {
                item {
                    Text(
                        text = errorMessage,
                        color = MaterialTheme.colorScheme.error
                    )
                }
            }

            item {
                Button(
                    onClick = onSaveTrip,
                    enabled = canSave,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        text = if (isSaving) {
                            "Saving..."
                        } else {
                            "Save trip"
                        }
                    )
                }
            }
        }
    }
}

@Composable
private fun LocationInput(
    label: String,
    value: String,
    placesClient: PlacesClient?,
    onValueChange: (String) -> Unit
) {
    if (placesClient == null) {
        OutlinedTextField(
            value = value,
            onValueChange = onValueChange,
            label = {
                Text(label)
            },
            singleLine = true,
            modifier = Modifier.fillMaxWidth()
        )
    } else {
        LocationAutocompleteField(
            label = label,
            value = value,
            placesClient = placesClient,
            onValueChange = onValueChange,
            modifier = Modifier.fillMaxWidth()
        )
    }
}

@Preview(
    showBackground = true,
    showSystemUi = true,
    widthDp = 400,
    heightDp = 800
)
@Composable
private fun AddTripScreenPreview() {
    TravelPackTheme {
        AddTripScreen(
            placesClient = null,
            tripName = "Scotland Trip",
            fromLocation = "London",
            toLocation = "Edinburgh",
            startDate = "10/12/2026",
            endDate = "15/12/2026",
            notes = "Winter holiday",
            attachments = emptyList(),
            checklistItems = listOf(
                "Pack passport",
                "Book hotel"
            ),
            newChecklistItem = "",
            isSaving = false,
            errorMessage = null,
            onTripNameChange = {},
            onFromLocationChange = {},
            onToLocationChange = {},
            onStartDateChange = {},
            onEndDateChange = {},
            onNotesChange = {},
            onNewChecklistItemChange = {},
            onAddChecklistItem = {},
            onRemoveChecklistItem = {},
            onUseCurrentLocationForFrom = {},
            onUseCurrentLocationForTo = {},
            onCapturePhoto = {},
            onDocumentsSelected = {},
            onRemoveAttachment = {},
            onSaveTrip = {},
            onCancel = {}
        )
    }
}
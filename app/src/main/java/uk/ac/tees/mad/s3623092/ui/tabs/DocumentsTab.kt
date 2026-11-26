package uk.ac.tees.mad.s3623092.ui.tabs

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding

import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import uk.ac.tees.mad.s3623092.ui.components.AttachmentPreview
import uk.ac.tees.mad.s3623092.ui.components.TripSelector
import uk.ac.tees.mad.s3623092.ui.model.AttachmentUiModel
import uk.ac.tees.mad.s3623092.ui.model.TripUiModel
import uk.ac.tees.mad.s3623092.ui.theme.TravelPackTheme

@Composable
fun DocumentsTab(
    trips: List<TripUiModel>,
    selectedTripId: Long?,
    onTripSelected: (TripUiModel) -> Unit,
    attachments: List<AttachmentUiModel>,
    onAttachmentsSelected: (List<Uri>) -> Unit,
    onOpenAttachment: (AttachmentUiModel) -> Unit,
    onDeleteAttachment: (AttachmentUiModel) -> Unit,
    modifier: Modifier = Modifier
) {
    val documentPicker =
        rememberLauncherForActivityResult(
            contract = ActivityResultContracts
                .OpenMultipleDocuments()
        ) { selectedUris ->
            if (selectedUris.isNotEmpty()) {
                onAttachmentsSelected(selectedUris)
            }
        }

    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Text(
            text = "Documents",
            style = MaterialTheme.typography.headlineSmall
        )

        TripSelector(
            trips = trips,
            selectedTripId = selectedTripId,
            onTripSelected = onTripSelected
        )

        if (selectedTripId == null) {
            Text(
                text = "Select a trip to view its documents.",
                color = MaterialTheme.colorScheme
                    .onSurfaceVariant
            )
        } else {
            Button(
                onClick = {
                    documentPicker.launch(
                        arrayOf(
                            "image/*",
                            "application/pdf"
                        )
                    )
                },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Add documents or images")
            }

            if (attachments.isEmpty()) {
                Text(
                    text = "No documents or images added yet.",
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
                        items = attachments,
                        key = { attachment ->
                            attachment.id.toString() +
                                    attachment.uri
                        }
                    ) { attachment ->
                        AttachmentPreview(
                            attachment = attachment,
                            onOpen = {
                                onOpenAttachment(attachment)
                            },
                            onDelete = {
                                onDeleteAttachment(attachment)
                            }
                        )
                    }
                }
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
private fun DocumentsTabPreview() {
    val sampleTrip = TripUiModel(
        id = 1L,
        name = "Scotland Trip",
        fromLocation = "London",
        toLocation = "Edinburgh",
        startDate = "2026-12-10",
        endDate = "2026-12-15"
    )

    val sampleImage = AttachmentUiModel(
        id = 1L,
        uri = "content://sample/image",
        fileName = "hotel-photo.jpg",
        mimeType = "image/jpeg"
    )

    val samplePdf = AttachmentUiModel(
        id = 2L,
        uri = "content://sample/document",
        fileName = "booking-confirmation.pdf",
        mimeType = "application/pdf"
    )

    TravelPackTheme {
        DocumentsTab(
            trips = listOf(sampleTrip),
            selectedTripId = sampleTrip.id,
            onTripSelected = {},
            attachments = listOf(
                sampleImage,
                samplePdf
            ),
            onAttachmentsSelected = {},
            onOpenAttachment = {},
            onDeleteAttachment = {}
        )
    }
}
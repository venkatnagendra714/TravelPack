package uk.ac.tees.mad.s3623092.ui.components

import android.graphics.BitmapFactory
import android.net.Uri
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.produceState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import uk.ac.tees.mad.s3623092.ui.model.AttachmentUiModel
import uk.ac.tees.mad.s3623092.ui.theme.TravelPackTheme

@Composable
fun AttachmentPreview(
    attachment: AttachmentUiModel,
    onOpen: () -> Unit,
    onDelete: (() -> Unit)? = null,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current

    val imageBitmap by produceState<ImageBitmap?>(
        initialValue = null,
        key1 = attachment.uri
    ) {
        if (attachment.isImage) {
            value = withContext(Dispatchers.IO) {
                try {
                    context.contentResolver
                        .openInputStream(
                            Uri.parse(attachment.uri)
                        )
                        ?.use { inputStream ->
                            BitmapFactory
                                .decodeStream(inputStream)
                                ?.asImageBitmap()
                        }
                } catch (_: Exception) {
                    null
                }
            }
        }
    }

    Card(
        onClick = onOpen,
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors()
    ) {
        if (attachment.isImage) {
            ImageAttachmentContent(
                attachment = attachment,
                imageBitmap = imageBitmap
            )
        } else {
            DocumentAttachmentContent(
                attachment = attachment,
                onOpen = onOpen
            )
        }

        if (onDelete != null) {
            TextButton(
                onClick = onDelete,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Remove")
            }
        }
    }
}

@Composable
private fun ImageAttachmentContent(
    attachment: AttachmentUiModel,
    imageBitmap: ImageBitmap?
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(12.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        if (imageBitmap != null) {
            Image(
                bitmap = imageBitmap,
                contentDescription = attachment.fileName,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(180.dp)
                    .clip(
                        RoundedCornerShape(8.dp)
                    ),
                contentScale = ContentScale.Crop
            )
        } else {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(180.dp)
                    .clip(
                        RoundedCornerShape(8.dp)
                    )
                    .background(
                        MaterialTheme.colorScheme.surfaceVariant
                    ),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "Image preview unavailable"
                )
            }
        }

        Text(
            text = attachment.fileName,
            maxLines = 1
        )
    }
}

@Composable
private fun DocumentAttachmentContent(
    attachment: AttachmentUiModel,
    onOpen: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Text(
            text = if (attachment.isPdf) {
                "PDF document"
            } else {
                "Document"
            },
            style = MaterialTheme.typography.titleMedium
        )

        Text(
            text = attachment.fileName,
            maxLines = 2
        )

        TextButton(
            onClick = onOpen
        ) {
            Text("Open document")
        }
    }
}

@Preview(
    showBackground = true,
    widthDp = 380
)
@Composable
private fun AttachmentPreviewPreview() {
    val imageAttachment = AttachmentUiModel(
        id = 1L,
        uri = "content://preview/image",
        fileName = "hotel-photo.jpg",
        mimeType = "image/jpeg"
    )

    val pdfAttachment = AttachmentUiModel(
        id = 2L,
        uri = "content://preview/pdf",
        fileName = "booking-confirmation.pdf",
        mimeType = "application/pdf"
    )

    TravelPackTheme {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            AttachmentPreview(
                attachment = imageAttachment,
                onOpen = {},
                onDelete = {}
            )

            AttachmentPreview(
                attachment = pdfAttachment,
                onOpen = {},
                onDelete = {}
            )
        }
    }
}
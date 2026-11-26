package uk.ac.tees.mad.s3623092.util

import android.content.ActivityNotFoundException
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.widget.Toast
import androidx.core.content.FileProvider
import java.io.File

object DocumentOpener {

    fun openAttachment(
        context: Context,
        uriString: String,
        mimeType: String
    ): Boolean {
        return try {
            val originalUri = Uri.parse(uriString)

            val openableUri = convertToOpenableUri(
                context = context,
                uri = originalUri
            )

            val type = if (
                mimeType.isBlank() ||
                mimeType == "application/octet-stream"
            ) {
                TripFileUtils.getMimeType(
                    context,
                    originalUri
                )
            } else {
                mimeType
            }

            val intent = Intent(
                Intent.ACTION_VIEW
            ).apply {
                setDataAndType(
                    openableUri,
                    type
                )
                addFlags(
                    Intent.FLAG_GRANT_READ_URI_PERMISSION
                )
                addFlags(
                    Intent.FLAG_ACTIVITY_NEW_TASK
                )
            }

            context.startActivity(
                Intent.createChooser(
                    intent,
                    "Open attachment"
                )
            )

            true
        } catch (_: ActivityNotFoundException) {
            showMessage(
                context,
                "No application can open this file."
            )
            false
        } catch (_: Exception) {
            showMessage(
                context,
                "Unable to open this attachment."
            )
            false
        }
    }

    fun openPdf(
        context: Context,
        uriString: String
    ): Boolean {
        return openAttachment(
            context = context,
            uriString = uriString,
            mimeType = "application/pdf"
        )
    }

    fun openImage(
        context: Context,
        uriString: String
    ): Boolean {
        return openAttachment(
            context = context,
            uriString = uriString,
            mimeType = "image/*"
        )
    }

    private fun convertToOpenableUri(
        context: Context,
        uri: Uri
    ): Uri {
        if (uri.scheme != "file") {
            return uri
        }

        val filePath = uri.path
            ?: return uri

        return FileProvider.getUriForFile(
            context,
            "${context.packageName}.fileprovider",
            File(filePath)
        )
    }

    private fun showMessage(
        context: Context,
        message: String
    ) {
        Toast.makeText(
            context,
            message,
            Toast.LENGTH_SHORT
        ).show()
    }
}
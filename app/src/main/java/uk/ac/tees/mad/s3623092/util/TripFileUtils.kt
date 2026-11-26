package uk.ac.tees.mad.s3623092.util

import android.content.Context
import android.database.Cursor
import android.net.Uri
import android.provider.OpenableColumns
import android.webkit.MimeTypeMap
import androidx.core.content.FileProvider
import java.io.File
import java.io.IOException

object TripFileUtils {

    private const val PHOTO_DIRECTORY = "trip_photos"
    private const val DOCUMENT_DIRECTORY = "trip_documents"

    fun createCameraImageFile(context: Context): File {
        val directory = File(
            context.filesDir,
            PHOTO_DIRECTORY
        )

        if (!directory.exists()) {
            directory.mkdirs()
        }

        return File.createTempFile(
            "trip_photo_",
            ".jpg",
            directory
        )
    }

    fun getFileProviderUri(
        context: Context,
        file: File
    ): Uri {
        return FileProvider.getUriForFile(
            context,
            "${context.packageName}.fileprovider",
            file
        )
    }

    fun copyUriToPrivateStorage(
        context: Context,
        sourceUri: Uri
    ): File? {
        return try {
            val resolver = context.contentResolver
            val fileName = getFileName(
                context,
                sourceUri
            )

            val mimeType = getMimeType(
                context,
                sourceUri
            )

            val extension = getFileExtension(
                fileName,
                mimeType
            )

            val directory = File(
                context.filesDir,
                DOCUMENT_DIRECTORY
            )

            if (!directory.exists()) {
                directory.mkdirs()
            }

            val safeName = sanitiseFileName(
                fileName.substringBeforeLast(
                    ".",
                    fileName
                )
            )

            val destination = File(
                directory,
                "${safeName}_${System.currentTimeMillis()}$extension"
            )

            val inputStream = resolver.openInputStream(
                sourceUri
            ) ?: return null

            inputStream.use { input ->
                destination.outputStream().use { output ->
                    input.copyTo(output)
                }
            }

            destination
        } catch (_: Exception) {
            null
        }
    }

    fun getFileName(
        context: Context,
        uri: Uri
    ): String {
        var fileName: String? = null

        if (uri.scheme == "content") {
            val projection = arrayOf(
                OpenableColumns.DISPLAY_NAME
            )

            context.contentResolver.query(
                uri,
                projection,
                null,
                null,
                null
            )?.use { cursor: Cursor ->
                val nameIndex = cursor.getColumnIndex(
                    OpenableColumns.DISPLAY_NAME
                )

                if (
                    nameIndex >= 0 &&
                    cursor.moveToFirst()
                ) {
                    fileName = cursor.getString(
                        nameIndex
                    )
                }
            }
        }

        return fileName
            ?: uri.lastPathSegment
            ?: "attachment"
    }

    fun getMimeType(
        context: Context,
        uri: Uri
    ): String {
        val resolverType = context.contentResolver
            .getType(uri)

        if (!resolverType.isNullOrBlank()) {
            return resolverType
        }

        val extension = MimeTypeMap
            .getFileExtensionFromUrl(
                uri.toString()
            )
            .lowercase()

        return MimeTypeMap
            .getSingleton()
            .getMimeTypeFromExtension(extension)
            ?: "application/octet-stream"
    }

    fun getFileExtension(
        fileName: String,
        mimeType: String
    ): String {
        val existingExtension = fileName
            .substringAfterLast(
                ".",
                ""
            )
            .trim()

        if (existingExtension.isNotBlank()) {
            return ".$existingExtension"
        }

        val mimeExtension = MimeTypeMap
            .getSingleton()
            .getExtensionFromMimeType(mimeType)

        return if (
            mimeExtension.isNullOrBlank()
        ) {
            ""
        } else {
            ".$mimeExtension"
        }
    }

    fun sanitiseFileName(
        fileName: String
    ): String {
        val cleanedName = fileName
            .replace(
                Regex("[^A-Za-z0-9_-]"),
                "_"
            )
            .trim('_')

        return if (
            cleanedName.isBlank()
        ) {
            "attachment"
        } else {
            cleanedName
        }
    }

    fun isImage(
        context: Context,
        uri: Uri
    ): Boolean {
        return getMimeType(
            context,
            uri
        ).startsWith("image/")
    }

    fun isPdf(
        context: Context,
        uri: Uri
    ): Boolean {
        return getMimeType(
            context,
            uri
        ) == "application/pdf"
    }

    fun deleteFile(
        file: File?
    ): Boolean {
        return file?.takeIf { it.exists() }?.delete()
            ?: false
    }

    fun deleteFileFromUri(
        uri: Uri
    ): Boolean {
        return try {
            if (uri.scheme == "file") {
                File(
                    uri.path ?: return false
                ).delete()
            } else {
                false
            }
        } catch (_: IOException) {
            false
        } catch (_: SecurityException) {
            false
        }
    }
}
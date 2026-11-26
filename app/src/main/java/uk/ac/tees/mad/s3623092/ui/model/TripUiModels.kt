package uk.ac.tees.mad.s3623092.ui.model

data class TripUiModel(
    val id: Long = 0L,
    val name: String = "",
    val fromLocation: String = "",
    val toLocation: String = "",
    val startDate: String = "",
    val endDate: String = "",
    val notes: String = "",
    val photoUri: String? = null,
    val attachments: List<AttachmentUiModel> = emptyList()
)

data class AttachmentUiModel(
    val id: Long = 0L,
    val uri: String,
    val fileName: String,
    val mimeType: String
) {
    val isImage: Boolean
        get() = mimeType.startsWith("image/")

    val isPdf: Boolean
        get() = mimeType == "application/pdf"
}

data class ChecklistUiModel(
    val id: Long = 0L,
    val tripId: Long = 0L,
    val title: String = "",
    val isCompleted: Boolean = false
)

data class TripFormState(
    val tripId: Long? = null,
    val name: String = "",
    val fromLocation: String = "",
    val toLocation: String = "",
    val startDate: String = "",
    val endDate: String = "",
    val notes: String = "",
    val attachments: List<AttachmentUiModel> = emptyList(),
    val checklistItems: List<String> = emptyList(),
    val isSaving: Boolean = false,
    val errorMessage: String? = null
)

enum class TripListTab {
    UPCOMING,
    COMPLETED
}

enum class TripSortOption {
    START_DATE,
    END_DATE,
    NAME,
    FROM_LOCATION,
    TO_LOCATION
}
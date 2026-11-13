package uk.ac.tees.mad.s3623092.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.emptyFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import uk.ac.tees.mad.s3623092.data.ChecklistItemEntity
import uk.ac.tees.mad.s3623092.data.TravelPackDatabase
import uk.ac.tees.mad.s3623092.data.TripAttachmentEntity
import uk.ac.tees.mad.s3623092.data.TripEntity
import uk.ac.tees.mad.s3623092.data.TripRepository

class TripViewModel(
    application: Application
) : AndroidViewModel(application) {

    private val database =
        TravelPackDatabase.getDatabase(application)

    private val repository =
        TripRepository(
            tripDao = database.tripDao(),
            tripAttachmentDao = database.tripAttachmentDao(),
            checklistItemDao = database.checklistItemDao()
        )

    val trips: StateFlow<List<TripEntity>> =
        repository.allTrips.stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5_000),
            initialValue = emptyList()
        )

    fun saveTrip(trip: TripEntity) {
        viewModelScope.launch {
            repository.insertTrip(trip)
        }
    }

    fun saveTripWithAttachments(
        trip: TripEntity,
        attachments: List<TripAttachmentEntity>
    ) {
        viewModelScope.launch {
            val tripId = repository.insertTrip(trip)

            repository.insertAttachments(
                attachments.map { attachment ->
                    attachment.copy(tripId = tripId)
                }
            )
        }
    }

    fun saveTripWithAttachmentsAndChecklist(
        trip: TripEntity,
        attachments: List<TripAttachmentEntity>,
        checklistItems: List<ChecklistItemEntity>
    ) {
        viewModelScope.launch {
            val tripId = repository.insertTrip(trip)

            repository.insertAttachments(
                attachments.map { attachment ->
                    attachment.copy(tripId = tripId)
                }
            )

            checklistItems
                .filter { it.title.isNotBlank() }
                .forEach { item ->
                    repository.insertChecklistItem(
                        item.copy(tripId = tripId)
                    )
                }
        }
    }

    fun attachmentsForTrip(
        tripId: Long
    ): Flow<List<TripAttachmentEntity>> {
        return if (tripId == 0L) {
            emptyFlow()
        } else {
            repository.getAttachmentsForTrip(tripId)
        }
    }

    fun addAttachments(
        tripId: Long,
        attachments: List<TripAttachmentEntity>
    ) {
        viewModelScope.launch {
            repository.insertAttachments(
                attachments.map { attachment ->
                    attachment.copy(tripId = tripId)
                }
            )
        }
    }

    fun checklistForTrip(
        tripId: Long
    ): Flow<List<ChecklistItemEntity>> {
        return if (tripId == 0L) {
            emptyFlow()
        } else {
            repository.getChecklistForTrip(tripId)
        }
    }

    fun addChecklistItem(
        tripId: Long,
        title: String
    ) {
        val cleanTitle = title.trim()

        if (tripId == 0L || cleanTitle.isBlank()) {
            return
        }

        viewModelScope.launch {
            repository.insertChecklistItem(
                ChecklistItemEntity(
                    tripId = tripId,
                    title = cleanTitle
                )
            )
        }
    }

    fun updateChecklistItem(
        item: ChecklistItemEntity
    ) {
        viewModelScope.launch {
            repository.updateChecklistItem(item)
        }
    }

    fun deleteChecklistItem(
        item: ChecklistItemEntity
    ) {
        viewModelScope.launch {
            repository.deleteChecklistItem(item)
        }
    }

    fun updateTrip(trip: TripEntity) {
        viewModelScope.launch {
            repository.updateTrip(trip)
        }
    }

    fun deleteTrip(trip: TripEntity) {
        viewModelScope.launch {
            repository.deleteTrip(trip)
        }
    }
}
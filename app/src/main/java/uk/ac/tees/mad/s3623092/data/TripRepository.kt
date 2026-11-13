package uk.ac.tees.mad.s3623092.data

import kotlinx.coroutines.flow.Flow

class TripRepository(
    private val tripDao: TripDao,
    private val tripAttachmentDao: TripAttachmentDao,
    private val checklistItemDao: ChecklistItemDao
) {
    val allTrips: Flow<List<TripEntity>> =
        tripDao.getAllTrips()

    suspend fun getTripById(tripId: Long): TripEntity? {
        return tripDao.getTripById(tripId)
    }

    suspend fun insertTrip(trip: TripEntity): Long {
        return tripDao.insertTrip(trip)
    }

    suspend fun updateTrip(trip: TripEntity) {
        tripDao.updateTrip(trip)
    }

    suspend fun deleteTrip(trip: TripEntity) {
        tripDao.deleteTrip(trip)
    }

    fun getAttachmentsForTrip(
        tripId: Long
    ): Flow<List<TripAttachmentEntity>> {
        return tripAttachmentDao.getAttachmentsForTrip(tripId)
    }

    suspend fun insertAttachments(
        attachments: List<TripAttachmentEntity>
    ) {
        if (attachments.isNotEmpty()) {
            tripAttachmentDao.insertAttachments(attachments)
        }
    }

    fun getChecklistForTrip(
        tripId: Long
    ): Flow<List<ChecklistItemEntity>> {
        return checklistItemDao.getChecklistForTrip(tripId)
    }

    suspend fun insertChecklistItem(
        item: ChecklistItemEntity
    ): Long {
        return checklistItemDao.insertChecklistItem(item)
    }

    suspend fun updateChecklistItem(
        item: ChecklistItemEntity
    ) {
        checklistItemDao.updateChecklistItem(item)
    }

    suspend fun deleteChecklistItem(
        item: ChecklistItemEntity
    ) {
        checklistItemDao.deleteChecklistItem(item)
    }
}

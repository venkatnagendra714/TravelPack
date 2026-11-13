package uk.ac.tees.mad.s3623092.data

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface TripAttachmentDao {

    @Query(
        "SELECT * FROM trip_attachments " +
                "WHERE tripId = :tripId ORDER BY id ASC"
    )
    fun getAttachmentsForTrip(
        tripId: Long
    ): Flow<List<TripAttachmentEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAttachments(
        attachments: List<TripAttachmentEntity>
    )
}

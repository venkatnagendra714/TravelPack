package uk.ac.tees.mad.s3623092.data

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

@Dao
interface ChecklistItemDao {

    @Query(
        "SELECT * FROM checklist_items " +
                "WHERE tripId = :tripId ORDER BY id ASC"
    )
    fun getChecklistForTrip(
        tripId: Long
    ): Flow<List<ChecklistItemEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertChecklistItem(
        item: ChecklistItemEntity
    ): Long

    @Update
    suspend fun updateChecklistItem(
        item: ChecklistItemEntity
    )

    @Delete
    suspend fun deleteChecklistItem(
        item: ChecklistItemEntity
    )
}

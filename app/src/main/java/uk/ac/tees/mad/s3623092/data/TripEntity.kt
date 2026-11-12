package uk.ac.tees.mad.s3623092.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "trips")
data class TripEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0L,

    val name: String,

    val fromLocation: String,

    val toLocation: String,

    val startDate: String,

    val endDate: String,

    val notes: String
)
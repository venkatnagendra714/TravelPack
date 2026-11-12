package uk.ac.tees.mad.s3623092.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import uk.ac.tees.mad.s3623092.data.TravelPackDatabase
import uk.ac.tees.mad.s3623092.data.TripEntity
import uk.ac.tees.mad.s3623092.data.TripRepository

class TripViewModel(
    application: Application
) : AndroidViewModel(application) {

    private val database =
        TravelPackDatabase.getDatabase(application)

    private val repository =
        TripRepository(database.tripDao())

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
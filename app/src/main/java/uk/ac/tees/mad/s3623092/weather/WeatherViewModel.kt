package uk.ac.tees.mad.s3623092.weather

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope

import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class WeatherViewModel : ViewModel() {

    private val repository =
        WeatherRepository()

    private val _uiState =
        MutableStateFlow(
            WeatherUiState()
        )

    val uiState: StateFlow<WeatherUiState> =
        _uiState.asStateFlow()

    private var weatherJob: Job? = null

    private var lastDestination = ""

    fun loadWeather(
        destination: String
    ) {
        val cleanDestination =
            destination.trim()

        if (cleanDestination.isBlank()) {
            clearWeather()
            return
        }

        val currentState =
            _uiState.value

        if (
            cleanDestination.equals(
                lastDestination,
                ignoreCase = true
            ) &&
            currentState.weather != null &&
            !currentState.isLoading &&
            currentState.errorMessage == null
        ) {
            return
        }

        lastDestination = cleanDestination

        weatherJob?.cancel()

        weatherJob = viewModelScope.launch {
            _uiState.value = WeatherUiState(
                destination = cleanDestination,
                isLoading = true
            )

            try {
                val weather =
                    repository.getWeather(
                        cleanDestination
                    )

                _uiState.value = WeatherUiState(
                    destination = cleanDestination,
                    isLoading = false,
                    weather = weather
                )
            } catch (exception: CancellationException) {
                throw exception
            } catch (_: Exception) {
                _uiState.value = WeatherUiState(
                    destination = cleanDestination,
                    isLoading = false,
                    errorMessage =
                        "Unable to load weather information."
                )
            }
        }
    }

    fun retry() {
        val destination =
            lastDestination

        if (destination.isBlank()) {
            return
        }

        lastDestination = ""

        loadWeather(destination)
    }

    fun clearWeather() {
        weatherJob?.cancel()
        weatherJob = null
        lastDestination = ""

        _uiState.value = WeatherUiState()
    }

    override fun onCleared() {
        weatherJob?.cancel()
        super.onCleared()
    }
}
package uk.ac.tees.mad.s3623092.ui.tabs

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import uk.ac.tees.mad.s3623092.ui.components.TripSelector
import uk.ac.tees.mad.s3623092.ui.model.TripUiModel
import uk.ac.tees.mad.s3623092.ui.theme.TravelPackTheme
import uk.ac.tees.mad.s3623092.weather.DailyWeather
import uk.ac.tees.mad.s3623092.weather.WeatherData
import uk.ac.tees.mad.s3623092.weather.WeatherUiState
import java.util.Locale

@Composable
fun WeatherTab(
    trips: List<TripUiModel>,
    selectedTripId: Long?,
    onTripSelected: (TripUiModel) -> Unit,
    weatherState: WeatherUiState,
    onRetry: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(
                rememberScrollState()
            )
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Text(
            text = "Destination weather",
            style = MaterialTheme.typography.headlineSmall,
            fontWeight = FontWeight.Bold
        )

        TripSelector(
            trips = trips,
            selectedTripId = selectedTripId,
            onTripSelected = onTripSelected
        )

        if (selectedTripId == null) {
            Text(
                text = "Select a trip to view its weather."
            )
        } else {
            when {
                weatherState.isLoading -> {
                    Column(
                        verticalArrangement =
                            Arrangement.spacedBy(12.dp)
                    ) {
                        CircularProgressIndicator()

                        Text(
                            text = "Loading weather information..."
                        )
                    }
                }

                weatherState.errorMessage != null -> {
                    Column(
                        verticalArrangement =
                            Arrangement.spacedBy(12.dp)
                    ) {
                        Text(
                            text = weatherState.errorMessage,
                            color = MaterialTheme.colorScheme.error
                        )

                        Button(
                            onClick = onRetry
                        ) {
                            Text("Retry")
                        }
                    }
                }

                weatherState.weather != null -> {
                    WeatherContent(
                        weather = weatherState.weather
                    )
                }

                else -> {
                    Text(
                        text = "Weather information is not available."
                    )
                }
            }
        }
    }
}

@Composable
private fun WeatherContent(
    weather: WeatherData
) {
    Column(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        CurrentWeatherCard(weather)

        Text(
            text = "Five-day forecast",
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.Bold
        )

        weather.dailyForecast.forEach { forecast ->
            ForecastCard(forecast)
        }
    }
}

@Composable
private fun CurrentWeatherCard(
    weather: WeatherData
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme
                .secondaryContainer
        )
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Text(
                text = weather.destination,
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold
            )

            Text(
                text = formatTemperature(
                    weather.currentTemperature
                ),
                style = MaterialTheme.typography.displaySmall,
                fontWeight = FontWeight.Bold
            )

            Text(
                text = weatherDescription(
                    weather.currentWeatherCode
                )
            )

            Text(
                text = "Feels like: ${
                    formatTemperature(
                        weather.apparentTemperature
                    )
                }"
            )

            Text(
                text = "Wind speed: ${
                    formatNumber(
                        weather.currentWindSpeed
                    )
                } km/h"
            )

            Text(
                text = "Precipitation: ${
                    formatNumber(
                        weather.currentPrecipitation
                    )
                } mm"
            )
        }
    }
}

@Composable
private fun ForecastCard(
    forecast: DailyWeather
) {
    Card(
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            Text(
                text = forecast.date,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )

            Text(
                text = weatherDescription(
                    forecast.weatherCode
                )
            )

            Text(
                text = "Temperature: ${
                    formatTemperature(
                        forecast.minimumTemperature
                    )
                } to ${
                    formatTemperature(
                        forecast.maximumTemperature
                    )
                }"
            )

            Text(
                text = "Rain probability: ${
                    formatNumber(
                        forecast.precipitationProbability
                    )
                }%"
            )
        }
    }
}

private fun formatTemperature(
    value: Double
): String {
    return String.format(
        Locale.UK,
        "%.1f °C",
        value
    )
}

private fun formatNumber(
    value: Double
): String {
    return String.format(
        Locale.UK,
        "%.1f",
        value
    )
}

private fun weatherDescription(
    weatherCode: Int
): String {
    return when (weatherCode) {
        0 -> "Clear sky"
        1 -> "Mainly clear"
        2 -> "Partly cloudy"
        3 -> "Overcast"
        45, 48 -> "Fog"
        51, 53, 55 -> "Drizzle"
        56, 57 -> "Freezing drizzle"
        61, 63, 65 -> "Rain"
        66, 67 -> "Freezing rain"
        71, 73, 75, 77 -> "Snow"
        80, 81, 82 -> "Rain showers"
        85, 86 -> "Snow showers"
        95 -> "Thunderstorm"
        96, 99 -> "Thunderstorm with hail"
        else -> "Weather information unavailable"
    }
}

@Preview(
    showBackground = true,
    showSystemUi = true,
    widthDp = 400,
    heightDp = 800
)
@Composable
private fun WeatherTabPreview() {
    val sampleTrip = TripUiModel(
        id = 1L,
        name = "Scotland Trip",
        fromLocation = "London",
        toLocation = "Edinburgh",
        startDate = "2026-12-10",
        endDate = "2026-12-15"
    )

    val sampleWeather = WeatherData(
        destination = "Edinburgh",
        currentTemperature = 11.5,
        apparentTemperature = 9.5,
        currentWindSpeed = 2.0,
        currentPrecipitation = 0.0,
        currentWeatherCode = 2,
        dailyForecast = listOf(
            DailyWeather(
                date = "2026-12-10",
                minimumTemperature = 8.6,
                maximumTemperature = 11.7,
                precipitationProbability = 8.0,
                weatherCode = 2
            ),
            DailyWeather(
                date = "2026-12-11",
                minimumTemperature = 5.9,
                maximumTemperature = 8.9,
                precipitationProbability = 90.0,
                weatherCode = 61
            )
        )
    )

    TravelPackTheme {
        WeatherTab(
            trips = listOf(sampleTrip),
            selectedTripId = sampleTrip.id,
            onTripSelected = {},
            weatherState = WeatherUiState(
                destination = "Edinburgh",
                isLoading = false,
                weather = sampleWeather,
                errorMessage = null
            ),
            onRetry = {}
        )
    }
}
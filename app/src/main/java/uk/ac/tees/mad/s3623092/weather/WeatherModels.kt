package uk.ac.tees.mad.s3623092.weather

data class DailyWeather(
    val date: String,
    val minimumTemperature: Double,
    val maximumTemperature: Double,
    val precipitationProbability: Double,
    val weatherCode: Int
)

data class WeatherData(
    val destination: String,
    val currentTemperature: Double,
    val apparentTemperature: Double,
    val currentWindSpeed: Double,
    val currentPrecipitation: Double,
    val currentWeatherCode: Int,
    val dailyForecast: List<DailyWeather>
)

data class WeatherUiState(
    val destination: String = "",
    val isLoading: Boolean = false,
    val weather: WeatherData? = null,
    val errorMessage: String? = null
)
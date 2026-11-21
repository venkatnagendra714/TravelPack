package uk.ac.tees.mad.s3623092.weather

import android.net.Uri

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

import org.json.JSONArray
import org.json.JSONObject

import java.io.IOException
import java.net.HttpURLConnection
import java.net.URL


private data class LocationResult(
    val name: String,
    val latitude: Double,
    val longitude: Double
)


class WeatherRepository {

    suspend fun getWeather(
        destination: String
    ): WeatherData = withContext(Dispatchers.IO) {
        val location = findLocation(destination)

        val forecastUrl =
            "https://api.open-meteo.com/v1/forecast" +
                    "?latitude=${location.latitude}" +
                    "&longitude=${location.longitude}" +
                    "&current=" +
                    "temperature_2m," +
                    "apparent_temperature," +
                    "precipitation," +
                    "weather_code," +
                    "wind_speed_10m" +
                    "&daily=" +
                    "weather_code," +
                    "temperature_2m_max," +
                    "temperature_2m_min," +
                    "precipitation_probability_max" +
                    "&forecast_days=5" +
                    "&timezone=auto"

        val response =
            requestJson(forecastUrl)

        val current =
            response.optJSONObject("current")
                ?: throw IOException(
                    "Current weather data is unavailable."
                )

        val daily =
            response.optJSONObject("daily")
                ?: throw IOException(
                    "Daily weather data is unavailable."
                )

        val currentTemperature =
            requiredDouble(
                current,
                "temperature_2m"
            )

        val apparentTemperature =
            requiredDouble(
                current,
                "apparent_temperature"
            )

        val currentWindSpeed =
            requiredDouble(
                current,
                "wind_speed_10m"
            )

        val currentPrecipitation =
            requiredDouble(
                current,
                "precipitation"
            )

        val currentWeatherCode =
            current.optInt(
                "weather_code",
                -1
            )

        if (currentWeatherCode < 0) {
            throw IOException(
                "Current weather condition is unavailable."
            )
        }

        val dates =
            daily.optJSONArray("time")
                ?: throw IOException(
                    "Forecast dates are unavailable."
                )

        val minimumTemperatures =
            daily.optJSONArray(
                "temperature_2m_min"
            ) ?: throw IOException(
                "Minimum temperatures are unavailable."
            )

        val maximumTemperatures =
            daily.optJSONArray(
                "temperature_2m_max"
            ) ?: throw IOException(
                "Maximum temperatures are unavailable."
            )

        val weatherCodes =
            daily.optJSONArray(
                "weather_code"
            ) ?: throw IOException(
                "Forecast weather conditions are unavailable."
            )

        val precipitationProbabilities =
            daily.optJSONArray(
                "precipitation_probability_max"
            )

        val forecast =
            mutableListOf<DailyWeather>()

        for (index in 0 until dates.length()) {
            val weatherCode =
                weatherCodes.optInt(
                    index,
                    -1
                )

            if (weatherCode < 0) {
                continue
            }

            val minimumTemperature =
                requiredDouble(
                    minimumTemperatures,
                    index
                )

            val maximumTemperature =
                requiredDouble(
                    maximumTemperatures,
                    index
                )

            val precipitationProbability =
                precipitationProbabilities
                    ?.optDouble(index, 0.0)
                    ?: 0.0

            forecast.add(
                DailyWeather(
                    date = dates.optString(index),
                    minimumTemperature =
                        minimumTemperature,
                    maximumTemperature =
                        maximumTemperature,
                    precipitationProbability =
                        precipitationProbability,
                    weatherCode = weatherCode
                )
            )
        }

        WeatherData(
            destination = location.name,
            currentTemperature = currentTemperature,
            apparentTemperature = apparentTemperature,
            currentWindSpeed = currentWindSpeed,
            currentPrecipitation = currentPrecipitation,
            currentWeatherCode = currentWeatherCode,
            dailyForecast = forecast
        )
    }


    private fun findLocation(
        destination: String
    ): LocationResult {
        val cleanDestination =
            destination.trim()

        val firstPart =
            cleanDestination
                .substringBefore(",")
                .trim()

        val locationQueries = listOf(
            cleanDestination,

            cleanDestination.replace(
                oldValue = "UK",
                newValue = "GB",
                ignoreCase = true
            ),

            cleanDestination.replace(
                oldValue = "UK",
                newValue = "United Kingdom",
                ignoreCase = true
            ),

            "$firstPart, GB",

            "$firstPart, United Kingdom",

            firstPart
        ).filter {
            it.isNotBlank()
        }.distinct()

        var lastError: IOException? = null

        for (query in locationQueries) {
            try {
                val location =
                    searchLocation(query)

                if (location != null) {
                    return location
                }
            } catch (exception: IOException) {
                lastError = exception
            }
        }

        throw lastError
            ?: IOException(
                "Destination could not be found."
            )
    }


    private fun searchLocation(
        destination: String
    ): LocationResult? {
        val encodedDestination =
            Uri.encode(destination.trim())

        val geocodingUrl =
            "https://geocoding-api.open-meteo.com/v1/search" +
                    "?name=$encodedDestination" +
                    "&count=1" +
                    "&language=en" +
                    "&format=json"

        val response =
            requestJson(geocodingUrl)

        val results =
            response.optJSONArray("results")
                ?: return null

        if (results.length() == 0) {
            return null
        }

        val firstResult =
            results.optJSONObject(0)
                ?: return null

        val latitude =
            firstResult.optDouble(
                "latitude",
                Double.NaN
            )

        val longitude =
            firstResult.optDouble(
                "longitude",
                Double.NaN
            )

        if (
            latitude.isNaN() ||
            longitude.isNaN()
        ) {
            return null
        }

        val name =
            firstResult
                .optString("name")
                .ifBlank {
                    destination.trim()
                }

        return LocationResult(
            name = name,
            latitude = latitude,
            longitude = longitude
        )
    }


    private fun requestJson(
        urlString: String
    ): JSONObject {
        val connection =
            URL(urlString).openConnection()
                    as HttpURLConnection

        connection.requestMethod = "GET"
        connection.connectTimeout = 15_000
        connection.readTimeout = 15_000
        connection.doInput = true

        return try {
            val responseCode =
                connection.responseCode

            val inputStream =
                if (responseCode in 200..299) {
                    connection.inputStream
                } else {
                    connection.errorStream
                }

            val responseText =
                inputStream
                    ?.bufferedReader()
                    ?.use { reader ->
                        reader.readText()
                    }
                    .orEmpty()

            if (responseCode !in 200..299) {
                throw IOException(
                    "Weather service error: $responseCode"
                )
            }

            if (responseText.isBlank()) {
                throw IOException(
                    "Weather service returned no data."
                )
            }

            JSONObject(responseText)
        } finally {
            connection.disconnect()
        }
    }


    private fun requiredDouble(
        json: JSONObject,
        key: String
    ): Double {
        val value =
            json.optDouble(
                key,
                Double.NaN
            )

        if (value.isNaN()) {
            throw IOException(
                "Weather value is unavailable: $key"
            )
        }

        return value
    }


    private fun requiredDouble(
        array: JSONArray,
        index: Int
    ): Double {
        val value =
            array.optDouble(
                index,
                Double.NaN
            )

        if (value.isNaN()) {
            throw IOException(
                "Forecast value is unavailable."
            )
        }

        return value
    }
}
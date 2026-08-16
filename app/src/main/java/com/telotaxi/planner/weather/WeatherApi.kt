package com.telotaxi.planner.weather

import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.GET
import retrofit2.http.Query

data class WeatherResponse(
    val current: CurrentWeather,
    val daily: DailyWeather,
    val hourly: HourlyWeather
)

data class CurrentWeather(
    val temperature_2m: Double,
    val apparent_temperature: Double,
    val relative_humidity_2m: Int,
    val wind_speed_10m: Double,
    val weather_code: Int,
    val precipitation: Double
)

data class DailyWeather(
    val time: List<String>,
    val temperature_2m_max: List<Double>,
    val temperature_2m_min: List<Double>,
    val precipitation_probability_max: List<Int>,
    val weather_code: List<Int>,
    val wind_speed_10m_max: List<Double>
)

data class HourlyWeather(
    val time: List<String>,
    val temperature_2m: List<Double>,
    val weather_code: List<Int>,
    val precipitation_probability: List<Int>
)

interface OpenMeteoApi {
    @GET("v1/forecast")
    suspend fun getForecast(
        @Query("latitude") latitude: Double,
        @Query("longitude") longitude: Double,
        @Query("current") current: String = "temperature_2m,apparent_temperature,relative_humidity_2m,wind_speed_10m,weather_code,precipitation",
        @Query("hourly") hourly: String = "temperature_2m,weather_code,precipitation_probability",
        @Query("daily") daily: String = "temperature_2m_max,temperature_2m_min,precipitation_probability_max,weather_code,wind_speed_10m_max",
        @Query("timezone") timezone: String = "auto",
        @Query("forecast_days") days: Int = 2
    ): WeatherResponse

    companion object {
        fun create(): OpenMeteoApi {
            return Retrofit.Builder()
                .baseUrl("https://api.open-meteo.com/")
                .addConverterFactory(GsonConverterFactory.create())
                .build()
                .create(OpenMeteoApi::class.java)
        }
    }
}

/** Traduit le code météo WMO d'Open-Meteo en libellé et emoji lisibles pour le chauffeur. */
object WeatherCodeMapper {
    fun describe(code: Int): Pair<String, String> = when (code) {
        0 -> "Ciel dégagé" to "☀️"
        1, 2 -> "Peu nuageux" to "🌤️"
        3 -> "Couvert" to "☁️"
        45, 48 -> "Brouillard" to "🌫️"
        51, 53, 55 -> "Bruine" to "🌦️"
        56, 57 -> "Bruine verglaçante" to "🌧️"
        61, 63, 65 -> "Pluie" to "🌧️"
        66, 67 -> "Pluie verglaçante" to "🌧️"
        71, 73, 75, 77 -> "Neige" to "❄️"
        80, 81, 82 -> "Averses" to "🌧️"
        85, 86 -> "Averses de neige" to "🌨️"
        95 -> "Orage" to "⛈️"
        96, 99 -> "Orage avec grêle" to "⛈️"
        else -> "Conditions variables" to "🌡️"
    }

    /** Indique si les conditions justifient une vigilance particulière au volant. */
    fun isRiskyForDriving(code: Int): Boolean = code in listOf(45, 48, 56, 57, 65, 66, 67, 75, 82, 86, 95, 96, 99)
}

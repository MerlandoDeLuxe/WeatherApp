package com.example.weatherapp.data.network.api

import com.example.weatherapp.data.network.dto.CityDto
import com.example.weatherapp.data.network.dto.WeatherCurrentDto
import com.example.weatherapp.data.network.dto.WeatherForecastDto
import retrofit2.http.GET
import retrofit2.http.Query

interface ApiService {

    @GET("current.json")
    suspend fun loadCurrentWeather(
        @Query("q") query: String,
        @Query("key") apiKey: String = API_KEY
    ) : WeatherCurrentDto

    @GET("forecast.json")
    suspend fun loadForecast(
        @Query("q") query: String,
        @Query("days") daysCount: Int = MIN_COUNT_FORECAST_DAYS,
        @Query("key") apiKey: String = API_KEY
    ): WeatherForecastDto

    @GET("search.json")
    suspend fun searchCity(
        @Query("q") query: String,
        @Query("key") apiKey: String = API_KEY
    ) : List<CityDto>

    companion object {
        private const val API_KEY = ""
        private const val MIN_COUNT_FORECAST_DAYS = 4
    }
}
package com.example.weatherapp.presentation

import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import com.example.weatherapp.data.network.api.ApiFactory
import com.example.weatherapp.presentation.ui.theme.WeatherAppTheme
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        val scope = CoroutineScope(Dispatchers.Main)
        val apiService = ApiFactory.apiService
        scope.launch {
            val currentWeather = apiService.loadCurrentWeather("Ekaterinburg")
            val forecast = apiService.loadForecast("Ekaterinburg")
            val cities = apiService.searchCity("Ekaterinburg")
            Log.d("MainActivity", "currentWeather: $currentWeather")
            Log.d("MainActivity", "forecast: $forecast")
            Log.d("MainActivity", "cities: $cities")
        }

        setContent {
            WeatherAppTheme {

            }
        }
    }
}

package com.example.weatherapp.domain.repository

import com.example.weatherapp.domain.entity.Forecast
import com.example.weatherapp.domain.entity.Weather

interface WeatherRepository {

    //Чтобы загружать погоду для городов на главном экране, нужно это делать в другом потоке
    suspend fun getWeather(cityId: Int): Weather

    //На окне детальной информации нужно получать и погоду и прогноз на несколько дней
    suspend fun getForecast(cityId: Int): Forecast

}
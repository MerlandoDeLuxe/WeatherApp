package com.example.weatherapp.domain.entity

import java.util.Calendar

data class Weather(
    val temperature: Float,
    val conditionText: String, //Туман, Солнечно и т.д.
    val conditionUrl: String,  //Картинка облаков и т.д.
    val date: Calendar
)

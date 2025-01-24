package com.example.weatherapp

import android.app.Application
import com.example.weatherapp.di.DaggerApplicationComponent


class WeatherApp : Application() {

    val component by lazy {
        DaggerApplicationComponent.factory()
            .create(this)
    }
}